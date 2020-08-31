package apps.envision.mychurch.pojo;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "current_downloads_table")
public class Download {

    @PrimaryKey
    @ColumnInfo(name = "id")
    private long id;

    @NonNull
    @ColumnInfo(name = "title")
    private String title = "";

    @NonNull
    @ColumnInfo(name = "cover_photo")
    private String cover_photo = "";

    @NonNull
    @ColumnInfo(name = "media_type")
    private String media_type = "";

    @NonNull
    @ColumnInfo(name = "download_path")
    private String download_path = "";

    @NonNull
    @ColumnInfo(name = "file_path")
    private String file_path;

    @NonNull
    @ColumnInfo(name = "temp_dir")
    private String temp_dir;

    @NonNull
    @ColumnInfo(name = "progress")
    private int progress = 0;

    @NonNull
    @ColumnInfo(name = "current_file_size")
    private int current_file_size = 0;

    @NonNull
    @ColumnInfo(name = "lastModified")
    private String lastModified = "";

    @NonNull
    @ColumnInfo(name = "total_file_size")
    private int total_file_size = 0;

    @NonNull
    @ColumnInfo(name = "notify_id")
    private int notify_id = 0;

    @NonNull
    @ColumnInfo(name = "status")
    private int status = 0;//0 for pending, 1 for downloading, 2 for PAUSE, 3 for completed, 4 for error

    @NonNull
    @ColumnInfo(name = "addTimeStamp")
    private Long addTimeStamp = System.currentTimeMillis();

    public Download() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @NonNull
    public String getCover_photo() {
        return cover_photo;
    }

    public void setCover_photo(@NonNull String cover_photo) {
        this.cover_photo = cover_photo;
    }

    @NonNull
    public String getMedia_type() {
        return media_type;
    }

    public void setMedia_type(@NonNull String media_type) {
        this.media_type = media_type;
    }

    @NonNull
    public Long getAddTimeStamp() {
        return addTimeStamp;
    }

    public void setAddTimeStamp(@NonNull Long addTimeStamp) {
        this.addTimeStamp = addTimeStamp;
    }

    public int getNotify_id() {
        return notify_id;
    }

    public void setNotify_id(int notify_id) {
        this.notify_id = notify_id;
    }

    @NonNull
    public int getCurrent_file_size() {
        return current_file_size;
    }

    public void setCurrent_file_size(@NonNull int current_file_size) {
        this.current_file_size = current_file_size;
    }

    @NonNull
    public int getTotal_file_size() {
        return total_file_size;
    }

    public void setTotal_file_size(@NonNull int total_file_size) {
        this.total_file_size = total_file_size;
    }

    @NonNull
    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(@NonNull String lastModified) {
        this.lastModified = lastModified;
    }

    @NonNull
    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(@NonNull String file_path) {
        this.file_path = file_path;
    }

    @NonNull
    public String getTemp_dir() {
        return temp_dir;
    }

    public void setTemp_dir(@NonNull String temp_dir) {
        this.temp_dir = temp_dir;
    }

    @NonNull
    public String getDownload_path() {
        return download_path;
    }

    public void setDownload_path(@NonNull String download_path) {
        this.download_path = download_path;
    }
}
