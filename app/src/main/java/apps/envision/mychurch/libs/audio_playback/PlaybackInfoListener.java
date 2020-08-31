package apps.envision.mychurch.libs.audio_playback;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Allows {@link MediaPlayerHolder} to report media playback duration and progress updates to
 * the activities.
 */
public abstract class PlaybackInfoListener {

    public void onPositionChanged(final int position) {
    }

    public void onStateChanged(final @State int state) {
    }

    public void onPlaybackCompleted() {
    }

    public void onMusicStopped() {
    }

    @IntDef({State.INVALID, State.PLAYING, State.PAUSED, State.COMPLETED, State.RESUMED, State.LOADING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {

        int INVALID = -1;
        int PLAYING = 0;
        int PAUSED = 1;
        int COMPLETED = 2;
        int RESUMED = 3;
        int LOADING = 4;
    }

    public void onSongSelected() {
    }
}