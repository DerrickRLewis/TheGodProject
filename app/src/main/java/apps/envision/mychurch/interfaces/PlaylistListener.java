package apps.envision.mychurch.interfaces;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import apps.envision.mychurch.pojo.Playlist;

public interface PlaylistListener {
    void new_playlist();
    boolean isMediaAddedTOPlaylist(Playlist playlist);
    void playListOnClick(Playlist playlist);
    void setThumbnail(ImageView imageView, Playlist playlist);
    void setPlaylistSize(TextView textView, Playlist playlist);
    boolean showOptions();
    void onOptionsClick(Playlist playlist, View view);
}
