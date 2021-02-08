package de.finnik.music.ui.player

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import de.finnik.music.MainActivity
import de.finnik.music.R
import de.finnik.music.Utils
import de.finnik.music.player.MusicPlayerService
import java.util.function.Consumer

class PlayerActivity : Activity() {
    private var connectedToService = false
    private lateinit var musicPlayerService: MusicPlayerService

    private lateinit var iv_close: ImageView
    private lateinit var iv_thumbnail: ImageView
    private lateinit var tv_title: TextView
    private lateinit var tv_artist: TextView
    private lateinit var sb_duration: SeekBar
    private lateinit var iv_previous: ImageView
    private lateinit var iv_play: ImageView
    private lateinit var iv_next: ImageView

    private val serviceConnection = object: ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            connectedToService = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            connectedToService = true
            musicPlayerService = (service as MusicPlayerService.MusicBinder).service
            musicPlayerService.registerSeekBar(sb_duration)
            musicPlayerService.addSongChangeListener(Consumer {
                iv_thumbnail.setImageDrawable(BitmapDrawable(resources, it.thumbnail))
                tv_title.setText(it.title)
                tv_artist.setText(it.artist)
            })
            musicPlayerService.addPlayListener(Consumer {
                iv_play.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_pause))
            })
            musicPlayerService.addPauseListener(Consumer {
                iv_play.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_play))
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        iv_close = findViewById(R.id.iv_close)
        iv_thumbnail = findViewById(R.id.iv_player_thumbnail)
        tv_title = findViewById(R.id.tv_player_title)
        tv_artist = findViewById(R.id.tv_player_artist)
        sb_duration = findViewById(R.id.sb_duration)
        iv_previous = findViewById(R.id.iv_player_previous)
        iv_play = findViewById(R.id.iv_player_play)
        iv_next = findViewById(R.id.iv_player_next)

        iv_play.setOnClickListener {
            if (musicPlayerService.isPlaying()) {
                musicPlayerService.ACTION_PAUSE()
            } else {
                musicPlayerService.ACTION_PLAY()
            }
        }
        iv_next.setOnClickListener { musicPlayerService.ACTION_NEXT() }
        iv_previous.setOnClickListener { musicPlayerService.ACTION_PREVIOUS() }

        iv_close.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.nothing, R.anim.bottom_down)
        }

        Intent(this, MusicPlayerService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }


    }
}