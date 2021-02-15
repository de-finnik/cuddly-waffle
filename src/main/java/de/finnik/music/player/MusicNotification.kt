package de.finnik.music.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.finnik.music.R
import de.finnik.music.songs.Song
import de.finnik.music.Utils
import de.finnik.music.ui.player.PlayerActivity

class MusicNotification(val context: Context) {
    private val channelId = "CHANNEL_1"
    private lateinit var builder: NotificationCompat.Builder
    private val actionPlay: NotificationCompat.Action
    private val actionPause: NotificationCompat.Action
    private val actionNext: NotificationCompat.Action
    private val actionPrevious: NotificationCompat.Action

    private val colors: HashMap<String, Int> = HashMap()

    init {
        actionPlay = NotificationCompat.Action(R.drawable.ic_play, "Play", pendingIntent(context, MusicPlayerService.ACTION_PLAY))
        actionPause = NotificationCompat.Action(R.drawable.ic_pause, "Pause", pendingIntent(context, MusicPlayerService.ACTION_PAUSE))
        actionPrevious = NotificationCompat.Action(R.drawable.ic_previous, "Previous", pendingIntent(context, MusicPlayerService.ACTION_PREVIOUS))
        actionNext = NotificationCompat.Action(R.drawable.ic_next, "Next", pendingIntent(context, MusicPlayerService.ACTION_NEXT))

        createNotificationChannel(context)
    }

    private fun setupBuilder(song: Song, action: NotificationCompat.Action) {
        if(colors.containsKey(song.videoInfo.id).not()) {
            colors[song.id] = Utils.averageColor(song.thumbnail)
        }
        builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setLargeIcon(song.thumbnail)
            .addAction(actionPrevious)
            .addAction(action)
            .addAction(actionNext)
            .setColor(colors[song.id]!!)
            .setContentIntent(PendingIntent.getActivity(context, 100, Intent(context, PlayerActivity::class.java), 0))
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0,1,2))
    }

    fun showPlay(song: Song) {
        setupBuilder(song, actionPlay)
        showNotification()
    }

    fun showPause(song: Song) {
        setupBuilder(song, actionPause)
        builder.setOngoing(true)
        showNotification()
    }

    private fun showNotification() {
        with(NotificationManagerCompat.from(context)) {
            notify(2122, builder.build())
        }
    }

    private fun pendingIntent(context: Context, name: String): PendingIntent {
        val intent = Intent(name)
        val pendingIntent = PendingIntent.getService(context, 100, intent, 0)
        return pendingIntent
    }

    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Music Service"
            val descriptionText = "Shows the notification to control the music service"
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