package de.finnik.music.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bawaviki.youtubedl_android.mapper.VideoInfo
import de.finnik.music.R

class DownloadNotification(val context: Context, val info: VideoInfo) {
    private val channelId = "CHANNEL_2"

    val builder = NotificationCompat.Builder(context, channelId)

    init {
        builder.setContentTitle(info.title)
        builder.setSmallIcon(R.drawable.ic_notifications_black_24dp)
        builder.setContentText(info.uploader)
        createNotificationChannel(context)
    }

    fun showProgress(progress: Int) {
        if(progress == 100) {
            builder.setContentTitle("Download complete")
            builder.setContentText("${info.title} - ${info.uploader}")
            builder.setProgress(0,0, false)
        } else {
            builder.setProgress(100, progress, false)
        }
        showNotification()
    }

    private fun showNotification() {
        with(NotificationManagerCompat.from(context)) {
            notify(info.id.hashCode(), builder.build())
        }
    }

    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Download Service"
            val descriptionText = "Shows the notification that indicates the progress of a downloading song"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}