package apps.envision.mychurch.pojo;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "slider_table")
public class Slider {

    @PrimaryKey
    @ColumnInfo(name = "id")
    private long id;

    @NonNull
    @ColumnInfo(name = "category")
    private String category = "";

    @NonNull
    @ColumnInfo(name = "title")
    private String title = "";

    @NonNull
    @ColumnInfo(name = "cover_photo")
    private String cover_photo = "";

    @NonNull
    @ColumnInfo(name = "description")
    private String description = "";

    @NonNull
    @ColumnInfo(name = "ad_preview_hour")
    private int ad_preview_hour = 0;

    @NonNull
    @ColumnInfo(name = "download_url")
    private String download_url = "";

    @NonNull
    @ColumnInfo(name = "stream_url")
    private String stream_url = "";

    @NonNull
    @ColumnInfo(name = "duration")
    private long duration = 0;

    @NonNull
    @ColumnInfo(name = "can_preview")
    private boolean can_preview = false;

    @NonNull
    @ColumnInfo(name = "can_download")
    private boolean can_download = false;

    @NonNull
    @ColumnInfo(name = "is_free")
    private boolean is_free = false;

    @NonNull
    @ColumnInfo(name = "preview_duration")
    private long preview_duration = 0;

    @NonNull
    @ColumnInfo(name = "media_type")
    private String media_type = "";

    @NonNull
    @ColumnInfo(name = "video_type")
    private String video_type = "";

    @NonNull
    @ColumnInfo(name = "comments_count")
    private long comments_count = 0;

    @NonNull
    @ColumnInfo(name = "likes_count")
    private long likes_count = 0;

    @NonNull
    @ColumnInfo(name = "views_count")
    private long views_count = 0;

    @NonNull
    @ColumnInfo(name = "user_liked")
    private boolean userLiked = false;

    @NonNull
    @ColumnInfo(name = "http")
    private boolean http = true;

    public Slider() {

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
    public String getDescription() {
        return description;
    }

    public void setDescription(@NonNull String description) {
        this.description = description;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isHttp() {
        return http;
    }

    public void setHttp(boolean http) {
        this.http = http;
    }

    @NonNull
    public boolean isCan_preview() {
        return can_preview;
    }

    public void setCan_preview(@NonNull boolean can_preview) {
        this.can_preview = can_preview;
    }

    @NonNull
    public boolean isCan_download() {
        return can_download;
    }

    public void setCan_download(@NonNull boolean can_download) {
        this.can_download = can_download;
    }

    @NonNull
    public boolean isIs_free() {
        return is_free;
    }

    public void setIs_free(@NonNull boolean is_free) {
        this.is_free = is_free;
    }

    @NonNull
    public long getPreview_duration() {
        return preview_duration;
    }

    public void setPreview_duration(@NonNull long preview_duration) {
        this.preview_duration = preview_duration;
    }

    @NonNull
    public String getMedia_type() {
        return media_type;
    }

    public void setMedia_type(@NonNull String media_type) {
        this.media_type = media_type;
    }

    @NonNull
    public void setDownload_url(String download_url) {
         this.download_url = download_url;
    }

    @NonNull
    public String getDownload_url() {
        return download_url;
    }

    @NonNull
    public String getStream_url() {
        return stream_url;
    }

    public void setStream_url(@NonNull String stream_url) {
        this.stream_url = stream_url;
    }

    public long getComments_count() {
        return comments_count;
    }

    public void setComments_count(long comments_count) {
        this.comments_count = comments_count;
    }

    public long getLikes_count() {
        return likes_count;
    }

    public void setLikes_count(long likes_count) {
        this.likes_count = likes_count;
    }

    public boolean isUserLiked() {
        return userLiked;
    }

    public void setUserLiked(boolean userLiked) {
        this.userLiked = userLiked;
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    public void setCategory(@NonNull String category) {
        this.category = category;
    }

    public long getViews_count() {
        return views_count;
    }

    public void setViews_count(long views_count) {
        this.views_count = views_count;
    }

    @NonNull
    public String getVideo_type() {
        return video_type;
    }

    public void setVideo_type(@NonNull String video_type) {
        this.video_type = video_type;
    }

    public int getAd_preview_hour() {
        return ad_preview_hour;
    }

    public void setAd_preview_hour(int ad_preview_hour) {
        this.ad_preview_hour = ad_preview_hour;
    }
}
