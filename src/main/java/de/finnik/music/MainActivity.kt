package de.finnik.music

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bawaviki.ffmpeg.FFmpeg
import com.bawaviki.youtubedl_android.YoutubeDL
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.finnik.music.player.MusicPlayerView
import de.finnik.music.ui.player.PlayerActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    lateinit var musicPlayerView: MusicPlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
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

        /**File(application.filesDir, "audio").listFiles().forEach {
            Log.i("TAG", "onCreate: filelist: ${it.absolutePath}")
        }**/
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