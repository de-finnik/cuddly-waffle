package de.finnik.music.player

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import de.finnik.music.Song
import java.util.function.Consumer

class MusicPlayerService: Service() {
    lateinit var player: MediaPlayer
    lateinit var songs: MutableList<Song>
    var songPosn: Int = 0
    private val binder = MusicBinder()

    override fun onCreate() {
        super.onCreate()
        player = MediaPlayer()
    }

    fun play(song: Song) {
        val myUri: Uri = Uri.fromFile(song.audio)
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
    }

    lateinit var consumer: Consumer<Boolean>

    fun doOnPause(consumer: Consumer<Boolean>) {
        this.consumer = consumer
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        Log.i("TAG", "onStartCommand: $action")
        when (action) {
            ACTION_PLAY -> {
                player.start()
                consumer.accept(player.isPlaying)
            }
            ACTION_PAUSE -> {
                player.pause()
                Log.i("TAG", "onStartCommand: ${player.isPlaying}")
                consumer.accept(player.isPlaying)
            }
        }
        return super.onStartCommand(intent, flags, startId)
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
}