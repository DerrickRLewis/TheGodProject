package apps.envision.mychurch.libs.bible_download;

/**
 * Created by raypower on 7/15/2017.
 */

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import apps.envision.mychurch.interfaces.BibleDownloadStatusListener;
import apps.envision.mychurch.interfaces.DownloadStatusListener;
import apps.envision.mychurch.pojo.BibleDownload;
import apps.envision.mychurch.pojo.Download;
import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Background Async Task to write  file download progress
 * */
public class BibleWriteResponse extends AsyncTask<Void, String, String> {

    private BibleDownloadStatusListener downloadListener;
    private ResponseBody body;
    private BibleDownload downloads;
    private Headers headers;

    /**
     * required class constructor
     * @param downloadListener, body, songs
     */
    public BibleWriteResponse(BibleDownloadStatusListener downloadListener, Response<ResponseBody> response, BibleDownload downloads){
        this.downloadListener = downloadListener;
        this.body = response.body();
        this.downloads = downloads;
        this.headers = response.headers();
    }


    @Override
    protected String doInBackground(Void... params) {
        int count;
        byte data[] = new byte[1024 * 4];
        long fileSize = body.contentLength();
        int totalFileSize = (int)Math.ceil(fileSize / (Math.pow(1024, 2)));
        downloads.setTotal_file_size(totalFileSize);
        downloads.setLastModified(headers.get("Last-Modified"));
        InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
        OutputStream output = null;
        try {
            long total = 0;
            output = new FileOutputStream(downloads.getDownload_path());
            int previousProgress = 0;
            while ((count = bis.read(data)) != -1 && downloads.getProgress() < 100) {
                total += count;
                int progress = (int) ((total * 100) / fileSize);
                //
                int current = (int) Math.round(total / (Math.pow(1024, 2)));
                downloads.setProgress(progress);
                downloads.setCurrent_file_size(current);
                //Log.d("progress",String.valueOf(downloads.getProgress()) + "%");
                //Log.d("info",String.format("Downloaded %d of %d MB",downloads.getCurrent_file_size(),downloads.getTotal_file_size()));
                output.write(data, 0, count);
                if (progress > previousProgress) {
                    downloadListener.progress(downloads);
                    // Only post progress event if we've made progress.
                    previousProgress = progress;
                }
            }
            output.flush();
            output.close();
            bis.close();

        } catch (FileNotFoundException e) {
            //set download error
            downloadListener.downloadError(downloads);
            e.printStackTrace();
        } catch (IOException e) {
            //set download error
            downloadListener.downloadError(downloads);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Updating progress bar
     * @param progress
     */
    protected void onProgressUpdate(String... progress) {
        downloadListener.progress(downloads);
    }

    @Override
    protected void onPostExecute(String params) {
        downloadListener.progress(downloads);
    }

}