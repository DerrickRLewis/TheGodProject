package apps.envision.mychurch.pojo;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity()
public class BibleDownload {

    private String title = "";

    private String book = "";

    private int current_page = 0;

    private int progress = 0;

    private int notify_id = 0;

    private String download_task_identifier = "";

    private UUID download_id = null;

    private boolean isOngoing = false;

    private boolean completed = false;

    private boolean processing = false;

    private String download_path = "";

    private String file_path;

    private int current_file_size = 0;

    private String lastModified = "";

    private int total_file_size = 0;

    public BibleDownload() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getNotify_id() {
        return notify_id;
    }

    public void setNotify_id(int notify_id) {
        this.notify_id = notify_id;
    }

    public String getBook() {
        return book;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public String getDownload_task_identifier() {
        return download_task_identifier;
    }

    public void setDownload_task_identifier(String download_task_identifier) {
        this.download_task_identifier = download_task_identifier;
    }

    public int getCurrent_page() {
        return current_page;
    }

    public void setCurrent_page(int current_page) {
        this.current_page = current_page;
    }

    public boolean isOngoing() {
        return isOngoing;
    }

    public void setOngoing(boolean ongoing) {
        isOngoing = ongoing;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public UUID getDownload_id() {
        return download_id;
    }

    public void setDownload_id(UUID download_id) {
        this.download_id = download_id;
    }

    public String getDownload_path() {
        return download_path;
    }

    public void setDownload_path(String download_path) {
        this.download_path = download_path;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public int getCurrent_file_size() {
        return current_file_size;
    }

    public void setCurrent_file_size(int current_file_size) {
        this.current_file_size = current_file_size;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public int getTotal_file_size() {
        return total_file_size;
    }

    public void setTotal_file_size(int total_file_size) {
        this.total_file_size = total_file_size;
    }

    public boolean isProcessing() {
        return processing;
    }

    public void setProcessing(boolean processing) {
        this.processing = processing;
    }
}
