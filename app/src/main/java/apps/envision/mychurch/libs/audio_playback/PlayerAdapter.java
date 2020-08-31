package apps.envision.mychurch.libs.audio_playback;

import android.app.Activity;
import android.media.MediaPlayer;
import androidx.annotation.NonNull;

import java.util.List;

import apps.envision.mychurch.pojo.Media;

/**
 * Allows activities to control media playback of {@link MediaPlayerHolder}.
 */
public interface PlayerAdapter {

    void playSelectedSong(@NonNull final Media song);

    void release();

    void stop_player();

    boolean isMediaPlayer();

    boolean isPlaying();

    void resumeOrPause();

    void reset();
    boolean isReset();

    void instantReset();

    Media getCurrentMedia();

    String getNavigationArtist();

    void setNavigationArtist(@NonNull final String navigationArtist);

    void setCurrentSong(@NonNull final Media song);

    void setPlaylist(@NonNull final List<Media> songs);

    void skip(final boolean isNext);

    void openEqualizer(@NonNull final Activity activity);

    void seekTo(final int position);

    void setPlaybackInfoListener(final PlaybackInfoListener playbackInfoListener);

    @PlaybackInfoListener.State
    int getState();

    int getPlayerPosition();

    int getPlayerDuration();

    void registerNotificationActionsReceiver(final boolean isRegister);

    MediaPlayer getMediaPlayer();

    int getPlayingSongIndex();
}
