package apps.envision.mychurch.ui.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.andremion.music.MusicCoverView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.libs.audio_playback.AudioNotificationManager;
import apps.envision.mychurch.libs.audio_playback.AudioPlayerService;
import apps.envision.mychurch.libs.audio_playback.EqualizerUtils;
import apps.envision.mychurch.libs.audio_playback.EqualizerView;
import apps.envision.mychurch.libs.audio_playback.PlayPauseDrawable;
import apps.envision.mychurch.libs.audio_playback.PlaybackInfoListener;
import apps.envision.mychurch.libs.audio_playback.PlayerAdapter;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.utils.Utility;

import static apps.envision.mychurch.utils.ImageLoader.getCircularDisplayRequestOptions;


public class RadioPlayerActivity extends AppCompatActivity implements View.OnClickListener, LocalMessageCallback {

    private ProgressBar progressBar;
    private TextView song_title;
    private ImageView play_pause;

    private boolean mIsBound;

    private AudioPlayerService mMusicService;
    private PlaybackListener mPlaybackListener;
    private PlayerAdapter mPlayerAdapter;
    private Media media;
    private Utility utility;
    private MusicCoverView mCoverView;
    private EqualizerView mEqualizerView;
    private PlayPauseDrawable mPlayPauseDrawable;
    private AdView AdMobView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radio_player);
        utility = new Utility(this);

        if(getIntent().getStringExtra("media")!=null) {
            Gson gson = new Gson();
            media = gson.fromJson(getIntent().getStringExtra("media"), Media.class);
        }

        set_media_controller_view();

        //bind service
        doBindService();
        //if user is not subscribed, load ads
        if(Utility.shouldLoadAds()) {
            initAdmobAdView();
        }
    }

    /*
    * connect to audio player service
     */
    private AudioNotificationManager mMusicNotificationManager;
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(@NonNull final ComponentName componentName, @NonNull final IBinder iBinder) {
            mMusicService = ((AudioPlayerService.LocalBinder) iBinder).getInstance();
            mPlayerAdapter = mMusicService.getMediaPlayerHolder();
            mMusicNotificationManager = mMusicService.getMusicNotificationManager();

            if (mPlaybackListener == null) {
                mPlaybackListener = new PlaybackListener();
                mPlayerAdapter.setPlaybackInfoListener(mPlaybackListener);
            }
            mIsBound = true;
            Log.e("mIsBound",String.valueOf(mIsBound));

            if(mPlayerAdapter.isMediaPlayer()){
                if(media == null){
                    currentPlayerStatus();
                }else {
                    Media current_audio = mPlayerAdapter.getCurrentMedia();
                    if (current_audio.getId() != media.getId()) {
                        mPlayerAdapter.setCurrentSong(media);
                        mPlayerAdapter.playSelectedSong(media);
                    } else {
                        currentPlayerStatus();
                    }
                }
            }else {
                if(media !=null) {
                    mPlayerAdapter.setCurrentSong(media);
                    mPlayerAdapter.playSelectedSong(media);
                }
            }
        }

        @Override
        public void onServiceDisconnected(@NonNull final ComponentName componentName) {
            mMusicService = null;
        }
    };

    /**
     * initialise media player view
     */
    private void set_media_controller_view(){
        mEqualizerView = findViewById(R.id.equalizer);
        mCoverView = (MusicCoverView) findViewById(R.id.cover);
        progressBar = findViewById(R.id.progressBar);
        song_title = findViewById(R.id.song_title);
        play_pause = findViewById(R.id.play_pause);
        mPlayPauseDrawable = new PlayPauseDrawable(60, 0XFFE91E63, 0XFFffffff,300);
        play_pause.setImageDrawable(mPlayPauseDrawable);
        findViewById(R.id.navigate_back).setOnClickListener(v -> {
            animate_finish();
            finish();
        });
    }

    private void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(this,
                AudioPlayerService.class), mConnection, Context.BIND_AUTO_CREATE);

        final Intent startNotStickyIntent = new Intent(this, AudioPlayerService.class);
        startService(startNotStickyIntent);
    }

    private void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    public void skipPrev(@NonNull final View v) {
        if (checkIsPlayer()) {//check if user account is blocked and user cant access media
            if(utility.isUserBlocked()){
                return;
            }
            mPlayerAdapter.instantReset();
            if (mPlayerAdapter.isReset()) {
                mPlayerAdapter.reset();
            }
        }
    }

    /**
     * resume or pause the audio player
     * @param v
     */
    public void resumeOrPause(@NonNull final View v) {
        if (checkIsPlayer()) {
            //check if user account is blocked and user cant access media
            if(utility.isUserBlocked()){
                return;
            }
            mPlayerAdapter.resumeOrPause();
        }
    }

    /**
     * play next audio on current playlist
     * @param v
     */
    public void skipNext(@NonNull final View v) {
        if (checkIsPlayer()) {
            //check if user account is blocked and user cant access media
            if(utility.isUserBlocked()){
                return;
            }

            mPlayerAdapter.skip(true);
        }
    }

    private boolean checkIsPlayer() {
        return mPlayerAdapter.isMediaPlayer();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceSettings.setAudioActivityStatus(true);
        LocalMessageManager.getInstance().addListener(this);
    }

    /**
     * handle local events
     * @param localMessage event message
     */
    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
        if(localMessage.getId()== R.id.open_equalizer){
            if (EqualizerUtils.hasEqualizer(this)) {
                if (checkIsPlayer()) {
                    mPlayerAdapter.openEqualizer(RadioPlayerActivity.this);
                }
            } else {
                Toast.makeText(this, getString(R.string.no_eq), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * handle playback states in the audio player
     */
    class PlaybackListener extends PlaybackInfoListener {

        @Override
        public void onSongSelected() {
            initPlayerInfo();
            showLoader();
            final Media selectedMedia = mPlayerAdapter.getCurrentMedia();
            Utility.fetchMediaTotalLikesAndComments(selectedMedia);
        }

        @Override
        public void onPositionChanged(int position) {
            //Log.e("progress change",String.valueOf(position));
            int totalDuration = mPlayerAdapter.getPlayerDuration();
            if(position>totalDuration)return;
        }

        @Override
        public void onStateChanged(@State int state) {

            updatePlayingStatus();
            if(mPlayerAdapter.getState()== State.PLAYING){
                if(progressBar.getVisibility()==View.VISIBLE)
                hideLoader();
            }
            if (mPlayerAdapter.getState() != State.RESUMED && mPlayerAdapter.getState() != State.PAUSED) {
                updatePlayingInfo(false, true);
            }
        }

        @Override
        public void onPlaybackCompleted() {

        }

        @Override
        public void onMusicStopped() {
            animate_finish();
            finish();
        }
    }

    /**
     * set the selected media and reset the player views.
     */
    private void initPlayerInfo(){
        final Media selectedMedia = mPlayerAdapter.getCurrentMedia();
        song_title.post(() -> song_title.setText(selectedMedia.getTitle()));
        load_image_thumbnail(selectedMedia);
    }

    /**
     * load coverphoto for the selected media
     * extract the vibrant color and set the background color
     * @param selectedMedia
     */
    private void load_image_thumbnail(Media selectedMedia){
        Glide.with(App.getContext())
                .asBitmap().load(selectedMedia.getCover_photo())
                .apply(getCircularDisplayRequestOptions())
                .listener(new RequestListener<Bitmap>() {
                              @Override
                              public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Bitmap> target, boolean b) {
                                  return false;
                              }

                              @Override
                              public boolean onResourceReady(Bitmap bitmap, Object o, Target<Bitmap> target, DataSource dataSource, boolean b) {
                                  //Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                                  // mCoverView.setImageDrawable(drawable);
                                  runOnUiThread(() -> mCoverView.setImageBitmap(bitmap));
                                  //mCoverView.setBackground(drawable);
                                  // mCoverView.morph();
                                  return false;
                              }
                          }
                ).submit();
    }

    /**
     * set the current player status
     */
    private void currentPlayerStatus(){
        if(mPlayerAdapter.isMediaPlayer() && (mPlayerAdapter.getState()== PlaybackInfoListener.State.PLAYING
                || mPlayerAdapter.getState()== PlaybackInfoListener.State.PAUSED || mPlayerAdapter.getState()== PlaybackInfoListener.State.RESUMED )){
            final Media selectedMedia = mPlayerAdapter.getCurrentMedia();
            song_title.post(() -> song_title.setText(selectedMedia.getTitle()));
            updatePlayingStatus();
            load_image_thumbnail(selectedMedia);
            hideLoader();
        }else if(mPlayerAdapter.isMediaPlayer() && mPlayerAdapter.getState() == PlaybackInfoListener.State.LOADING){
            initPlayerInfo();
            showLoader();
        }
    }

    private void updatePlayingStatus() {
        //final int drawable = mPlayerAdapter.getState() != PlaybackInfoListener.State.PAUSED ? R.drawable.music_pause_button : R.drawable.music_play_button;
        //play_pause.post(() -> play_pause.setImageResource(drawable));
        if(mPlayerAdapter.getState() != PlaybackInfoListener.State.PAUSED){
            mPlayPauseDrawable.animatePause();
        }else{
            mPlayPauseDrawable.animatePlay();
        }
        if(mPlayerAdapter.getState() == PlaybackInfoListener.State.PAUSED){
            mCoverView.stop();
            if (mEqualizerView.isAnimating()) {
                mEqualizerView.stopBars();
            }
        }else{
            mCoverView.start();
            mEqualizerView.setVisibility(View.VISIBLE);
            mEqualizerView.animateBars();
        }
    }

    private void updatePlayingInfo(final boolean restore, final boolean startPlay) {

        if (startPlay) {
            mPlayerAdapter.getMediaPlayer().start();
            new Handler().postDelayed(() -> mMusicService.startForeground(AudioNotificationManager.NOTIFICATION_ID, mMusicNotificationManager.createNotification()), 250);
        }


        if (restore) {
            updatePlayingStatus();
            new Handler().postDelayed(() -> {
                //stop foreground if coming from pause state
                if (mMusicService.isRestoredFromPause()) {
                    mMusicService.stopForeground(false);
                    mMusicService.getMusicNotificationManager().getNotificationManager().notify(AudioNotificationManager.NOTIFICATION_ID, mMusicService.getMusicNotificationManager().getNotificationBuilder().build());
                    mMusicService.setRestoredFromPause(false);
                }
            }, 250);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlaybackListener = null;
        doUnbindService();
        LocalMessageManager.getInstance().removeListener(this);
        PreferenceSettings.setAudioActivityStatus(false);
        utility.hide_dialog();
    }


    private void showLoader(){
        progressBar.setVisibility(View.VISIBLE);
        play_pause.setVisibility(View.GONE);
        mCoverView.stop();
        if(mEqualizerView.isAnimating()){
            mEqualizerView.stopBars();
        }
    }

    private void hideLoader(){
        progressBar.setVisibility(View.GONE);
        play_pause.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        animate_finish();
    }

    private void animate_finish(){
        overridePendingTransition(R.anim.still, R.anim.slide_down);
    }

    private void initAdmobAdView(){
        AdMobView = findViewById(R.id.admobAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        AdMobView.loadAd(adRequest);
        AdMobView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                AdMobView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
            }
        });
    }
}
