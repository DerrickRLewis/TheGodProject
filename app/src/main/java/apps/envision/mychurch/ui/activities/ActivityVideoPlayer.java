package apps.envision.mychurch.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ct7ct7ct7.androidvimeoplayer.model.PlayerState;
import com.ct7ct7ct7.androidvimeoplayer.view.VimeoPlayerActivity;
import com.ct7ct7ct7.androidvimeoplayer.view.VimeoPlayerView;
import com.dailymotion.android.player.sdk.PlayerWebView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.alexandroid.utils.exoplayerhelper.ExoPlayerHelper;
import net.alexandroid.utils.exoplayerhelper.ExoPlayerListener;
import net.alexandroid.utils.exoplayerhelper.ExoThumbListener;

import org.jetbrains.annotations.NotNull;

import apps.envision.mychurch.R;
import apps.envision.mychurch.db.DataViewModel;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.interfaces.MediaClickListener;
import apps.envision.mychurch.interfaces.MediaOptionsListener;
import apps.envision.mychurch.libs.likes.SmallBangView;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.Bookmarks;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.ui.adapters.ListMediaAdapter;
import apps.envision.mychurch.utils.ExoPlayerFullScreenHandler;
import apps.envision.mychurch.utils.FullScreenHelper;
import apps.envision.mychurch.utils.ImageLoader;
import apps.envision.mychurch.utils.MediaOptions;
import apps.envision.mychurch.utils.NetworkUtil;
import apps.envision.mychurch.utils.ObjectMapper;
import apps.envision.mychurch.utils.Utility;
import timber.log.Timber;


public class ActivityVideoPlayer extends AppCompatActivity implements MediaOptionsListener, MediaClickListener, LocalMessageCallback, ExoThumbListener, ExoPlayerListener {

    private TextView title,description,playlist_total,likes_count,comments_count;
    private ImageView likeButton;
    private Media media;
    private MediaOptions mediaOptions;
    private List<Bookmarks> bookmarksList = new ArrayList<>();
    private List<Media> mediaList = new ArrayList<>();

    private DataViewModel dataViewModel;
    private ListMediaAdapter listMediaAdapter;
    private Utility utility;
    private AdView AdMobView;

    private FrameLayout video_layout;
    private Bundle savedInstanceState;

    //dailymotion playerview
    private PlayerWebView mVideoView;
    //vimeo player view
    VimeoPlayerView vimeoPlayer;
    private int VIMEO_REQUEST_CODE = 23456;

    //exoplayer
    PlayerView exoPlayerView;
    private ExoPlayerHelper mExoPlayerHelper = null;
    private ExoPlayerFullScreenHandler exoPlayerFullScreenHandler;

    //youtube
    private YouTubePlayerView youTubePlayerView;
    private FullScreenHelper fullScreenHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        utility = new Utility(this);
        exoPlayerFullScreenHandler = new ExoPlayerFullScreenHandler(this);
        fullScreenHelper = new FullScreenHelper(this);
        mediaOptions = new MediaOptions(this,this);
        setContentView(R.layout.activity_video_player);
        init_views();

        if(getIntent().getStringExtra("media")!=null) {
            Gson gson = new Gson();
            media = gson.fromJson(getIntent().getStringExtra("media"), Media.class);
            playSelectedMedia();
        }else{
            finish();
        }


        // Get a new or existing ViewModel from the ViewModelProvider.
        dataViewModel = ViewModelProviders.of(this).get(DataViewModel.class);
        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.

        dataViewModel.getPlayingVideos().observe(this, playing_videos -> {
            // Update the cached copy of the interests in the adapter.
            mediaList = ObjectMapper.mapMediaFromPlayingVideosList(playing_videos);
            if(mediaList!=null){
                setFirstPlayingMediaFromPosition(); //set first media item, so we can remove from queued list
                setQueuedList();
            }
        });


