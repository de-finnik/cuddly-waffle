package de.finnik.music;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class MediaPlayerService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    MediaSession mediaSession;
    Bundle mediaSessionExtras;

    @Override
    public void onCreate() {
        super.onCreate();

        mediaSession = new MediaSession(this, "MusicService");
        mediaSession.setCallback(new MediaSession.Callback() {

        });
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        Context context = getApplicationContext();
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 99 /*request code*/,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mediaSession.setSessionActivity(pi);

        mediaSessionExtras = new Bundle();
        mediaSession.setExtras(mediaSessionExtras);
    }
}
