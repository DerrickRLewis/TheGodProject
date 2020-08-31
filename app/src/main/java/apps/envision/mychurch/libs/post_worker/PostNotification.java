package apps.envision.mychurch.libs.post_worker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.socials.NewPostActivity;
import apps.envision.mychurch.socials.SocialActivity;

/**
 * class notify user of download progress
 */
public class PostNotification {

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private int notificationID = 9092;
    private static final String RETRY_ACTION = App.getContext().getPackageName()+".RETRY";

    public PostNotification(){
        //Random r = new Random();
        //notificationID = r.nextInt(80 - 65) + 65;
        notificationManager = (NotificationManager) App.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void createNotificationManager(boolean isSuccess){
        // Create an Intent for the activity you want to start
        Intent resultIntent;
        if(isSuccess){
            resultIntent = new Intent(App.getContext(), SocialActivity.class);
        }else{
            resultIntent = new Intent(App.getContext(), NewPostActivity.class);
        }
       // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(App.getContext());
        stackBuilder.addNextIntentWithParentStack(resultIntent);
      // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder = new NotificationCompat.Builder(App.getContext(), App.getContext().getString(R.string.app_name));
        notificationBuilder.setContentIntent(resultPendingIntent);
    }


    public void init_notifications(){
        createNotificationManager(false);
        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_upload);
        notificationBuilder.setContentTitle("Processing")
                .setContentText("Processing post")
                .setAutoCancel(true);
        notificationManager.notify(notificationID, notificationBuilder.build());
    }

    public void sendNotification(){
        createNotificationManager(false);
        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_upload);
        notificationBuilder.setColor(App.getContext().getResources().getColor( R.color.colorAccent));
        notificationBuilder.setProgress(100,0,true);
        notificationBuilder.setContentText(App.getContext().getString(R.string.processing_post_request_info));
        notificationManager.notify(notificationID, notificationBuilder.build());
    }

    public void onPostUploadCompleteNotification(){
        createNotificationManager(true);
        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download_done);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setProgress(0,0,false);
        notificationBuilder.setContentText(App.getContext().getString(R.string.successful_post_request_info));
        notificationManager.notify(notificationID, notificationBuilder.build());

    }

    public void onPostErrorNotification(String msg){
        createNotificationManager(false);
        notificationBuilder.setSmallIcon(R.drawable.ic_error_outline_black_24dp);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setProgress(0,0,false);
        notificationBuilder.setContentTitle(App.getContext().getString(R.string.post_request_failed));
        notificationBuilder.setContentText(msg);
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(msg));
        notificationManager.notify(notificationID, notificationBuilder.build());

    }

    public void stopNofication(){
        notificationManager.cancel(notificationID);
    }
}