        dataViewModel.getBookmarks().observe(this, bookmarksList -> {
            // Update the cached copy of the interests in the adapter.
            //we only set the items from db, the first time fragment is launched
            this.bookmarksList = bookmarksList;
        });

        //if user is not subscribed, load ads
        if(Utility.shouldLoadAds()) {
            initAdmobAdView();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.getStringExtra("media")!=null) {
            Gson gson = new Gson();
            media = gson.fromJson(intent.getStringExtra("media"), Media.class);
            set_player_view();
            playSelectedMedia();
            setFirstPlayingMediaFromPosition(); //set first media item, so we can remove from queued list
            setQueuedList();
        }
    }

    /**
     * play the selected video
     */
    private void playSelectedMedia(){
        if(media.getVideo_type().equalsIgnoreCase("dailymotion_video")){
            loadDailyMotionVideo();
        }else if(media.getVideo_type().equalsIgnoreCase("youtube_video")){
            loadYoutubeVideo();
        }else if(media.getVideo_type().equalsIgnoreCase("vimeo_video")){
            loadVimeoVideo();
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
                .setVideoUrls(media.getStream_url())
                //.setSubTitlesUrls(livestreams.getTitle())
                .setRepeatModeOn(false)
                .setAutoPlayOn(true)
                .enableLiveStreamSupport()
                .setUiControllersVisibility(true)
                .addSavedInstanceState(savedInstanceState)
                .addProgressBarWithColor(getResources().getColor(R.color.colorAccent))
                .setFullScreenBtnVisible()
                .addMuteButton(false, false)
                .setMuteBtnVisible()
                .setExoPlayerEventsListener(this)
                .setThumbImageViewEnabled(this)
                .createAndPrepare();
    }

    private void loadDailyMotionVideo(){
            removeVideoLayout();
            init_dailymotion_layout();
        mVideoView.load(media.getStream_url());
    }

    private void loadVimeoVideo(){
            removeVideoLayout();
            init_vimeo_layout();
        vimeoPlayer.initialize(true, Integer.parseInt(media.getStream_url()));
        vimeoPlayer.setFullscreenVisibility(true);
        vimeoPlayer.setFullscreenClickListener(view -> {
            String requestOrientation = VimeoPlayerActivity.REQUEST_ORIENTATION_AUTO;
            startActivityForResult(VimeoPlayerActivity.createIntent(ActivityVideoPlayer.this, requestOrientation, vimeoPlayer), VIMEO_REQUEST_CODE);
        });
    }

    /**
     * start youtube player module
     */
    private void loadYoutubeVideo(){
        if(youTubePlayerView!=null)youTubePlayerView.release();
            removeVideoLayout();
            init_youtube_layout();
        exoPlayerFullScreenHandler.initFullscreenDialog(video_layout,youTubePlayerView);
        // The player will automatically release itself when the activity is destroyed.
        // The player will automatically pause when the activity is paused
        // If you don't add YouTubePlayerView as a lifecycle observer, you will have to release it manually.
        getLifecycle().addObserver(youTubePlayerView);
        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                youTubePlayer.loadVideo(media.getStream_url(), 0);
                addFullScreenListenerToPlayer();
            }
        });
    }

    private void addFullScreenListenerToPlayer() {
        youTubePlayerView.addFullScreenListener(new YouTubePlayerFullScreenListener() {
            @Override
            public void onYouTubePlayerEnterFullScreen() {
                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                //fullScreenHelper.enterFullScreen();
                //Utility.set_video_parent_to_match_parent(video_layout);
                exoPlayerFullScreenHandler.fullscreenToggle(video_layout,youTubePlayerView);
            }

            @Override
            public void onYouTubePlayerExitFullScreen() {
                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                //fullScreenHelper.exitFullScreen();
                //Utility.set_video_parent_height(video_layout);
                //Utility.set_video_height(youTubePlayerView);
                exoPlayerFullScreenHandler.fullscreenToggle(video_layout,youTubePlayerView);
            }
        });
    }



    /**
     * set and display the current video playlist
     */
    private void setQueuedList(){
        List<Media> list = new ArrayList<>(mediaList);
        list.remove(media);//remove current media from list
        listMediaAdapter.setAdapter(list);
        playlist_total.setText(String.valueOf(list.size())+ " Item(s)");
        //recyclerView.scrollToPosition(0);
    }

    /**
     * init the layout views
     */
    private void init_views(){
        video_layout = findViewById(R.id.video_layout);
        title = findViewById(R.id.media_title);
        description = findViewById(R.id.media_description);
        playlist_total = findViewById(R.id.playlist_total);
        comments_count = findViewById(R.id.comments_count);
        likes_count = findViewById(R.id.likes_count);
        likeButton = findViewById(R.id.likeButton);

        // Set the recycler adapter
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.tracks);
        assert recyclerView != null;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setFocusable(false);
        listMediaAdapter = new ListMediaAdapter(this);
        recyclerView.setAdapter(listMediaAdapter);
    }

    private void init_video_layout(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.video_player, null);
        exoPlayerView = rowView.findViewById(R.id.exoPlayerView);
        // Add the new row before the add field button.
        video_layout.addView(rowView, 0);
    }

    private void init_vimeo_layout(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.vimeo, null);
        vimeoPlayer = rowView.findViewById(R.id.vimeoPlayer);
        getLifecycle().addObserver(vimeoPlayer);
        // Add the new row before the add field button.
        video_layout.addView(rowView, 0);
    }

    private void init_youtube_layout(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.youtube, null);
        youTubePlayerView = rowView.findViewById(R.id.youtube_player_view);
        // Add the new row before the add field button.
        video_layout.addView(rowView, 0);
    }

    private void init_dailymotion_layout(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.dailymotion, null);
        mVideoView = (PlayerWebView) rowView.findViewById(R.id.dailymotionPlayer);
        // Add the new row before the add field button.
        video_layout.addView(rowView, 0);
    }

    /**
     * set the current player info
     */
    private void set_player_view(){
        title.setText(media.getTitle());
        description.setText(media.getDescription());
        enableDisableSocialLayout();
        Utility.fetchMediaTotalLikesAndComments(media);
    }

    public void removeVideoLayout() {
        if(mExoPlayerHelper!=null)mExoPlayerHelper.releasePlayer();
        video_layout.removeAllViews();
    }

    /**
     * load the comments activity
     * @param v
     */
    public void startCommentsActivity(@NonNull final View v) {
        if(media.isHttp()){
            Gson gson = new Gson();
            String myJson = gson.toJson(media);
            Intent intent = new Intent(this, CommentsActivity.class);
            intent.putExtra("media", myJson);
            startActivity(intent);
        }
    }

    /**
     * method to handle media likes
     * and update media likes count
     * @param v
     */
    public void likeMedia(@NonNull final View v){
        SmallBangView like_heart = (SmallBangView) v;
        if(PreferenceSettings.isUserLoggedIn() && media.isHttp() && NetworkUtil.hasConnection(this)){
            if(media.isUserLiked()){
                media.setLikes_count(media.getLikes_count()-1);
                media.setUserLiked(false);
                set_likes_button_color(false);
            }else{
                media.setLikes_count(media.getLikes_count()+1);
                media.setUserLiked(true);
                set_likes_button_color(true);
                like_heart.likeAnimation(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                });
            }
            set_likes_count();
            Utility.likeunlikemedia(media);
        }else{
            Toast.makeText(this,getString(R.string.like_media_error),Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * if media downloads is enabled for the current video
     * download the video to users device
     * @param v
     */
    public void downloadMedia(@NonNull final View v){
        if(media.isCan_download()) mediaOptions.download_media(media);
    }

    public void shareMedia(@NonNull final View v){
        mediaOptions.shareThisMedia(media);
    }

    /**
     * enable comments/likes display for the current audio
     */
    private void enableDisableSocialLayout(){
        findViewById(R.id.download_cancelled).setVisibility(View.VISIBLE);
        if(media.isHttp()){
            findViewById(R.id.comments_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.likes_layout).setVisibility(View.VISIBLE);
            if(media.isCan_download()){
                findViewById(R.id.download_cancelled).setVisibility(View.GONE);
            }

            set_comments_count();
            set_likes_count();
            set_likes_button_color(media.isUserLiked());
        }else{
            findViewById(R.id.comments_layout).setVisibility(View.INVISIBLE);
            findViewById(R.id.likes_layout).setVisibility(View.INVISIBLE);
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        LocalMessageManager.getInstance().addListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalMessageManager.getInstance().removeListener(this);
        if(youTubePlayerView!=null)youTubePlayerView.release();
        if(mExoPlayerHelper!=null)mExoPlayerHelper.onActivityDestroy();;
        if(mVideoView!=null)mVideoView.release();
        if(vimeoPlayer!=null)vimeoPlayer.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mVideoView!=null)mVideoView.onPause();
        if(mExoPlayerHelper!=null)mExoPlayerHelper.onActivityPause();
        if(vimeoPlayer!=null)vimeoPlayer.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mExoPlayerHelper!=null)mExoPlayerHelper.onActivityStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mVideoView!=null)mVideoView.onResume();
        if(mExoPlayerHelper!=null)mExoPlayerHelper.onActivityResume();
    }

    /**
     * set the current comments count for the current video
     */
    private void set_comments_count(){
        if(media.getComments_count() == 0){
            comments_count.setText("");
        }else{
            comments_count.setText(Utility.reduceCountToString(media.getComments_count()));
        }
    }

    /**
     * set the current likes count for the current video
     */
    private void set_likes_count(){
        if(media.getLikes_count() == 0){
            likes_count.setText("");
        }else{
            likes_count.setText(Utility.reduceCountToString(media.getLikes_count()));
        }
    }

    /**
     * set the color for the like button
     * @param liked
     */
    private void set_likes_button_color(boolean liked){
        Drawable mDrawable;
        if(!liked){
            mDrawable = ContextCompat.getDrawable(ActivityVideoPlayer.this,R.drawable.like_icon_outline);
            mDrawable.setColorFilter(new
                    PorterDuffColorFilter(getResources().getColor(R.color.material_grey_700), PorterDuff.Mode.SRC_IN));
        }else{
            mDrawable = ContextCompat.getDrawable(ActivityVideoPlayer.this,R.drawable.like_icon_filled);
            mDrawable.setColorFilter(new
                    PorterDuffColorFilter(getResources().getColor(R.color.accent), PorterDuff.Mode.SRC_IN));
        }
        likeButton.setImageDrawable(mDrawable);
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
     * check if media is already added to bookmarks
     * @param media
     * @return
     */
    @Override
    public boolean isBookmarked(Media media) {
        for (Bookmarks bookmarks: bookmarksList) {
            if(bookmarks.getId() == media.getId())
                return true;
        }
        return false;
    }

    /**
     * bookmark a media
     * @param media
     */
    @Override
    public void bookmark(Media media) {
        if(!isBookmarked(media)) {
            dataViewModel.bookmarkMedia(ObjectMapper.mapBoomarkFromMedia(media));
            Toast.makeText(this, R.string.media_bookmarked,Toast.LENGTH_SHORT).show();
        } else {
            dataViewModel.removeMediaFromBookmarks(media.getId());
            Toast.makeText(this, R.string.media_unbookmarked,Toast.LENGTH_SHORT).show();
        }
    }

    public void addToPlaylist(View view) {
        addToPlaylist(media);
    }

    /**
     * load playlist activity to add media to playlist
     * @param media
     */
    @Override
    public void addToPlaylist(Media media) {
        Gson gson = new Gson();
        String myJson = gson.toJson(media);
        Intent intent = new Intent(this, AddPlaylistActivity.class);
        intent.putExtra("media", myJson);
        startActivity(intent);
    }

    @Override
    public boolean isPlaylistActivity() {
        return false;
    }

    @Override
    public boolean isDownloads() {
        return false;
    }

    /**
     * load a video when user clicks on another video on the playlist
     * @param media
     * @param type
     */
    @Override
    public void OnItemClick(Media media, String type) {
        //check if user account is blocked
        if(utility.isUserBlocked()){
            return;
        }
        if(utility.requiresUserSubscription(media)){
            new Utility(ActivityVideoPlayer.this).show_play_subscribe_alert(media.getMedia_type());
        }else {
            //update current playing media to false
            this.media = media;
            set_player_view();
            playSelectedMedia();
            setQueuedList();//reload adapter view
        }
    }

    @Override
    public void OnOptionClick(Media media, View view) {
        mediaOptions.display(view,media);
    }

    /**
     * handle local events
     * @param localMessage event message
     */
    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
        switch (localMessage.getId()){
            case R.id.video_player_progress:
                long progress = (long)localMessage.getObject();
                long duration = progress/1000; //convert to seconds
                //if duration watched is upto 10seconds we update media views
                if(duration>=10){
                    Utility.update_media_total_views(media);
                }

                if(utility.requiresUserSubscription(media)){ //if user cant preview this video without subscription, stop playback
                    stop_player();
                }else if(Utility.isMediaPreviewDuration(media,duration)){
                    //if user can preview but requires subscription to play full media
                    //we stop player once playback reaches preview duration
                    stop_player();
                }
                break;
            case R.id.update_media_comment_count: case R.id.updateMediaTotalLikesAndCommentsViews:
                Media _media = (Media) localMessage.getObject();
                if(_media!=null && media.getId() == _media.getId()) {
                    media.setComments_count(_media.getComments_count());
                    media.setLikes_count(_media.getLikes_count());
                    set_comments_count();
                    set_likes_count();
                }
                break;
            case R.id.reverse_media_reaction:
                Media __media = (Media) localMessage.getObject();
                if(__media!=null && media.getId() == __media.getId()) {
                    media.setLikes_count(__media.getLikes_count());
                    media.setUserLiked(__media.isUserLiked());
                    set_likes_count();
                    set_likes_button_color(__media.isUserLiked());
                }
                break;
        }
    }

    private void stop_player(){
        utility.show_play_subscribe_alert(media.getMedia_type());
    }

    /**
     * get the first media from the current playlist
     */
    private void setFirstPlayingMediaFromPosition(){
        for (Media itm:mediaList) {
            if(itm.getId() == media.getId()){
                media = itm;
            }
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == VIMEO_REQUEST_CODE) {
            if(data==null)return;
            float playAt = data.getFloatExtra(VimeoPlayerActivity.RESULT_STATE_VIDEO_PLAY_AT, 0.0F);
            vimeoPlayer.seekTo(playAt);
            PlayerState playerState = PlayerState.valueOf(data.getStringExtra(VimeoPlayerActivity.RESULT_STATE_PLAYER_STATE));
            if(playerState == PlayerState.PLAYING) vimeoPlayer.play();
            if(playerState == PlayerState.PAUSED) vimeoPlayer.pause();
        }
    }

    /**
     * ExoPlayerListener
     */

    @Override
    public void onThumbImageViewReady(ImageView imageView) {
        ImageLoader.loadThumbnail(imageView,media.getCover_photo());
    }

    @Override
    public void onLoadingStatusChanged(boolean isLoading, long bufferedPosition, int bufferedPercentage) {
    }

    @Override
    public void onPlayerPlaying(long duration) {
        Timber.e(String.valueOf(duration));
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
        if(mExoPlayerHelper==null)return;
        mExoPlayerHelper.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }
}
