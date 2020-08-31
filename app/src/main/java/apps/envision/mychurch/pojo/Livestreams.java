package apps.envision.mychurch.pojo;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "livestreams_table")
public class Livestreams {

    @PrimaryKey
    @ColumnInfo(name = "id")
    private long id;

    @NonNull
    @ColumnInfo(name = "title")
    private String title = "";

    @NonNull
    @ColumnInfo(name = "type")
    private String type = "";


    @NonNull
    @ColumnInfo(name = "stream_url")
    private String stream_url = "";

    @NonNull
    @ColumnInfo(name = "status")
    private boolean status = false;

    @NonNull
    @ColumnInfo(name = "is_free")
    private boolean is_free = false;



    public Livestreams() {

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
    public String getStream_url() {
        return stream_url;
    }

    public void setStream_url(@NonNull String stream_url) {
        this.stream_url = stream_url;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean isIs_free() {
        return is_free;
    }

    public void setIs_free(boolean is_free) {
        this.is_free = is_free;
    }
}
