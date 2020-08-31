package apps.envision.mychurch.pojo;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "devotionals_table")
public class Devotionals {

    @PrimaryKey
    @ColumnInfo(name = "id")
    private long id;

    @NonNull
    @ColumnInfo(name = "thumbnail")
    private String thumbnail = "";

    @NonNull
    @ColumnInfo(name = "title")
    private String title = "";

    @NonNull
    @ColumnInfo(name = "content")
    private String content = "";

    @NonNull
    @ColumnInfo(name = "bible_reading")
    private String bible_reading = "";

    @NonNull
    @ColumnInfo(name = "confession")
    private String confession = "";

    @NonNull
    @ColumnInfo(name = "studies")
    private String studies = "";

    @NonNull
    @ColumnInfo(name = "author")
    private String author = "";

    @NonNull
    @ColumnInfo(name = "date")
    private String date = "";




    public Devotionals() {

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

    @NonNull
    public String getContent() {
        return content;
    }

    public void setContent(@NonNull String content) {
        this.content = content;
    }

    @NonNull
    public String getBible_reading() {
        return bible_reading;
    }

    public void setBible_reading(@NonNull String bible_reading) {
        this.bible_reading = bible_reading;
    }

    @NonNull
    public String getConfession() {
        return confession;
    }

    public void setConfession(@NonNull String confession) {
        this.confession = confession;
    }

    @NonNull
    public String getStudies() {
        return studies;
    }

    public void setStudies(@NonNull String studies) {
        this.studies = studies;
    }

    @NonNull
    public String getAuthor() {
        return author;
    }

    public void setAuthor(@NonNull String author) {
        this.author = author;
    }

    @NonNull
    public String getDate() {
        return date;
    }

    public void setDate(@NonNull String date) {
        this.date = date;
    }

    @NonNull
    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(@NonNull String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
