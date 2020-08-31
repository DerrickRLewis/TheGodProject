package apps.envision.mychurch.libs.downloads;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import alirezat775.lib.downloader.Downloader;
import alirezat775.lib.downloader.core.OnDownloadListener;
import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.AppDb;
import apps.envision.mychurch.db.DataInterfaceDao;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.interfaces.DownloadStatusListener;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.Download;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Created by ray on 12/12/2018.
 * worker class to handle download tasks
 */

public class DownloadWorker extends Worker implements DownloadStatusListener {

    private DataInterfaceDao dataInterfaceDao;
    private ExecutorService executorService;

    public static final String DOWNLOAD_ARG = "download_arg";
    private Call<ResponseBody> call;
    private CountDownLatch latch;
    private DownloadNotification downloadNotification;
    private Downloader downloader;

    public DownloadWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Need to wait for the download to finish or error to be encountered.
            latch = new CountDownLatch(1);

            AppDb db = AppDb.getDatabase(App.getContext());
            dataInterfaceDao = db.dataDbInterface();
            executorService = Executors.newSingleThreadExecutor();
            Gson gson = new Gson();
            Download downloads = gson.fromJson(getInputData().getString(DOWNLOAD_ARG), Download.class);

            //init notification class
            downloadNotification = new DownloadNotification();

            //start the download process
            init_download_process(downloads);

            //wait indefinitely till download is successfull, an error is encountered or user cancels or pauses the current download
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Result.success();
    }

    @Override
    public void onStopped() {
        //cancel download
        if(downloader!=null){
            downloader.cancelDownload();
        }
        latch.countDown();
        super.onStopped();
    }

    private void init_download_process(Download downloads){
        //start downloads with retrofit
        new CheckResumeDownload(downloads,DownloadWorker.this).execute();
        //show notification for pending download
        downloadNotification.init_notifications(downloads);
    }

    @Override
    public void startDownload(Download downloads, int status) {
        //first we check if file is already completely downloaded and exit
        //Log.e("download name",FilenameUtils.getBaseName(downloads.getDownload_path()));
        String ext = downloads.getMedia_type().equalsIgnoreCase(App.getContext().getString(R.string.audio))?".mp3":".mp4";
        if(downloads.getDownload_path().contains(".")) {
            ext = downloads.getDownload_path().substring(downloads.getDownload_path().lastIndexOf("."));
        }
        downloader = new Downloader.Builder(App.getContext(), downloads.getDownload_path())
                .downloadDirectory(downloads.getFile_path())
                .fileName(downloads.getTitle(),ext.replace(".",""))
                .downloadListener(new OnDownloadListener(){
                    @Override
                    public void onStart() {
                        Log.e("download started","Yesooo");
                        PreferenceSettings.setDownloadInProgress(true);
                        downloads.setStatus(1);
                        LocalMessageManager.getInstance().send(R.id.download_progess,downloads);
                    }

                    @Override
                    public void onPause() {

                    }

                    @Override
                    public void onResume() {

                    }

                    @Override
                    public void onProgressUpdate(int progress, int downloadedSize, int file_size) {
                        Log.e("download progress",String.valueOf(progress));
                        downloads.setProgress(progress);
                        downloads.setCurrent_file_size((int) Math.round(downloadedSize / (Math.pow(1024, 2))));
                        downloads.setTotal_file_size((int)Math.ceil(file_size / (Math.pow(1024, 2))));
                        downloads.setStatus(2);
                        LocalMessageManager.getInstance().send(R.id.download_progess,downloads);
                        downloadNotification.sendNotification(downloads);
                    }

                    @Override
                    public void onCompleted(@Nullable File file) {
                        PreferenceSettings.setDownloadInProgress(false);
                        downloads.setStatus(3);
                        downloads.setProgress(100);
                        LocalMessageManager.getInstance().send(R.id.download_progess,downloads);
                        downloadNotification.onDownloadCompleteNotification(downloads);
                        LocalMessageManager.getInstance().send(R.id.reload_downloads,downloads.getMedia_type());

                        latch.countDown();
                    }

                    @Override
                    public void onFailure(@Nullable String s) {
                        Log.e("download started",s);
                        PreferenceSettings.setDownloadInProgress(false);
                        downloads.setStatus(4);
                        LocalMessageManager.getInstance().send(R.id.download_progess,downloads);
                        downloadNotification.onDownloadErrorNotification(downloads);
                        latch.countDown();
                    }

                    @Override
                    public void onCancel() {
                        PreferenceSettings.setDownloadInProgress(false);
                        latch.countDown();
                        downloadNotification.stopNofication();
                    }
                }).build();
        downloader.download();

    }

    /**
     * get download progress and notify user
     * @param downloads
     */
    @Override
    public void progress(Download downloads) {
        //Log.e("status",String.valueOf(downloads.getStatus()));
        //Log.e("progress",String.valueOf(downloads.getProgress()) + "%");
        if(call!=null && downloads.getProgress()<100) {
            downloads.setStatus(1);
            downloadNotification.sendNotification(downloads);
            //send notification progress
            LocalMessageManager.getInstance().send(R.id.download_progess,downloads);
        }else if(downloads.getProgress() == 100){
            downloads.setProgress(100);
            downloads.setStatus(3);
            downloadNotification.onDownloadCompleteNotification(downloads);
            executorService.execute(() -> dataInterfaceDao.updateCurrentDownload(downloads));

            //update flag for download in progress to false
            PreferenceSettings.setDownloadInProgress(false);

            //move downloaded file from temp directory to apps storage folder
            File tmpFile = new File(downloads.getTemp_dir());
            tmpFile.renameTo(new File(downloads.getFile_path()));

            //notify downloads fragment to reload its view
            LocalMessageManager.getInstance().send(R.id.reload_downloads,downloads.getMedia_type());

            //
            latch.countDown();
        }
    }

    @Override
    public void downloadError(Download downloads) {
        //if this download error was not caused because user paused or cancelled current download
        if(!PreferenceSettings.getIsUserInitiatedProcess()) {
            Log.e("download error","Caused by network");
            downloads.setStatus(4);
            //set notification bar to download error
            downloadNotification.onDownloadErrorNotification(downloads);
            executorService.execute(() -> dataInterfaceDao.updateCurrentDownload(downloads));
            //update flag for download in progress to false
            PreferenceSettings.setDownloadInProgress(false);
        }else{
            Log.e("download error","User cancelled or paused current download");
            PreferenceSettings.setIsUserInitiatedProcess(false);
            //clear notification
            downloadNotification.stopNofication();
        }

        //
        latch.countDown();
    }
}
