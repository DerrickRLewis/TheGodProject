package apps.envision.mychurch.libs.audio_playback;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import androidx.annotation.NonNull;

import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.utils.NetworkUtil;
import apps.envision.mychurch.utils.Utility;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Exposes the functionality of the {@link MediaPlayer} and implements the {@link PlayerAdapter}
 * so that  activities can control music playback.
 */
public final class MediaPlayerHolder implements PlayerAdapter, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    // The volume we set the media player to when we lose audio focus, but are
    // allowed to reduce the volume instead of stopping playback.
    private static final float VOLUME_DUCK = 0.2f;
    // The volume we set the media player when we have audio focus.
    private static final float VOLUME_NORMAL = 1.0f;
    // we don't have audio focus, and can't duck (play at a low volume)
    private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
    // we don't have focus, but can duck (play at a low volume)
    private static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
    // we have full audio focus
    private static final int AUDIO_FOCUSED = 2;
    private final Context mContext;
    private final AudioPlayerService mMusicService;
    private final AudioManager mAudioManager;
    private String mNavigationArtist;
    private MediaPlayer mMediaPlayer;
    private PlaybackInfoListener mPlaybackInfoListener;
    private Handler mHandler = new Handler();
    private boolean sReplaySong = false;
    private @PlaybackInfoListener.State
    int mState = PlaybackInfoListener.State.LOADING;
    private NotificationReceiver mNotificationActionsReceiver;
    private AudioNotificationManager mMusicNotificationManager;
    private int mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
    private boolean sPlayOnFocusGain;
    private final AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {

                @Override
                public void onAudioFocusChange(final int focusChange) {

                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_GAIN:
                            mCurrentAudioFocusState = AUDIO_FOCUSED;
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            // Audio focus was lost, but it's possible to duck (i.e.: play quietly)
                            mCurrentAudioFocusState = AUDIO_NO_FOCUS_CAN_DUCK;
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            // Lost audio focus, but will gain it back (shortly), so note whether
                            // playback should resume
                            mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
                            sPlayOnFocusGain = isMediaPlayer() && mState == PlaybackInfoListener.State.PLAYING || mState == PlaybackInfoListener.State.RESUMED;
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS:
                            // Lost audio focus, probably "permanently"
                            mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
                            break;
                    }

                    if (mMediaPlayer != null) {
                        // Update the player state based on the change
                        configurePlayerState();
                    }

                }
            };
    private Media mSelectedSong;
    private List<Media> mSongs;

    MediaPlayerHolder(@NonNull final AudioPlayerService musicService) {
        mMusicService = musicService;
        mContext = mMusicService.getApplicationContext();
        mAudioManager = (AudioManager) mContext.getSystemService(AUDIO_SERVICE);
    }

    private void registerActionsReceiver() {
        mNotificationActionsReceiver = new NotificationReceiver();
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(AudioNotificationManager.PREV_ACTION);
        intentFilter.addAction(AudioNotificationManager.PLAY_PAUSE_ACTION);
        intentFilter.addAction(AudioNotificationManager.NEXT_ACTION);
        intentFilter.addAction(AudioNotificationManager.STOP_ACTION);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

        mMusicService.registerReceiver(mNotificationActionsReceiver, intentFilter);
    }

    private void unregisterActionsReceiver() {
        if (mMusicService != null && mNotificationActionsReceiver != null) {
            try {
                //mMusicNotificationManager.stopNofication();
                mMusicService.unregisterReceiver(mNotificationActionsReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public final String getNavigationArtist() {
        return mNavigationArtist;
    }

    @Override
    public void setNavigationArtist(@NonNull final String navigationArtist) {
        mNavigationArtist = navigationArtist;
    }

    @Override
    public final Media getCurrentMedia() {
        return mSelectedSong;
    }

    @Override
    public void registerNotificationActionsReceiver(final boolean isReceiver) {

        if (isReceiver) {
            registerActionsReceiver();
        } else {
            unregisterActionsReceiver();
        }
    }

    @Override
    public void setCurrentSong(@NonNull final Media song) {
        mSelectedSong = song;
    }

    @Override
    public void setPlaylist(@NonNull final List<Media> songs) {
        mSongs = songs;
    }

    @Override
    public void onCompletion(@NonNull final MediaPlayer mediaPlayer) {
       /* mHandler.removeCallbacks(mUpdateTimeTask);
        if (mPlaybackInfoListener != null) {
            mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.COMPLETED);
            mPlaybackInfoListener.onPlaybackCompleted();
        }*/
        switch(PreferenceSettings.get_song_repeat_mode()){
            case 1:
                release();
                break;
            case 2:
                resetSong();
                break;
            case 3:
                skip(true);
                break;
        }
    }

    private void tryToGetAudioFocus() {

        final int result = mAudioManager.requestAudioFocus(
                mOnAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mCurrentAudioFocusState = AUDIO_FOCUSED;
        } else {
            mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
        }
    }

    private void giveUpAudioFocus() {
        if (mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener)
                == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
        }
    }

    public void setPlaybackInfoListener(@NonNull final PlaybackInfoListener listener) {
        mPlaybackInfoListener = listener;
    }

    private void setStatus(final @PlaybackInfoListener.State int state) {

        mState = state;
        if (mPlaybackInfoListener != null) {
            mPlaybackInfoListener.onStateChanged(state);
        }
        LocalMessageManager.getInstance().send(R.id.player_state_changed);
    }

    private void resumeMediaPlayer() {
        if (!isPlaying()) {
            mMediaPlayer.start();
            setStatus(PlaybackInfoListener.State.RESUMED);
            mMusicService.startForeground(AudioNotificationManager.NOTIFICATION_ID, mMusicNotificationManager.createNotification());
        }
    }

    private void pauseMediaPlayer() {
        setStatus(PlaybackInfoListener.State.PAUSED);
        mMediaPlayer.pause();
        mMusicService.stopForeground(false);
        mMusicNotificationManager.getNotificationManager().notify(AudioNotificationManager.NOTIFICATION_ID, mMusicNotificationManager.createNotification());
    }

    private void resetSong() {
        mMediaPlayer.seekTo(0);
        mMediaPlayer.start();
        setStatus(PlaybackInfoListener.State.PLAYING);
    }

    /**
     * Syncs the mMediaPlayer position with mPlaybackProgressCallback via recurring task.
     */
    private void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            updateProgressCallbackTask();
            mHandler.postDelayed(this, 100);
        }
    };

    // Reports media playback position to mPlaybackProgressCallback.
    private void stopUpdatingCallbackWithPosition() {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    private void updateProgressCallbackTask() {
        if (isMediaPlayer() && mMediaPlayer.isPlaying()) {
            int currentPosition = mMediaPlayer.getCurrentPosition();
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onPositionChanged(currentPosition);

                //we check if its valid to play this audio file
                //if user can preview, we terminate once we reach the duration specified for preview
                long duration = currentPosition/1000; //convert to seconds

                //if duration watched is upto 10seconds we update count for this media views
                if(duration>=10){
                    Utility.update_media_total_views(mSelectedSong);
                }
                //send event to update media progressbar
                LocalMessageManager.getInstance().send(R.id.audio_player_progress,currentPosition);

                if(Utility.mediaRequiresUserSubscription(mSelectedSong)){ //if user cant preview this audio without subscription, stop playback
                    pauseMediaPlayer(); //we pause so user can continue playing other audio in his playlist
                    show_subscribe_alert();
                }else if(Utility.isMediaPreviewDuration(mSelectedSong,duration)){
                    //if user can preview but requires subscription to play full media
                    //we stop player once playback reaches preview duration
                    pauseMediaPlayer(); //we pause so user can continue playing other audio in his playlist
                    show_subscribe_alert();
                }
            }
        }
    }

    //send alert to user to tell him why we paused audio playback
    private void show_subscribe_alert(){
        if(!App.isForeground)return;
        if(PreferenceSettings.getAudioActivityStatus()){
            LocalMessageManager.getInstance().send(R.id.audioActivity_subscription_alert);
        }else{
            LocalMessageManager.getInstance().send(R.id.mainActivity_subscription_alert);
        }
    }

    @Override
    public void instantReset() {
        if (isMediaPlayer()) {
            if (mMediaPlayer.getCurrentPosition() < 5000) {
                skip(false);
            } else {
                resetSong();
            }
        }
    }

    /**
     * Once the {@link MediaPlayer} is released, it can't be used again, and another one has to be
     * created. In the onStop() method of the activities the {@link MediaPlayer} is
     * released. Then in the onStart() of the activities a new {@link MediaPlayer}
     * object has to be created. That's why this method is private, and called by load(int) and
     * not the constructor.
     */
    @Override
    public void playSelectedSong(@NonNull final Media song) {
         setCurrentSong(song);
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
                mState = PlaybackInfoListener.State.LOADING;
            } else {
                mMediaPlayer = new MediaPlayer();
                EqualizerUtils.openAudioEffectSession(mContext, mMediaPlayer.getAudioSessionId());

                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setOnCompletionListener(this);
                mMediaPlayer.setOnErrorListener(this);
                mMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
                mMediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build());

                mMusicNotificationManager = mMusicService.getMusicNotificationManager();
               }
            mMusicService.startForeground(AudioNotificationManager.NOTIFICATION_ID, mMusicNotificationManager.createLoadingNotification());
            tryToGetAudioFocus();
            mMediaPlayer.setDataSource(mSelectedSong.getStream_url());
            //mMediaPlayer.prepare();

          //  Uri uri = Uri.parse(mSelectedSong.getStream_url());
         //   Map<String, String> headers = new HashMap<String, String>();
         //   headers.put("Authorization", "Bearer "+ PreferenceSettings.getAuthorizationKey()); //set headers

            // Use java reflection call the hide API:
            //Method method = mMediaPlayer.getClass().getMethod("setDataSource", Context.class, Uri.class, Map.class);
            //method.invoke(mMediaPlayer, this, uri, headers);
           // mMediaPlayer.setDataSource(App.getContext(), uri, headers);
            mMediaPlayer.prepareAsync();
            mPlaybackInfoListener.onSongSelected();
            LocalMessageManager.getInstance().send(R.id.song_selected);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(App.getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
           // Log.e("player error",e.getMessage());
        }
    }

    @Override
    public void stop_player() {
        release();
    }


    @Override
    public final MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        updateProgressBar();
        setStatus(PlaybackInfoListener.State.PLAYING);
    }

    @Override
    public void openEqualizer(@NonNull final Activity activity) {
        EqualizerUtils.openEqualizer(activity, mMediaPlayer);
    }

    @Override
    public void release() {
        if (isMediaPlayer()) {
            EqualizerUtils.closeAudioEffectSession(mContext, mMediaPlayer.getAudioSessionId());
            mMediaPlayer.release();
            mMediaPlayer = null;
            giveUpAudioFocus();
            //unregisterActionsReceiver();
            mHandler.removeCallbacks(mUpdateTimeTask);
            mPlaybackInfoListener.onMusicStopped();
            LocalMessageManager.getInstance().send(R.id.stop_player);
            mMusicService.stopForeground(true);
        }
    }

    @Override
    public boolean isPlaying() {
        return isMediaPlayer() && mMediaPlayer.isPlaying();
    }

    @Override
    public void resumeOrPause() {

        if (isPlaying()) {
            pauseMediaPlayer();
        } else {
            resumeMediaPlayer();
        }
    }

    @Override
    public final @PlaybackInfoListener.State
    int getState() {
        return mState;
    }

    @Override
    public boolean isMediaPlayer() {
        return mMediaPlayer != null;
    }

    @Override
    public void reset() {
        sReplaySong = !sReplaySong;
    }

    @Override
    public boolean isReset() {
        return sReplaySong;
    }

    @Override
    public void skip(final boolean isNext) {
        if(mSongs==null)return;
        getSkipSong(isNext);
    }

    private void getSkipSong(final boolean isNext) {
        if(mSongs==null)return;
        final int currentIndex = getPlayingSongIndex();//mSongs.indexOf(mSelectedSong);
        int index;

        try {
            index = isNext ? currentIndex + 1 : currentIndex - 1;
            mSelectedSong = mSongs.get(index);
        } catch (IndexOutOfBoundsException e) {
            mSelectedSong = currentIndex != 0 ? mSongs.get(0) : mSongs.get(mSongs.size() - 1);
            e.printStackTrace();
        }
        //stop_player();
        //mMusicService.stopForeground(true);
        if(!mSelectedSong.isHttp()){
            playSelectedSong(mSelectedSong);
        }else
        if(NetworkUtil.hasConnection(mMusicService)) {
            playSelectedSong(mSelectedSong);
        }else{
            Toast.makeText(mMusicService,App.getContext().getString(R.string.no_data_connection),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void seekTo(final int position) {
        if (isMediaPlayer() && mState!= PlaybackInfoListener.State.LOADING) {
            mMediaPlayer.seekTo(position);
        }
    }

    @Override
    public int getPlayerPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    @Override
    public int getPlayerDuration() {
        if(isMediaPlayer() &&  mState != PlaybackInfoListener.State.LOADING){
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    /**
     * Reconfigures the player according to audio focus settings and starts/restarts it. This method
     * starts/restarts the MediaPlayer instance respecting the current audio focus state. So if we
     * have focus, it will play normally; if we don't have focus, it will either leave the player
     * paused or set it to a low volume, depending on what is permitted by the current focus
     * settings.
     */
    private void configurePlayerState() {

        if (mCurrentAudioFocusState == AUDIO_NO_FOCUS_NO_DUCK) {
            // We don't have audio focus and can't duck, so we have to pause
            pauseMediaPlayer();
        } else {

            if (mCurrentAudioFocusState == AUDIO_NO_FOCUS_CAN_DUCK) {
                // We're permitted to play, but only if we 'duck', ie: play softly
                mMediaPlayer.setVolume(VOLUME_DUCK, VOLUME_DUCK);
            } else {
                mMediaPlayer.setVolume(VOLUME_NORMAL, VOLUME_NORMAL);
            }

            // If we were playing when we lost focus, we need to resume playing.
            if (sPlayOnFocusGain) {
                resumeMediaPlayer();
                sPlayOnFocusGain = false;
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(mMusicService,"Cant play this audio",Toast.LENGTH_SHORT).show();
       // mMusicNotificationManager.stopNofication();
        //unregisterActionsReceiver();
        stop_player();
      //  mMusicService.getMusicNotificationManager().stopNofication();
        return true;
    }

    private class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
            // TODO Auto-generated method stub
            final String action = intent.getAction();

            if (action != null) {

                switch (action) {
                    case AudioNotificationManager.STOP_ACTION:
                        stop_player();
                        break;
                    case AudioNotificationManager.PREV_ACTION:
                        if(Utility.isUserAccountBlocked()){
                            Toast.makeText(App.getContext(),App.getContext().getString(R.string.account_blocked_hint),Toast.LENGTH_SHORT).show();
                            break;
                        }
                        instantReset();
                        break;
                    case AudioNotificationManager.PLAY_PAUSE_ACTION:
                        if(!isPlaying() && Utility.isUserAccountBlocked()){
                            Toast.makeText(App.getContext(),App.getContext().getString(R.string.account_blocked_hint),Toast.LENGTH_SHORT).show();
                            break;
                        }
                        resumeOrPause();
                        break;
                    case AudioNotificationManager.NEXT_ACTION:
                        if(Utility.isUserAccountBlocked()){
                            Toast.makeText(App.getContext(),App.getContext().getString(R.string.account_blocked_hint),Toast.LENGTH_SHORT).show();
                            break;
                        }
                        skip(true);
                        break;

                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        if (mSelectedSong != null) {
                            pauseMediaPlayer();
                        }
                        break;
                    case BluetoothDevice.ACTION_ACL_CONNECTED:
                        if (mSelectedSong != null && !isPlaying()) {
                            resumeMediaPlayer();
                        }
                        break;
                    case Intent.ACTION_HEADSET_PLUG:
                        if (mSelectedSong != null) {
                            switch (intent.getIntExtra("state", -1)) {
                                //0 means disconnected
                                case 0:
                                    pauseMediaPlayer();
                                    break;
                                //1 means connected
                                case 1:
                                    if (!isPlaying()) {
                                        resumeMediaPlayer();
                                    }
                                    break;
                            }
                        }
                        break;
                    case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                        if (isPlaying()) {
                            pauseMediaPlayer();
                        }
                        break;
                }
            }
        }
    }

    @Override
    public int getPlayingSongIndex(){
        if(mSongs==null)return 0;
        for (Media song: mSongs) {
            if(song.getId() == mSelectedSong.getId()){
                return mSongs.indexOf(song);
            }
        }
        return 0;
    }
}
