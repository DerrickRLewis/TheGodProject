package apps.envision.mychurch.ui.activities;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ui.PlayerView;
import com.google.gson.Gson;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener;

import net.alexandroid.utils.exoplayerhelper.ExoPlayerHelper;
import net.alexandroid.utils.exoplayerhelper.ExoPlayerListener;
import net.alexandroid.utils.exoplayerhelper.ExoThumbListener;

import org.jetbrains.annotations.NotNull;

import apps.envision.mychurch.R;
import apps.envision.mychurch.interfaces.LiveStreamsClickListener;
import apps.envision.mychurch.pojo.Livestreams;
import apps.envision.mychurch.utils.ExoPlayerFullScreenHandler;
import apps.envision.mychurch.utils.FullScreenHelper;
import apps.envision.mychurch.utils.Utility;


public class LiveStreamsPlayer extends AppCompatActivity implements LiveStreamsClickListener, ExoThumbListener, View.OnClickListener, ExoPlayerListener {
    private TextView title;
    private Livestreams livestreams;
    private Utility utility;
    private FrameLayout video_layout;
    private Bundle savedInstanceState;

    //youtube
    private YouTubePlayerView youTubePlayerView;
    private FullScreenHelper fullScreenHelper;

    //exoplayer
    PlayerView exoPlayerView;
    private ExoPlayerHelper mExoPlayerHelper = null;
    private ExoPlayerFullScreenHandler exoPlayerFullScreenHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.savedInstanceState = savedInstanceState;
        utility = new Utility(this);
        fullScreenHelper = new FullScreenHelper(this);
        exoPlayerFullScreenHandler = new ExoPlayerFullScreenHandler(this);
        setContentView(R.layout.activity_livestreams);
        init_views();

