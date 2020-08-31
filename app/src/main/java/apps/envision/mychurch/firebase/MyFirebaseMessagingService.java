package apps.envision.mychurch.firebase;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.Events;
import apps.envision.mychurch.pojo.Inbox;
import apps.envision.mychurch.pojo.Livestreams;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.pojo.Notification;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.socials.SocialActivity;
import apps.envision.mychurch.ui.activities.ActivityVideoPlayer;
import apps.envision.mychurch.ui.activities.AudioPlayerActivity;
import apps.envision.mychurch.ui.activities.EventsViewerActivity;
import apps.envision.mychurch.ui.activities.InboxViewerActivity;
import apps.envision.mychurch.ui.activities.LiveStreamsPlayer;
import apps.envision.mychurch.ui.activities.MainActivity;
import apps.envision.mychurch.utils.Constants;
import apps.envision.mychurch.utils.JsonParser;
import apps.envision.mychurch.utils.Utility;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d("NEW_TOKEN",s);
        PreferenceSettings.setFcmToken(s);
        //store recieved token to server database
        App.sendUserTokenToServer(false);
    }

    /**
     *
     * @param remoteMessage
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.e("data",String.valueOf(remoteMessage.getData()));
        if (remoteMessage.getNotification() != null) {
            Log.e("notification", "Message Notification Body: " + remoteMessage.getNotification().getBody());
            Notification notification = new Notification(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody());
            LocalMessageManager.getInstance().send(R.id.home_screen_notification,notification);

        }
        if(remoteMessage.getData().get("action")==null)return;
        //if remotemessage action is new media, we construct and notify user
        if("social_notify".equalsIgnoreCase(Objects.requireNonNull(remoteMessage.getData().get("action")))){
            if(Utility.isUserAccountBlocked())return; //if user account is blocked, dont notify
            String title = remoteMessage.getData().get("title");
            String avatar = remoteMessage.getData().get("avatar");
            String message = remoteMessage.getData().get("message");
            String email = remoteMessage.getData().get("email");
            UserData userData = PreferenceSettings.getUserData();
            if(userData!=null && userData.getEmail().equalsIgnoreCase(email)) {
                Glide.with(App.getContext())
                        .asBitmap().load(avatar)
                        .listener(new RequestListener<Bitmap>() {
                                      @Override
                                      public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Bitmap> target, boolean b) {
                                          user_action_notifications(title, null, message);
                                          return false;
                                      }

                                      @Override
                                      public boolean onResourceReady(Bitmap bitmap, Object o, Target<Bitmap> target, DataSource dataSource, boolean b) {
                                          user_action_notifications(title, bitmap, message);
                                          return true;
                                      }
                                  }
                        ).submit();
                LocalMessageManager.getInstance().send(R.id.reload_notifications);
            }
        }else if("newMedia".equalsIgnoreCase(Objects.requireNonNull(remoteMessage.getData().get("action")))){
            if(Utility.isUserAccountBlocked())return; //if user account is blocked, dont notify
            Media media = JsonParser.getMedia(remoteMessage.getData().get("media"));
            Log.d("new media",media.getTitle());
            Glide.with(App.getContext())
                    .asBitmap().load(media.getCover_photo())
                    .listener(new RequestListener<Bitmap>() {
                                  @Override
                                  public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Bitmap> target, boolean b) {
                                      notify_user(remoteMessage.getData().get("title"),null,media);
                                      return false;
                                  }

                                  @Override
                                  public boolean onResourceReady(Bitmap bitmap, Object o, Target<Bitmap> target, DataSource dataSource, boolean b) {
                                      notify_user(remoteMessage.getData().get("title"),bitmap,media);
                                      return true;
                                  }
                              }
                    ).submit();
        }else if("editMedia".equalsIgnoreCase(Objects.requireNonNull(remoteMessage.getData().get("action")))){
            //if remotemessage action is edited media, we update all media files
            Media media = JsonParser.getMedia(remoteMessage.getData().get("media"));
            Log.d("edited media",media.getTitle());
            Utility.update_app_database(media);

        }else if("inbox".equalsIgnoreCase(Objects.requireNonNull(remoteMessage.getData().get("action")))){
            //if remotemessage action is edited media, we update all media files
            Inbox inbox = JsonParser.getInbox(remoteMessage.getData().get("inbox"));

            Intent resultIntent = new Intent(App.getContext(), InboxViewerActivity.class);
            Gson gson = new Gson();
            String json = gson.toJson(inbox);
            resultIntent.putExtra("inbox", json);
            // Create the TaskStackBuilder and add the intent, which inflates the back stack
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(App.getContext());
            stackBuilder.addNextIntentWithParentStack(resultIntent);
            // Get the PendingIntent containing the entire back stack
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(App.getContext(), Constants.NOTIFICATION.CHANNEL_ID);
            builder.setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText(inbox.getTitle())
                    .setContentTitle(App.getContext().getResources().getString(R.string.new_message))
                    .setTicker(App.getContext().getResources().getString(R.string.new_message))
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(true);

            builder.setLargeIcon(BitmapFactory.decodeResource(App.getContext().getResources(),
                    R.mipmap.ic_launcher));

            int res_sound_id = App.getContext().getResources().getIdentifier("notification", "raw", App.getContext().getPackageName());
            Uri path = Uri.parse("android.resource://" + App.getContext().getPackageName() + "/" +res_sound_id);
            builder.setSound(path);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(App.getContext());
            notificationManager.notify(100, builder.build());

        }else if("events".equalsIgnoreCase(Objects.requireNonNull(remoteMessage.getData().get("action")))){
            //if remotemessage action is edited media, we update all media files
            Events events = JsonParser.getEvents(remoteMessage.getData().get("events"));

            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
            DateTime dateTime = formatter.parseDateTime(events.getDate());

            Intent resultIntent = new Intent(App.getContext(), EventsViewerActivity.class);
            Gson gson = new Gson();
            String json = gson.toJson(events);
            resultIntent.putExtra("events", json);
            resultIntent.putExtra("date", dateTime.dayOfWeek().getAsText()+" "+dateTime.monthOfYear().getAsText()+" "+dateTime.getYear());
            // Create the TaskStackBuilder and add the intent, which inflates the back stack
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(App.getContext());
            stackBuilder.addNextIntentWithParentStack(resultIntent);
            // Get the PendingIntent containing the entire back stack
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(App.getContext(), Constants.NOTIFICATION.CHANNEL_ID);
            builder.setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText(events.getTitle())
                    .setContentTitle(App.getContext().getResources().getString(R.string.new_event))
                    .setTicker(App.getContext().getResources().getString(R.string.new_event))
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(true);

            builder.setLargeIcon(BitmapFactory.decodeResource(App.getContext().getResources(),
                    R.mipmap.ic_launcher));

            int res_sound_id = App.getContext().getResources().getIdentifier("notification", "raw", App.getContext().getPackageName());
            Uri path = Uri.parse("android.resource://" + App.getContext().getPackageName() + "/" +res_sound_id);
            builder.setSound(path);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(App.getContext());
            notificationManager.notify(100, builder.build());

        }else if("livestream".equalsIgnoreCase(Objects.requireNonNull(remoteMessage.getData().get("action")))){
            //if remotemessage action is edited media, we update all media files
            Livestreams livestreams = JsonParser.getLiveStreams(remoteMessage.getData().get("livestream"));
            if(!livestreams.isStatus())return;
            Intent resultIntent = new Intent(App.getContext(), LiveStreamsPlayer.class);
            Gson gson = new Gson();
            String json = gson.toJson(livestreams);
            resultIntent.putExtra("livestreams", json);
            // Create the TaskStackBuilder and add the intent, which inflates the back stack
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(App.getContext());
            stackBuilder.addNextIntentWithParentStack(resultIntent);
            // Get the PendingIntent containing the entire back stack
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(App.getContext(), Constants.NOTIFICATION.CHANNEL_ID);
            builder.setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText(livestreams.getTitle())
                    .setContentTitle(App.getContext().getResources().getString(R.string.new_livestreaming))
                    .setTicker(App.getContext().getResources().getString(R.string.new_livestreaming))
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(true);

            builder.setLargeIcon(BitmapFactory.decodeResource(App.getContext().getResources(),
                    R.mipmap.ic_launcher));

            int res_sound_id = App.getContext().getResources().getIdentifier("notification", "raw", App.getContext().getPackageName());
            Uri path = Uri.parse("android.resource://" + App.getContext().getPackageName() + "/" +res_sound_id);
            builder.setSound(path);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(App.getContext());
            notificationManager.notify(100, builder.build());

        }else {
            //if action is user related like block, unblock or delete
            //we check if its this user and perform action
            UserData userData = PreferenceSettings.getUserData();
            if (userData != null && userData.getEmail().equalsIgnoreCase(remoteMessage.getData().get("email"))) {
                //send notification to user
                Intent resultIntent = new Intent(App.getContext(), MainActivity.class);
                // Create the TaskStackBuilder and add the intent, which inflates the back stack
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(App.getContext());
                stackBuilder.addNextIntentWithParentStack(resultIntent);
                // Get the PendingIntent containing the entire back stack
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(App.getContext(), Constants.NOTIFICATION.CHANNEL_ID);
                String aVeryLongString = "";
                switch (Objects.requireNonNull(remoteMessage.getData().get("action"))) {
                    case "block":
                        userData.setBlocked(true);
                        builder.setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle(App.getContext().getString(R.string.account_blocked));
                        aVeryLongString = App.getContext().getString(R.string.account_blocked_hint);
                        PreferenceSettings.setUserData(userData);
                        Log.d("action", "block");
                        break;
                    case "unblock":
                        userData.setBlocked(false);
                        builder.setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle(App.getContext().getString(R.string.account_unblocked));
                        aVeryLongString = App.getContext().getString(R.string.account_unblocked_hint);
                        PreferenceSettings.setUserData(userData);
                        Log.d("action", "unblock");
                        break;
                    case "delete":
                        builder.setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle(App.getContext().getString(R.string.account_deleted));
                        aVeryLongString = App.getContext().getString(R.string.account_deleted_hint);
                        PreferenceSettings.setUserData(null);
                        PreferenceSettings.setIsUserLoggedIn(false);
                        Log.d("action", "delete");
                        break;
                }
                builder.setContentIntent(resultPendingIntent);
                builder.setAutoCancel(true);
                builder.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(aVeryLongString));

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(App.getContext());
                notificationManager.notify(200, builder.build());

                //if mainactivity is running, send event to recreate
                LocalMessageManager.getInstance().send(R.id.recreate_mainactivity);
            }
        }
    }

    private void notify_user(String title, Bitmap bitmap, Media media){
        // Create an Intent for the activity you want to start
        Intent resultIntent;
        if(media.getMedia_type().equalsIgnoreCase(App.getContext().getResources().getString(R.string.audio))){
             resultIntent = new Intent(App.getContext(), AudioPlayerActivity.class);
        }else{
            resultIntent = new Intent(App.getContext(), ActivityVideoPlayer.class);
        }
        Gson gson = new Gson();
        String myJson = gson.toJson(media);
        resultIntent.putExtra("media", myJson);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(App.getContext());
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(App.getContext(), Constants.NOTIFICATION.CHANNEL_ID);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("New Media")
                .setTicker("New Media")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentText(title)
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .setSummaryText(title)
                        .setBigContentTitle("New Media"))
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);
        if(bitmap!=null){
            builder.setLargeIcon(bitmap);
        }else{
            builder.setLargeIcon(BitmapFactory.decodeResource(App.getContext().getResources(),
                    R.mipmap.ic_launcher));
        }

        int res_sound_id = App.getContext().getResources().getIdentifier("notification", "raw", App.getContext().getPackageName());
        Uri path = Uri.parse("android.resource://" + App.getContext().getPackageName() + "/" +res_sound_id);
        builder.setSound(path);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(App.getContext());
        notificationManager.notify(400, builder.build());
    }

    private void user_action_notifications(String title, Bitmap bitmap, String message){
        Log.e("title",title);
        Log.e("message",message);
        // Create an Intent for the activity you want to start
        Intent resultIntent  = new Intent(App.getContext(), SocialActivity.class);
        resultIntent.putExtra("notify", "true");
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(App.getContext());
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(App.getContext(), Constants.NOTIFICATION.CHANNEL_ID);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(App.getContext().getResources().getString(R.string.app_name))
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification)
                //.setLargeIcon(bitmap)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);
        if(bitmap!=null){
            builder.setLargeIcon(bitmap);
        }else{
            builder.setLargeIcon(BitmapFactory.decodeResource(App.getContext().getResources(),
                    R.drawable.avatar));
        }

        int res_sound_id = App.getContext().getResources().getIdentifier("notification", "raw", App.getContext().getPackageName());
        Uri path = Uri.parse("android.resource://" + App.getContext().getPackageName() + "/" +res_sound_id);
        builder.setSound(path);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(App.getContext());
        notificationManager.notify(102, builder.build());
    }


    private void notify_user(Inbox inbox){
        // Create an Intent for the activity you want to start

    }
}