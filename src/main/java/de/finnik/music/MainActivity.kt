package de.finnik.music

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bawaviki.ffmpeg.FFmpeg
import com.bawaviki.youtubedl_android.DownloadProgressCallback
import com.bawaviki.youtubedl_android.YoutubeDL
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import de.finnik.music.download.DownloadTask
import de.finnik.music.player.MusicPlayerView
import de.finnik.music.songs.Playlist
import de.finnik.music.songs.PlaylistStore
import de.finnik.music.songs.Song
import de.finnik.music.songs.SongList
import de.finnik.music.ui.player.PlayerActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class MainActivity : AppCompatActivity() {

    lateinit var musicPlayerView: MusicPlayerView
    val songs: SongList = SongList()
    lateinit var playlistStore: PlaylistStore

    lateinit var audio_dir: File
    lateinit var playlist_dir: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        songs.clear()


        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        musicPlayerView = findViewById(R.id.music_player_view)
        musicPlayerView.setOnClickListener {
            val intent = Intent(this, PlayerActivity().javaClass)
            startActivity(intent)
            overridePendingTransition(R.anim.bottom_up, R.anim.nothing)
        }

        updateYoutubeDL()
        FFmpeg.getInstance().init(application, this)

        audio_dir = File(application.filesDir, "audio")
        audio_dir.mkdirs()

        playlist_dir = File(application.filesDir, "playlist")
        playlist_dir.mkdirs()

        if (intent.action == "android.intent.action.SEND") {
            val url = intent.extras?.get(Intent.EXTRA_TEXT) as String
            DownloadTask.download(
                url,
                audio_dir,
                DownloadProgressCallback { progress, size, rate, etaInSeconds -> })
                .execute {
                    loadSongs()
                }
        }

        loadSongs()
        playlistStore = PlaylistStore(Playlist(songs.map { it.id }.toMutableList(), getString(R.string.playlist_all)), playlist_dir)

        File(application.filesDir, "playlist").listFiles().forEach {
            Log.i("TAG", "onCreate: filelist: ${it.absolutePath}")
        }
    }

    fun loadSongs() {
        songs.clear()
        songs.addAll(Song.findSongs(audio_dir))
    }

    fun updateYoutubeDL() {
        GlobalScope.launch {
            val instance = YoutubeDL.getInstance()
            withContext(Dispatchers.IO) {
                instance.updateYoutubeDL(application)
            }
        }
    }
}