        if(getIntent().getStringExtra("livestreams")!=null) {
            Gson gson = new Gson();
            livestreams = gson.fromJson(getIntent().getStringExtra("livestreams"), Livestreams.class);
            set_player_view();
            playSelectedMedia();
        }else{
            finish();
        }
    }

    /**
     * play the selected video
     */
    private void playSelectedMedia(){
        if(livestreams.getType().equalsIgnoreCase("youtube")){
            loadYoutubeVideo();
        }else{
            loadVideoUrl();
        }

        set_player_view();
    }

    private void loadVideoUrl(){
        removeVideoLayout();
        init_video_layout();
        exoPlayerFullScreenHandler.initFullscreenDialog(video_layout,exoPlayerView);
        mExoPlayerHelper = new ExoPlayerHelper.Builder(this, exoPlayerView)
                .setVideoUrls(livestreams.getStream_url())
                //.setSubTitlesUrls(livestreams.getTitle())
                .setRepeatModeOn(false)
                .setAutoPlayOn(true)
                .enableLiveStreamSupport()
                .addSavedInstanceState(savedInstanceState)
                .setUiControllersVisibility(true)
                .addProgressBarWithColor(getResources().getColor(R.color.colorAccent))
                .setFullScreenBtnVisible()
                .addMuteButton(false, false)
                .setMuteBtnVisible()
                .setExoPlayerEventsListener(this)
                .setThumbImageViewEnabled(this)
                .createAndPrepare();
    }

    /**
     * start youtube player module
     */
    private void loadYoutubeVideo(){
            removeVideoLayout();
            init_youtube_layout();
       // }
        // The player will automatically release itself when the activity is destroyed.
        // The player will automatically pause when the activity is paused
        // If you don't add YouTubePlayerView as a lifecycle observer, you will have to release it manually.
        getLifecycle().addObserver(youTubePlayerView);
        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                youTubePlayer.loadVideo(livestreams.getStream_url(), 0);
                addFullScreenListenerToPlayer();
            }
        });
    }

    private void addFullScreenListenerToPlayer() {
        youTubePlayerView.addFullScreenListener(new YouTubePlayerFullScreenListener() {
            @Override
            public void onYouTubePlayerEnterFullScreen() {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                fullScreenHelper.enterFullScreen();
                Utility.set_video_parent_height(video_layout);
            }

            @Override
            public void onYouTubePlayerExitFullScreen() {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                fullScreenHelper.exitFullScreen();
                Utility.set_video_parent_to_match_parent(video_layout);
            }
        });
    }

    /**
     * init the layout views
     */
    private void init_views(){
        video_layout = findViewById(R.id.video_layout);
        title = findViewById(R.id.media_title);
    }

    private void init_video_layout(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.video_player, null);
        exoPlayerView = rowView.findViewById(R.id.exoPlayerView);
        // Add the new row before the add field button.
        video_layout.addView(rowView, 0);
    }

    private void init_youtube_layout(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.youtube_live, null);
        youTubePlayerView = rowView.findViewById(R.id.youtube_player_view);
        // Add the new row before the add field button.
        video_layout.addView(rowView, 0);
    }
    /**
     * set the current player info
     */
    private void set_player_view(){
        title.setText(livestreams.getTitle());
    }

    public void removeVideoLayout() {
        if(mExoPlayerHelper!=null)mExoPlayerHelper.releasePlayer();
        video_layout.removeAllViews();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * load a video when user clicks on another video on the playlist
     * @param livestreams
     */
    @Override
    public void OnItemClick(Livestreams livestreams) {
        //check if user account is blocked
        if(utility.isUserBlocked()){
            return;
        }
        if(utility.requiresUserSubscription(livestreams)){
            new Utility(LiveStreamsPlayer.this).show_play_subscribe_alert("livetv");
        }else {
            //update current playing livestreams to false
            this.livestreams = livestreams;
            set_player_view();
            playSelectedMedia();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(youTubePlayerView!=null)youTubePlayerView.release();
        if(mExoPlayerHelper!=null)mExoPlayerHelper.onActivityDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mExoPlayerHelper!=null)mExoPlayerHelper.onActivityResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mExoPlayerHelper!=null) mExoPlayerHelper.onActivityPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mExoPlayerHelper!=null)mExoPlayerHelper.onActivityStop();
    }

    @Override
    public void onClick(View view) {
    }

    /**
     * ExoPlayerListener
     */

    @Override
    public void onThumbImageViewReady(ImageView imageView) {
       // ImageLoader.loadThumbnail(imageView,livestreams.getCover_photo());
    }

    @Override
    public void onLoadingStatusChanged(boolean isLoading, long bufferedPosition, int bufferedPercentage) {
    }

    @Override
    public void onPlayerPlaying(long duration) {
    }

    @Override
    public void onPlayerPaused(int currentWindowIndex) {
    }

    @Override
    public void onPlayerBuffering(int currentWindowIndex) {
    }

    @Override
    public void onPlayerStateEnded(int currentWindowIndex) {
    }

    @Override
    public void onPlayerStateIdle(int currentWindowIndex) {
    }

    @Override
    public void onPlayerError(String errorString) {

    }

    @Override
    public void createExoPlayerCalled(boolean isToPrepare) {
    }

    @Override
    public void releaseExoPlayerCalled() {

    }

    @Override
    public void onVideoResumeDataLoaded(int window, long position, boolean isResumeWhenReady) {
    }

    @Override
    public void onVideoTapped() {

    }

    @Override
    public boolean onPlayBtnTap() {
        return false;
    }

    @Override
    public boolean onPauseBtnTap() {
        return false;
    }

    @Override
    public void onFullScreenBtnTap() {
        exoPlayerFullScreenHandler.fullscreenToggle(video_layout,exoPlayerView);
    }

    @Override
    public void onTracksChanged(int currentWindowIndex, int nextWindowIndex, boolean isPlayBackStateReady) { }

    @Override
    public void onMuteStateChanged(boolean isMuted) {

    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        if(mExoPlayerHelper!=null)
        mExoPlayerHelper.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }
}
