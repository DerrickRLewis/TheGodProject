package apps.envision.mychurch.interfaces;


import apps.envision.mychurch.pojo.Media;

/**
 * Created by Ray on 6/25/2018.
 */

public interface MediaOptionsListener {
    boolean isBookmarked(Media media);
    void bookmark(Media media);
    void addToPlaylist(Media media);
    boolean isPlaylistActivity();
    boolean isDownloads();
}
