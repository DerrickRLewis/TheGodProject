package apps.envision.mychurch.libs.bible_download;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import java.util.Locale;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.pojo.BibleDownload;
import apps.envision.mychurch.ui.activities.DownloadBibleActivity;

/**
 * class notify user of download progress
 */
public class DownloadBibleNotification {

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;

    public DownloadBibleNotification(){
        notificationManager = (NotificationManager) App.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void createNotificationManager(){
        // Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(App.getContext(), DownloadBibleActivity.class);
       // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(App.getContext());
        stackBuilder.addNextIntentWithParentStack(resultIntent);
      // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder = new NotificationCompat.Builder(App.getContext(), App.getContext().getString(R.string.app_name));
        notificationBuilder.setContentIntent(resultPendingIntent);
    }

    public void init_notifications(BibleDownload download){
        createNotificationManager();
        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
        notificationBuilder.setContentTitle("Pending")
                .setContentText("Download "+download.getTitle())
                .setAutoCancel(true);
        notificationManager.notify(download.getNotify_id(), notificationBuilder.build());
    }

    public void processing_downloaded_bible(BibleDownload download){
        createNotificationManager();
        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
        notificationBuilder.setContentTitle(App.getContext().getResources().getString(R.string.just_a_second))
                .setContentText("Processing "+download.getTitle())
                .setAutoCancel(true);
        notificationManager.notify(download.getNotify_id(), notificationBuilder.build());
    }

    public void sendNotification(BibleDownload download){
        createNotificationManager();
        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
        notificationBuilder.setColor(App.getContext().getResources().getColor( R.color.colorAccent));
        notificationBuilder.setProgress(100,download.getProgress(),false);
        notificationBuilder.setContentText(String.format(Locale.getDefault(),App.getContext().getResources().getString(R.string.downloaded_progress),download.getProgress(),100) + " of " + download.getTitle());
        notificationManager.notify(download.getNotify_id(), notificationBuilder.build());
    }

    public void onDownloadCompleteNotification(BibleDownload download){
        createNotificationManager();
        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download_done);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setProgress(0,0,false);
        notificationBuilder.setContentText(download.getTitle()+" Downloaded");
        notificationManager.notify(download.getNotify_id(), notificationBuilder.build());

    }

    public void onDownloadErrorNotification(BibleDownload download){
        createNotificationManager();
        notificationBuilder.setSmallIcon(R.drawable.ic_error_outline_black_24dp);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setProgress(0,0,false);
        notificationBuilder.setContentTitle("Download Failed.");
        notificationBuilder.setContentText("Failed to download "+download.getTitle());
        notificationManager.notify(download.getNotify_id(), notificationBuilder.build());

    }

    public void stopNofication(BibleDownload download){
        notificationManager.cancel(download.getNotify_id());
    }
}
