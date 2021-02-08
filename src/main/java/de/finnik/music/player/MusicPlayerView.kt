package de.finnik.music.player

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import de.finnik.music.R
import de.finnik.music.songs.Song
import java.util.function.Consumer

class MusicPlayerView(context: Context, attributeSet: AttributeSet) :
    RelativeLayout(context, attributeSet) {

    val iv_thumbnail: ImageView
    val tv_title: TextView
    val tv_artist: TextView
    val iv_previous: ImageView
    val iv_play: ImageView
    val iv_next: ImageView

    lateinit var musicPlayerService: MusicPlayerService

    init {
        inflate(context, R.layout.music_player, this)
        iv_thumbnail = findViewById(R.id.iv_player_thumbnail)
        tv_title = findViewById(R.id.tv_player_title)
        tv_artist = findViewById(R.id.tv_player_artist)
        iv_previous = findViewById(R.id.iv_player_previous)
        iv_play = findViewById(R.id.iv_player_play)
        iv_next = findViewById(R.id.iv_player_next)
    }

    fun setService(musicPlayerService: MusicPlayerService) {
        this.musicPlayerService = musicPlayerService
        if(musicPlayerService.isPlaying()) {
            visibility = VISIBLE
        }
        musicPlayerService.addSongChangeListener(Consumer {
            if (visibility == GONE)
                visibility = VISIBLE
            displaySong(it)
        })
        musicPlayerService.addPauseListener(Consumer {
            iv_play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play))
        })
        musicPlayerService.addPlayListener(Consumer {
            iv_play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pause))
        })
        iv_play.setOnClickListener {
            if (musicPlayerService.isPlaying())
                musicPlayerService.ACTION_PAUSE()
            else
                musicPlayerService.ACTION_PLAY()
        }
        iv_previous.setOnClickListener { musicPlayerService.ACTION_PREVIOUS() }
        iv_next.setOnClickListener { musicPlayerService.ACTION_NEXT() }
    }

    private fun displaySong(song: Song) {
        iv_thumbnail.setImageDrawable(BitmapDrawable(context.resources, song.thumbnail))
        tv_title.setText(song.title)
        tv_artist.setText(song.artist)
    }
}