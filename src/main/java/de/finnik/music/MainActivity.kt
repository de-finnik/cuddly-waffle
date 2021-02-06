package de.finnik.music

import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bawaviki.ffmpeg.FFmpeg
import com.bawaviki.youtubedl_android.YoutubeDL
import com.bawaviki.youtubedl_android.YoutubeDLRequest
import com.bawaviki.youtubedl_android.YoutubeDLResponse
import com.bawaviki.youtubedl_android.YoutubeDLUpdater.UpdateStatus
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File

class MainActivity : AppCompatActivity() {

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


        updateYoutubeDL()
        FFmpeg.getInstance().init(application, this)

        /**File(application.filesDir, "audio").listFiles().forEach {
            Log.i("TAG", "onCreate: filelist: ${it.absolutePath}")
        }
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource("/data/user/0/de.finnik.music/files/audio/n4RjJKxsamQ.opus")
        Log.i("TAG", "onCreate: ${mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)}")
         **/
    }


    private fun updateYoutubeDL() {
        val disposable =
            Observable.fromCallable {
                YoutubeDL.getInstance().updateYoutubeDL(
                    application
                )
            }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ status: UpdateStatus ->
                    when (status) {
                        UpdateStatus.DONE -> Toast.makeText(
                            this@MainActivity,
                            "update successful",
                            Toast.LENGTH_LONG
                        ).show()
                        UpdateStatus.ALREADY_UP_TO_DATE -> Toast.makeText(
                            this@MainActivity,
                            "already up to date",
                            Toast.LENGTH_LONG
                        ).show()
                        else -> Toast.makeText(
                            this@MainActivity,
                            status.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }) { e: Throwable? ->
                    Toast.makeText(this@MainActivity, "download failed", Toast.LENGTH_LONG).show()
                }
    }

}