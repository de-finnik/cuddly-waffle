package de.finnik.music.player

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.widget.SeekBar
import de.finnik.music.songs.Playlist
import de.finnik.music.songs.Song
import de.finnik.music.songs.SongPlayer
import java.util.function.Consumer

class MusicPlayerService : Service() {
    lateinit var player: MediaPlayer
    private val binder = MusicBinder()

    private lateinit var songPlayer: SongPlayer

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

    fun initSongPlayer(songs: List<Song>, playlist: Playlist) {
        songPlayer = SongPlayer(songs)
        setPlaylist(playlist)
    }

    fun isInitialized(): Boolean {
        return this::songPlayer.isInitialized
    }

    fun setPlaylist(playlist: Playlist) {
        songPlayer.play(playlist.ids)
    }

    private fun initSeekbars() {
        val handler = Handler()
        val onSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    player.seekTo(progress * 1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
        val updateSeekBar = object : Runnable {
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
            val myUri: Uri = Uri.fromFile(songPlayer.getCurrentSong().audio)
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
        player.setOnCompletionListener { ACTION_NEXT() }
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
    }

    fun addPauseListener(consumer: Consumer<Song>) {
        onPause.add(consumer)
    }

    fun addPlayListener(consumer: Consumer<Song>) {
        onPlay.add(consumer)
    }

    fun play(query: List<String>) {
        songPlayer.play(query)

        songChange()
    }

    fun getCurrentSong(): Song {
        return songPlayer.getCurrentSong()
    }

    fun isPlaying(): Boolean {
        return player.isPlaying
    }

    private fun songChange() {
        onSongChange.call(songPlayer.getCurrentSong())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            ACTION_PLAY -> ACTION_PLAY()
            ACTION_PAUSE -> ACTION_PAUSE()
            ACTION_PREVIOUS -> ACTION_PREVIOUS()
            ACTION_NEXT -> ACTION_NEXT()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun ACTION_PLAY() {
        onPlay.call(songPlayer.getCurrentSong())
    }

    fun ACTION_PAUSE() {
        onPause.call(songPlayer.getCurrentSong())
    }

    fun ACTION_PREVIOUS() {
        songPlayer.previous()
        songChange()
    }

    fun ACTION_NEXT() {
        songPlayer.next()
        songChange()
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