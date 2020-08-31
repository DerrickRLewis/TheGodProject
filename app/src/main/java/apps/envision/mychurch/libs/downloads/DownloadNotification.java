package apps.envision.mychurch.libs.downloads;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import java.util.Locale;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.pojo.Download;
import apps.envision.mychurch.ui.activities.DownloadsActivity;

/**
 * class notify user of download progress
 */
public class DownloadNotification {

    private int notify_id = 1000;

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;

    public DownloadNotification(){
        notificationManager = (NotificationManager) App.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void createNotificationManager(){
        // Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(App.getContext(), DownloadsActivity.class);
       // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(App.getContext());
        stackBuilder.addNextIntentWithParentStack(resultIntent);
      // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder = new NotificationCompat.Builder(App.getContext(), App.getContext().getString(R.string.app_name));
        notificationBuilder.setContentIntent(resultPendingIntent);
    }

    public void init_notifications(Download download){
        createNotificationManager();
        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
        notificationBuilder.setContentTitle("Pending")
                .setContentText("Download "+download.getTitle())
                .setAutoCancel(true);
        notificationManager.notify(notify_id, notificationBuilder.build());
    }

    public void sendNotification(Download download){
        createNotificationManager();
        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
        notificationBuilder.setColor(App.getContext().getResources().getColor( R.color.colorAccent));
        notificationBuilder.setProgress(100,download.getProgress(),false);
        notificationBuilder.setContentText(String.format(Locale.getDefault(),"Downloaded (%d/%d) MB",download.getCurrent_file_size(),download.getTotal_file_size()) + " of " + download.getTitle());
        notificationManager.notify(notify_id, notificationBuilder.build());
    }

    public void onDownloadCompleteNotification(Download download){
        createNotificationManager();
        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download_done);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setProgress(0,0,false);
        notificationBuilder.setContentText(download.getTitle()+" Downloaded");
        notificationManager.notify(notify_id, notificationBuilder.build());

    }

    public void onDownloadErrorNotification(Download download){
        createNotificationManager();
        notificationBuilder.setSmallIcon(R.drawable.ic_error_outline_black_24dp);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setProgress(0,0,false);
        notificationBuilder.setContentTitle("Download Failed.");
        notificationBuilder.setContentText("Failed to download "+download.getTitle());
        notificationManager.notify(notify_id, notificationBuilder.build());

    }

    public void stopNofication(){
        notificationManager.cancel(notify_id);
    }
}
