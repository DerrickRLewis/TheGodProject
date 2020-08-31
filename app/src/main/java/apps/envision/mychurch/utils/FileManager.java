package apps.envision.mychurch.utils;

import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.pojo.Download;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.pojo.Uploads;

/*
* Class to help read media files from folder
* set media duration to textview
 */
public class FileManager {
    private static final DecimalFormat format = new DecimalFormat("#.##");
    private static final long MiB = 1024 * 1024;
    private static final long KiB = 1024;

    // Constructor
    public FileManager(){

    }

    /**
     * Function to read all mp4/mp3 files from our downloads folder
     * and store the details in ArrayList
     * */
    public ArrayList<Media> getDownloads(String media_type){
        Random random = new Random();
        File home = new File(getDestinationFolder(media_type));
        ArrayList<Media> downloads = new ArrayList<>();
        if(media_type.equalsIgnoreCase(App.getContext().getString(R.string.audio))) {
            if (home.listFiles(new MP3FileExtensionFilter()) != null && home.listFiles(new MP3FileExtensionFilter()).length > 0) {
                for (File file : home.listFiles(new MP3FileExtensionFilter())) {
                    long id = random.nextInt(900) + System.currentTimeMillis();
                    Media media = new Media();
                    media.setId(id);
                    String filename = file.getName().replace(".mp3", "");
                    filename = filename.substring(0, 1).toUpperCase() + filename.substring(1);
                    media.setTitle(filename);
                    media.setStream_url(file.getPath());
                    media.setDuration(0);
                    media.setHttp(false);
                    media.setMedia_type(App.getContext().getString(R.string.audio));
                    media.setCan_preview(true);
                    downloads.add(media);
                }
            }
        }else{
            if (home.listFiles(new MP4FileExtensionFilter()) != null && home.listFiles(new MP4FileExtensionFilter()).length > 0) {
                for (File file : home.listFiles(new MP4FileExtensionFilter())) {
                    long id = random.nextInt(900) + System.currentTimeMillis();
                    Media media = new Media();
                    media.setId(id);
                    String filename = file.getName().replace(".mp4", "");
                    filename = filename.substring(0, 1).toUpperCase() + filename.substring(1);
                    media.setTitle(filename);
                    media.setStream_url(file.getPath());
                    media.setDuration(0);
                    media.setMedia_type(App.getContext().getString(R.string.video));
                    media.setHttp(false);
                    media.setCan_preview(true);
                    downloads.add(media);
                }
            }
        }
        //we sort the downloads list in alphabetical order
        Collections.sort(downloads, (a, b) -> a.getTitle().compareTo(b.getTitle()));
        // return songs list array
        return downloads;
    }

    /**
     * Class to filter files audio files
     * */
    private class MP3FileExtensionFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".mp3")
                    || name.endsWith(".3gp")
                    || name.endsWith(".aac")
                    || name.endsWith(".ogg")
                    || name.endsWith(".wav")
                    || name.endsWith(".mid")
                    || name.endsWith(".flac")
                    || name.endsWith(".m4a")
            );
        }
    }

    /**
     * Class to filter files which are having .mp4 extension
     * */
    private class MP4FileExtensionFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".mp4")
                    || name.endsWith(".avi")
                    || name.endsWith(".mk4")
                    || name.endsWith(".3gp")
            );
        }
    }

    /**
     * method to get file save destination
     * */
    public static String getBibleDestinationFolder(){
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File myDir = new File(root +"/"+ App.getContext().getString(R.string.app_name) +"/"+ ".bible");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        return root +"/"+ App.getContext().getString(R.string.app_name) + "/" +".bible";
    }

    /**
     * method to get file save destination
     * */
    public static String getDestinationFolder(String media_type){
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File myDir = new File(root +"/"+ App.getContext().getString(R.string.app_name) +"/"+ media_type);
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        return root + "/" +App.getContext().getString(R.string.app_name) + "/"+ media_type;
    }

    /**
     * method to get temp file save destination
     * */
    public static String getTempDestinationFolder(){
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File myDir = new File(root +"/"+ App.getContext().getString(R.string.app_name) +"/"+ App.getContext().getString(R.string.temp_file_storage_dir));
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        return root + "/" +App.getContext().getString(R.string.app_name) + "/"+ App.getContext().getString(R.string.temp_file_storage_dir);
    }

    /**
     * method to get file save destination
     * */
    public static File getDestinationFile(Download downloads){
        return new File(getDestinationFolder(downloads.getMedia_type()), downloads.getTitle());
    }

    /**
     * method to get file save destination
     * */
    public static File getTempFileStorage(Download downloads){
        return new File(getTempDestinationFolder(), downloads.getTitle());
    }

    /**
     * static method to set file duration to a textview
     * @param file
     * @param view
     */
    public static void set_file_duration(String file, TextView view){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(file);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if(time!=null) {
                long timeInmillisec = Long.parseLong(time);
                long duration = timeInmillisec / 1000;
                long hours = duration / 3600;
                long minutes = (duration - hours * 3600) / 60;
                long seconds = duration - (hours * 3600 + minutes * 60);
                String hrs = hours == 0 ? "00" : String.valueOf(hours);
                String mins = minutes > 9 ? ":" + String.valueOf(minutes) : ":0" + String.valueOf(minutes);
                String secs = seconds > 9 ? ":" + String.valueOf(seconds) : ":0" + String.valueOf(seconds);
                String tim = hrs + mins + secs;
                view.setText(tim);
            }
        } catch (Exception ex) {
            Log.e("MediaMetadataRetriever", "MediaMetadataRetriever failed to get duration for " + file, ex);
        } finally {
            retriever.release();
        }

    }

    /**
     * static method to set file duration to a textview
     * @param time
     * @param view
     */
    public static void set_file_duration(long time, TextView view){
        long duration = time / 1000;
        long hours = duration / 3600;
        long minutes = (duration - hours * 3600) / 60;
        long seconds = duration - (hours * 3600 + minutes * 60);
        String hrs = hours == 0 ? "00" : String.valueOf(hours);
        String mins = minutes > 9 ? ":" + String.valueOf(minutes) : ":0" + String.valueOf(minutes);
        String secs = seconds > 9 ? ":" + String.valueOf(seconds) : ":0" + String.valueOf(seconds);
        String tim = hrs + mins + secs;
        view.setText(tim);
    }

    public static String getFileSize(double length) {
        if (length > MiB) {
            return format.format(length / MiB) + " MB";
        }
        if (length > KiB) {
            return format.format(length / KiB) + " KB";
        }
        return format.format(length) + " B";
    }

    public static String getFileSizeTotal(List<Uploads> uploads){
        //double total_length = 0;
        for(int i=0; i<uploads.size(); i++){
            double filesize = uploads.get(i).getLength()/MiB;
            if(filesize> Constants.Uploads.maximum_single_file_size){
                return App.getContext().getString(R.string.single_file_limit_info)+" "+Constants.Uploads.maximum_single_file_size+ "MB.";
            }/*else {
                total_length += uploads.get(i).getLength();
            }*/
        }
      /*  if (total_length > MiB) {
            double current_size = total_length / MiB;
            if(current_size>Constants.Uploads.maximum_total_file_size){
                response.add("20MB, current size is "+format.format(current_size) + " MB.");
                return response;
            }
        }*/
        return "";
    }
}
