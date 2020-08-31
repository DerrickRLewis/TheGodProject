package apps.envision.mychurch.pojo;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "playlist_table")
public class Playlist {

    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id=0;

    @NonNull
    @ColumnInfo(name = "title")
    private String title="";

    @NonNull
    @ColumnInfo(name = "media_type")
    private String media_type="";

    public Playlist() {

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
    public String getMedia_type() {
        return media_type;
    }

    public void setMedia_type(@NonNull String media_type) {
        this.media_type = media_type;
    }
}
