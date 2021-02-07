package de.finnik.music.player

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.SeekBar
import de.finnik.music.Song
import java.util.function.Consumer

class MusicPlayerService: Service() {
    lateinit var player: MediaPlayer
    private val binder = MusicBinder()

    private lateinit var currentSong: Song

    private val onSongChange: SongListener = SongListener()
    private val onPause: SongListener = SongListener()
    private val onPlay: SongListener = SongListener()

    private val seekBars: MutableList<SeekBar> = ArrayList()

    override fun onCreate() {
        super.onCreate()

        initPlayer()
        initNotification()

        initSeekbars()
    }

    private fun initSeekbars() {
        val handler = Handler()
        val onSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    player.seekTo(progress * 1000)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
        val updateSeekBar = object: Runnable {
            override fun run() {
                seekBars.forEach {
                    it.max = player.duration / 1000
                    it.progress = player.currentPosition / 1000
                    it.setOnSeekBarChangeListener(onSeekBarChangeListener)
                }
                handler.postDelayed(this, 500)
            }
        }
        addSongChangeListener(Consumer {
            handler.post(updateSeekBar)
        })
        addPauseListener(Consumer {
            handler.removeCallbacks(updateSeekBar)
        })
        addPlayListener(Consumer {
            handler.post(updateSeekBar)
        })
    }

    private fun initPlayer() {
        player = MediaPlayer()
        addSongChangeListener(Consumer {
            val myUri: Uri = Uri.fromFile(currentSong.audio)
            player.apply {
                reset()
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(applicationContext, myUri)
                prepare()
                start()
            }
        })
        addPauseListener(Consumer { player.pause() })
        addPlayListener(Consumer { player.start() })
    }

    private fun initNotification() {
        val notification = MusicNotification(applicationContext)
        addSongChangeListener(Consumer { notification.showPause(it) })
        addPlayListener(Consumer { notification.showPause(it) })
        addPauseListener(Consumer { notification.showPlay(it) })
    }

    fun registerSeekBar(seekBar: SeekBar) {
        seekBars.add(seekBar)
    }

    fun addSongChangeListener(consumer: Consumer<Song>) {
        onSongChange.add(consumer)
        if(isPlaying()) {
            consumer.accept(currentSong)
        }
    }

    fun addPauseListener(consumer: Consumer<Song>) {
        onPause.add(consumer)
    }

    fun addPlayListener(consumer: Consumer<Song>) {
        onPlay.add(consumer)
    }

    fun play(song: Song) {
        currentSong = song

        songChange()
    }

    fun isPlaying(): Boolean {
        return player.isPlaying
    }

    private fun songChange() {
        onSongChange.call(currentSong)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            ACTION_PLAY -> ACTION_PLAY()
            ACTION_PAUSE -> ACTION_PAUSE()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun ACTION_PLAY() {
        onPlay.call(currentSong)
    }

    fun ACTION_PAUSE() {
        onPause.call(currentSong)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    inner class MusicBinder : Binder() {
        val service: MusicPlayerService
            get() = this@MusicPlayerService


    }

    companion object {
        val ACTION_NEXT: String = "de.finnik.music.ACTION_NEXT"
        val ACTION_PREVIOUS: String = "de.finnik.music.ACTION_PREVIOUS"
        val ACTION_PLAY = "de.finnik.music.ACTION_PLAY"
        val ACTION_PAUSE = "de.finnik.music.ACTION_PAUSE"
    }

    class SongListener {
        private val consumers: MutableList<Consumer<Song>>
        init {
            consumers = ArrayList()
        }
        fun add(consumer: Consumer<Song>) {
            consumers.add(consumer)
        }
        fun call(song: Song) {
            consumers.forEach { it.accept(song) }
        }
    }
}