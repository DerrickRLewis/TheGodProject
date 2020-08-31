package apps.envision.mychurch.libs.bible_download;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.Bible;
import apps.envision.mychurch.pojo.BibleDownload;
import apps.envision.mychurch.utils.Constants;
import apps.envision.mychurch.utils.FileManager;

/**
 * Created by ray on 12/12/2018.
 * worker class to handle download tasks
 */

public class BibleDownloadWorker extends Worker {

    private DataInterfaceDao dataInterfaceDao;
    private ExecutorService executorService;

    public static final String DOWNLOAD_ARG = "bible_download_arg";
    private Downloader downloader;
    private CountDownLatch latch;
    private DownloadBibleNotification downloadNotification;
    private BibleDownload bibleDownload;
    private boolean isCancelled = false;

    public BibleDownloadWorker(
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

            bibleDownload = PreferenceSettings.getCurrentBibleDownload(getInputData().getString(DOWNLOAD_ARG));

            //init notification class
            downloadNotification = new DownloadBibleNotification();
            //start the download process
            downloadNotification.init_notifications(bibleDownload);
            PreferenceSettings.setBibleDownloadInProgress(true);
            startDownload();
            //wait indefinitely till download is successfull, an error is encountered
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Result.success();
    }

    @Override
    public void onStopped() {
        //cancel download
        isCancelled = true;
        onDownloadError();
        super.onStopped();
    }


    public void startDownload() {
        Log.e("bible path",bibleDownload.getDownload_path());
        downloader = new Downloader.Builder(App.getContext(), bibleDownload.getDownload_path())
                .downloadDirectory(FileManager.getBibleDestinationFolder())
                .fileName(bibleDownload.getBook(),"json")
                .downloadListener(new OnDownloadListener(){
                    @Override
                    public void onStart() {
                        Log.e("download started","Yesooo");
                        PreferenceSettings.setDownloadInProgress(true);
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
                        bibleDownload.setProgress(progress);
                        bibleDownload.setOngoing(true);
                        Log.e("progress",String.valueOf(progress));
                        PreferenceSettings.setCurrentBibleDownload(bibleDownload.getBook(),bibleDownload);
                        //send progress report
                        downloadNotification.sendNotification(bibleDownload);
                        LocalMessageManager.getInstance().send(R.id.bible_download_progress,bibleDownload.getBook());
                    }

                    @Override
                    public void onCompleted(@Nullable File file) {
                        Log.e("download completed","Yesooo");
                        downloadNotification.processing_downloaded_bible(bibleDownload);
                        bibleDownload.setFile_path(file.getPath());
                        bibleDownload.setCompleted(true);
                        bibleDownload.setProcessing(true);
                        PreferenceSettings.setCurrentBibleDownload(bibleDownload.getBook(),bibleDownload);
                        LocalMessageManager.getInstance().send(R.id.bible_download_progress,bibleDownload.getBook());
                        process_downloaded_bible(bibleDownload);
                    }

                    @Override
                    public void onFailure(@Nullable String s) {
                        Log.e("download failed",s);
                        onDownloadError();
                    }

                    @Override
                    public void onCancel() {
                        PreferenceSettings.setBibleDownloadInProgress(false);
                        downloadNotification.stopNofication(bibleDownload);
                        latch.countDown();
                    }
                }).build();
        downloader.download();
    }

    private void process_downloaded_bible(BibleDownload bibleDownload){
        Log.e("file path",bibleDownload.getFile_path());
        executorService.execute(() -> {
            try {
                File file = new File(bibleDownload.getFile_path());
                JsonReader reader = new JsonReader(new InputStreamReader(FileUtils.openInputStream(file), StandardCharsets.UTF_8));
                Gson gson = new GsonBuilder()
                        .setLenient()
                        .create();
                // Read file in stream mode
                reader.beginArray();
                List<Bible> bibleList = new ArrayList<>();
                List<Bible> _bibleList = dataInterfaceDao.getAllBible();
                while (reader.hasNext()) {
                    // Read data into object model
                    Bible bible = gson.fromJson(reader, Bible.class);
                   if(_bibleList.size()>0){
                       Bible bible_ = checkIfBibleExists(_bibleList,bible.getId());//dataInterfaceDao.getBible(bible.getBook(),bible.getChapter(),bible.getVerse());
                       if(bible_!=null) {
                           if (!bible_.getAMP().equalsIgnoreCase("")) {
                               bible.setAMP(bible_.getAMP());
                           }
                           if (!bible_.getKJV().equalsIgnoreCase("")) {
                               bible.setKJV(bible_.getKJV());
                           }
                           if (!bible_.getNLT().equalsIgnoreCase("")) {
                               bible.setNLT(bible_.getNLT());
                           }
                           if (!bible_.getNIV().equalsIgnoreCase("")) {
                               bible.setNIV(bible_.getNIV());
                           }
                           if (!bible_.getMSG().equalsIgnoreCase("")) {
                               bible.setMSG(bible_.getMSG());
                           }
                           if (!bible_.getNRSV().equalsIgnoreCase("")) {
                               bible.setNRSV(bible_.getNRSV());
                           }
                           if (!bible_.getNKJV().equalsIgnoreCase("")) {
                               bible.setNKJV(bible_.getNKJV());
                           }
                       }
                   }
                    bibleList.add(bible);
                }
                dataInterfaceDao.insertAllBible(bibleList);
                reader.close();
                setDownloadComplete();
            } catch (UnsupportedEncodingException ex) {
                Log.e("error", ex.getLocalizedMessage());
            } catch (IOException ex) {
                Log.e("error", ex.getLocalizedMessage());
            }
        });

    }

    private void onDownloadError(){
        PreferenceSettings.setBibleDownloadInProgress(false);
        bibleDownload.setOngoing(false);
        PreferenceSettings.setCurrentBibleDownload(bibleDownload.getBook(),bibleDownload);
        downloadNotification.onDownloadErrorNotification(bibleDownload);
        LocalMessageManager.getInstance().send(R.id.bible_download_progress,bibleDownload.getBook());
        if(downloader!=null){
            downloader.cancelDownload();
        }
        latch.countDown();
    }

    private void setDownloadComplete(){
        PreferenceSettings.setBibleDownloadInProgress(false);
        bibleDownload.setOngoing(false);
        bibleDownload.setProgress(100);
        bibleDownload.setCompleted(true);
        bibleDownload.setProcessing(false);
        PreferenceSettings.setCurrentBibleDownload(bibleDownload.getBook(),bibleDownload);
        downloadNotification.onDownloadCompleteNotification(bibleDownload);
        LocalMessageManager.getInstance().send(R.id.bible_download_complete,bibleDownload.getBook());
        latch.countDown();
    }

    private Bible checkIfBibleExists(List<Bible> bibleList, long pos){
        int itm = (int)pos-1;
        if(bibleList.size() == 0)return null;
        if(bibleList.size() < itm)return null;
        if(bibleList.get(itm)!=null){
            return bibleList.get(itm);
        }
        return null;
    }
}
