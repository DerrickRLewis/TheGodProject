package apps.envision.mychurch.libs.downloads;

import android.os.AsyncTask;

import apps.envision.mychurch.interfaces.DownloadStatusListener;
import apps.envision.mychurch.pojo.Download;

/**
 * send a request to server & decide to start a new download or not
 * return 0 if to start a new download
 * return 1 to resume a download
 * return 2 if file already exists and its completed
 *
 */
public class CheckResumeDownload extends AsyncTask<Void, Void, Integer> {

    private Download downloads;
    private DownloadStatusListener downloadsListener;

    public CheckResumeDownload(Download downloads, DownloadStatusListener downloadsListener){
        this.downloads = downloads;
        this.downloadsListener = downloadsListener;
    }

    protected Integer doInBackground(Void... arg0){
         /*  URL url = null;
        try {
            url = new URL(downloads.getDownload_path());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // Open connection to URL.
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setReadTimeout(3000);
            conn.setConnectTimeout(3000);
            File downloadedFile = new File(downloads.getFile_path());
            File downloadedTempFile = new File(downloads.getTemp_dir());
            int fileLength = conn.getContentLength();
           // Log.d("file exists",String.valueOf(downloadedFile.exists()));
           // Log.d("remoteLastModified",remoteLastModified);
           // Log.d("LastModified","downloads : "+downloads.getLastModified());
           // Log.d("file length",String.valueOf(downloadedFile.length()));
           // Log.d("remote file exists",String.valueOf(fileLength));
            int status = 0;
            if(downloadedFile.exists() && downloadedFile.length() < fileLength){
                status = 1;
            }else if(downloadedTempFile.exists()){//if the file exists in temp dir, send status to complete file download
                status = 1;
            }else if(downloadedFile.exists() && downloadedFile.length() >= fileLength){
                status = 2;
            }

            //Log.e("response",conn.getResponseMessage());
            conn.disconnect();
            return status;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            downloadsListener.downloadError(downloads);
        } catch (IOException e) {
            e.printStackTrace();
            downloadsListener.downloadError(downloads);
        }*/
        return 0;
    }

    protected void onPostExecute(Integer result) {
        //Log.d("resume download",String.valueOf(result));
        downloadsListener.startDownload(downloads,result);
    }
}
