package apps.envision.mychurch.pojo;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "radio_table")
public class Radio {

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
    @ColumnInfo(name = "stream_url")
    private String stream_url = "";


    public Radio() {

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
    public String getCover_photo() {
        return cover_photo;
    }

    public void setCover_photo(@NonNull String cover_photo) {
        this.cover_photo = cover_photo;
    }

    @NonNull
    public String getStream_url() {
        return stream_url;
    }

    public void setStream_url(@NonNull String stream_url) {
        this.stream_url = stream_url;
    }
}
