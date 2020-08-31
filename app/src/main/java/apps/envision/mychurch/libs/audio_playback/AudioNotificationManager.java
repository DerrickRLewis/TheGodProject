package apps.envision.mychurch.libs.audio_playback;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import apps.envision.mychurch.App;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.ui.activities.MainActivity;
import apps.envision.mychurch.R;

public class AudioNotificationManager {

    public static final int NOTIFICATION_ID = 101;
    static final String PLAY_PAUSE_ACTION = "envision.apps.mychurch.PLAYPAUSE";
    static final String NEXT_ACTION = "envision.apps.mychurch.NEXT";
    static final String PREV_ACTION = "envision.apps.mychurch.PREV";
    static final String STOP_ACTION = "envision.apps.mychurch.STOP";
    private final String CHANNEL_ID = "envision.apps.mychurch.ENVISIONAPPS";
    private final int REQUEST_CODE = 100;
    private final NotificationManager mNotificationManager;
    private final AudioPlayerService mMusicService;
    private NotificationCompat.Builder mNotificationBuilder;

    AudioNotificationManager(@NonNull final AudioPlayerService musicService) {
        mMusicService = musicService;
        mNotificationManager = (NotificationManager) mMusicService.getSystemService(Context.NOTIFICATION_SERVICE);
    }


    public final NotificationManager getNotificationManager() {
        return mNotificationManager;
    }

    public final NotificationCompat.Builder getNotificationBuilder() {
        return mNotificationBuilder;
    }

    public void stopNofication(){
        mNotificationManager.cancelAll();
    }

    public Notification createLoadingNotification() {
        final Media song = mMusicService.getMediaPlayerHolder().getCurrentMedia();

        mNotificationBuilder = new NotificationCompat.Builder(mMusicService, CHANNEL_ID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        final Intent openPlayerIntent = new Intent(mMusicService, MainActivity.class);
        openPlayerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent contentIntent = PendingIntent.getActivity(mMusicService, REQUEST_CODE,
                openPlayerIntent, 0);

        RemoteViews views = new RemoteViews(mMusicService.getPackageName(),
                R.layout.status_bar_loader);

        if(!song.getCover_photo().equalsIgnoreCase("")  || !song.getCover_photo().equalsIgnoreCase("null")) {
            setImageBitmap(views,song.getCover_photo());
        }


        Intent previousIntent = new Intent();
        previousIntent.setAction(PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getBroadcast(mMusicService, REQUEST_CODE, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent nextIntent = new Intent();
        nextIntent.setAction(NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getBroadcast(mMusicService, REQUEST_CODE, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent closeIntent = new Intent();
        closeIntent.setAction(STOP_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getBroadcast(mMusicService, REQUEST_CODE, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);
        views.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);
        views.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);

        views.setTextViewText(R.id.status_bar_track_name, song.getTitle());

        mNotificationBuilder
                .setShowWhen(false)
                .setCustomContentView(views)
                .setSmallIcon(R.drawable.image)
                .setContentIntent(contentIntent)
                .setAutoCancel(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        return mNotificationBuilder.build();
    }


    public Notification createNotification() {
        final Media song = mMusicService.getMediaPlayerHolder().getCurrentMedia();

        mNotificationBuilder = new NotificationCompat.Builder(mMusicService, CHANNEL_ID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        final Intent openPlayerIntent = new Intent(mMusicService, MainActivity.class);
        openPlayerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent contentIntent = PendingIntent.getActivity(mMusicService, REQUEST_CODE,
                openPlayerIntent, 0);

        RemoteViews views = new RemoteViews(mMusicService.getPackageName(),
                R.layout.music_notification);

        if(!song.getCover_photo().equalsIgnoreCase("")  || !song.getCover_photo().equalsIgnoreCase("null")) {
            setImageBitmap(views,song.getCover_photo());
        }


        Intent previousIntent = new Intent();
        previousIntent.setAction(PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getBroadcast(mMusicService, REQUEST_CODE, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent nextIntent = new Intent();
        nextIntent.setAction(NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getBroadcast(mMusicService, REQUEST_CODE, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);



        Intent playIntent = new Intent();
        playIntent.setAction(PLAY_PAUSE_ACTION);
        PendingIntent pplayIntent = PendingIntent.getBroadcast(mMusicService, REQUEST_CODE, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent closeIntent = new Intent();
        closeIntent.setAction(STOP_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getBroadcast(mMusicService, REQUEST_CODE, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
        views.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);
        views.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);
        views.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);

        if(mMusicService.getMediaPlayerHolder().getState() != PlaybackInfoListener.State.PAUSED) {
            //  views.setImageViewResource(R.id.status_bar_play,
            //         R.drawable.ic_pause_36dp);
            views.setImageViewResource(R.id.status_bar_play,
                    R.drawable.music_pause_button);
        }else{
            views.setImageViewResource(R.id.status_bar_play,
                    R.drawable.music_play_button);
        }
        views.setTextViewText(R.id.status_bar_track_name, song.getTitle());

        mNotificationBuilder
                .setShowWhen(false)
                .setCustomContentView(views)
                .setSmallIcon(R.drawable.image)
                .setContentText("Now Playing")
                .setContentIntent(contentIntent)
                .setAutoCancel(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        return mNotificationBuilder.build();

    }

    @RequiresApi(26)
    private void createNotificationChannel() {

        if (mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            final NotificationChannel notificationChannel =
                    new NotificationChannel(CHANNEL_ID,
                            mMusicService.getString(R.string.app_name),
                            NotificationManager.IMPORTANCE_LOW);

            notificationChannel.setDescription(
                    mMusicService.getString(R.string.app_name));

            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            notificationChannel.setShowBadge(false);

            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void setImageBitmap(RemoteViews views, String url) {
        Glide.with(App.getContext())
                .asBitmap().load(url)
                .listener(new RequestListener<Bitmap>() {
                              @Override
                              public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Bitmap> target, boolean b) {

                                  return false;
                              }

                              @Override
                              public boolean onResourceReady(Bitmap bitmap, Object o, Target<Bitmap> target, DataSource dataSource, boolean b) {
                                  views.setImageViewBitmap(R.id.status_bar_album_art, bitmap);
                                  return false;
                              }
                          }
                ).submit();
    }
}
