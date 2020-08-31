package apps.envision.mychurch.ui.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.DataViewModel;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.interfaces.MediaClickListener;
import apps.envision.mychurch.libs.likes.SmallBangView;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.libs.visualizer.VisualizerView;
import apps.envision.mychurch.libs.visualizer.renderer.CircleBarRenderer;
import apps.envision.mychurch.libs.visualizer.renderer.CircleRenderer;
import apps.envision.mychurch.libs.audio_playback.EqualizerUtils;
import apps.envision.mychurch.libs.audio_playback.AudioNotificationManager;
import apps.envision.mychurch.libs.audio_playback.AudioPlayerService;
import apps.envision.mychurch.libs.audio_playback.PlaybackInfoListener;
import apps.envision.mychurch.libs.audio_playback.PlayerAdapter;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.ui.adapters.PlayingListAdapter;
import apps.envision.mychurch.utils.ImageLoader;
import apps.envision.mychurch.utils.MediaOptions;
import apps.envision.mychurch.utils.NetworkUtil;
import apps.envision.mychurch.utils.ObjectMapper;
import apps.envision.mychurch.utils.Utility;

import static apps.envision.mychurch.utils.ImageLoader.getDisplayRequestOptions;
import static apps.envision.mychurch.utils.TimUtil.getProgressPercentage;
import static apps.envision.mychurch.utils.TimUtil.progressToTimer;
import static apps.envision.mychurch.utils.Utility.darkenColor;


public class AudioPlayerActivity extends AppCompatActivity implements View.OnClickListener, MediaClickListener, LocalMessageCallback {

    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 9976;
    private ProgressBar progressBar;
    private SeekBar mSeekbar;
    private TextView song_title,startText,endText,repeat_indicator,likes_count,comments_count;
    private ImageView thumbnail,play_pause,repeatButton,likeButton;

    private VisualizerView mVisualizerView;
    private Visualizer mVisualizer;
    private boolean mIsBound;
    private boolean mUserIsSeeking = false;

    private AudioPlayerService mMusicService;
    private PlaybackListener mPlaybackListener;
    private PlayerAdapter mPlayerAdapter;

    private List<Media> playlist = new ArrayList<>();
    private Media media;

    private Dialog playingListDialog;
    private Utility utility;
    private MediaOptions mediaOptions;
    private AdView AdMobView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_player);
        mediaOptions = new MediaOptions(this);
        utility = new Utility(this);

        if(getIntent().getStringExtra("media")!=null) {
            Gson gson = new Gson();
            media = gson.fromJson(getIntent().getStringExtra("media"), Media.class);
        }

        set_media_controller_view();

        //bind service
        doBindService();


        // Get a new or existing ViewModel from the ViewModelProvider.
        DataViewModel dataViewModel = ViewModelProviders.of(this).get(DataViewModel.class);

        dataViewModel.getPlayingAudios().observe(this, audioList -> {
            if(audioList!=null) {
                this.playlist = ObjectMapper.mapMediaFromPlayingAudiosList(audioList);
                if(mIsBound) {
                   mPlayerAdapter.setPlaylist(playlist);
                }
            }
        });

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

            if (ContextCompat.checkSelfPermission(AudioPlayerActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                request_permission();
            }else {
                enableVisualizer();
            }

            if(playlist!=null){
                mPlayerAdapter.setPlaylist(playlist);
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
        mVisualizerView = findViewById(R.id.visual_view);
        thumbnail = findViewById(R.id.thumbnail);
        song_title = findViewById(R.id.song_title);
        startText = findViewById(R.id.startText);
        endText = findViewById(R.id.endText);
        play_pause = findViewById(R.id.play_pause);
        progressBar = findViewById(R.id.progressBar);
        mSeekbar = findViewById(R.id.seekBar1);
        likeButton = findViewById(R.id.likeButton);
        comments_count = findViewById(R.id.comments_count);
        likes_count = findViewById(R.id.likes_count);
        mSeekbar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        mSeekbar.getThumb().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        initializeSeekBar();

        repeatButton = findViewById(R.id.repeatButton);
        repeatButton.setOnClickListener(this);
        repeat_indicator = findViewById(R.id.repeat_indicator);
        set_repeat_mode();

        findViewById(R.id.navigate_back).setOnClickListener(v -> {
            animate_finish();
            finish();
        });
        Utility.fetchMediaTotalLikesAndComments(media);
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

    /**
     * shuffle the current playlist
     * @param v
     */
    public void shuffleSongs(@NonNull final View v) {
        if(playlist!=null && playlist.size()>1) {
            Collections.shuffle(playlist);
            mPlayerAdapter.setPlaylist(playlist);
            mPlayerAdapter.playSelectedSong(playlist.get(0));
        }
    }

    /**
     * When user clicks on the option menu of any media
     * display the popu menu
     * @param v
     */
    public void mediaOptions(@NonNull final View v) {
        Media media = mPlayerAdapter.getCurrentMedia();
        mediaOptions.audioDisplay(v,media);
    }

    /**
     * launch the comment activity for the selected media
     * @param v
     */
    public void startCommentsActivity(@NonNull final View v) {
        Media media = mPlayerAdapter.getCurrentMedia();
        if(media.isHttp()){
            Gson gson = new Gson();
            String myJson = gson.toJson(media);
            Intent intent = new Intent(this, CommentsActivity.class);
            intent.putExtra("media", myJson);
            startActivity(intent);
        }
    }

    /**
     * method to like or unlike media
     * and update media likes count
     * @param v
     */
    public void likeMedia(@NonNull final View v){
        SmallBangView like_heart = (SmallBangView) v;
        Media media = mPlayerAdapter.getCurrentMedia();
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
            set_likes_count(media);
            Utility.likeunlikemedia(media);
        }else{
            Toast.makeText(this,getString(R.string.like_media_error),Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * enable comments/likes display for the current audio
     */
    private void enableDisableSocialLayout(){
        Media media = mPlayerAdapter.getCurrentMedia();
        if(media.isHttp()){
            findViewById(R.id.comments_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.likes_layout).setVisibility(View.VISIBLE);

            set_comments_count(media);
            set_likes_count(media);
            set_likes_button_color(media.isUserLiked());
        }else{
            findViewById(R.id.comments_layout).setVisibility(View.GONE);
            findViewById(R.id.likes_layout).setVisibility(View.GONE);
        }
    }

    /**
     * set the current comments count for the current audio
     * @param media
     */
    private void set_comments_count(Media media){
        if(media.getComments_count() == 0){
            comments_count.setText("");
        }else{
            comments_count.setText(Utility.reduceCountToString(media.getComments_count()));
        }
    }

    /**
     * set the current likes count for the current audio
     * @param media
     */
    private void set_likes_count(Media media){
        if(media.getLikes_count() == 0){
            likes_count.setText("");
        }else{
            likes_count.setText(Utility.reduceCountToString(media.getLikes_count()));
        }
    }

    /**
     * set the color for the like button
     * @param isLiked
     */
    private void set_likes_button_color(boolean isLiked){
        Drawable mDrawable;
        if(!isLiked){
            mDrawable = ContextCompat.getDrawable(AudioPlayerActivity.this,R.drawable.like_icon_outline);
            mDrawable.setColorFilter(new
                    PorterDuffColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN));
        }else{
            mDrawable = ContextCompat.getDrawable(AudioPlayerActivity.this,R.drawable.like_icon_filled);
            mDrawable.setColorFilter(new
                    PorterDuffColorFilter(getResources().getColor(R.color.accent), PorterDuff.Mode.SRC_IN));
        }
        likeButton.setImageDrawable(mDrawable);
    }

    /**
     * here we load the current playlist in a dialog
     */
    public void loadPlaylist(@NonNull final View v) {
        Media currentMedia = mPlayerAdapter.getCurrentMedia();
        View view = LayoutInflater.from(this).inflate(R.layout.playlist_media, null);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.playlist);
        TextView no_of_tracks = (TextView)  view.findViewById(R.id.no_of_tracks);
        no_of_tracks.setText(playlist.size()+ " tracks");
        TextView track_pos = (TextView)  view.findViewById(R.id.track_pos);
        track_pos.setText(String.valueOf(mPlayerAdapter.getPlayingSongIndex() + 1)+"/"+String.valueOf(playlist.size()));
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        PlayingListAdapter playingListAdapter = new PlayingListAdapter(this);
        playingListAdapter.setCurrentMedia(currentMedia);
        playingListAdapter.setCurrentMediaState(mPlayerAdapter.getState());
        recyclerView.setAdapter(playingListAdapter);
        playingListAdapter.setAdapter(playlist);

        view.findViewById(R.id.close).setOnClickListener(v1 -> playingListDialog.dismiss());

        playingListDialog = new Dialog(this, R.style.MaterialList);
        playingListDialog.setContentView(view);
        playingListDialog.setCancelable(true);
        playingListDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        playingListDialog.getWindow().setGravity(Gravity.TOP);
        playingListDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        playingListDialog.getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        playingListDialog.show();
    }

    private boolean checkIsPlayer() {
        return mPlayerAdapter.isMediaPlayer();
    }

    //visualizer
    private void addCircleBarRenderer()
    {
        Paint paint2 = new Paint();
        paint2.setStrokeWidth(12f);
        paint2.setAntiAlias(true);
        paint2.setColor(Color.argb(240, 172, 175, 64));
        // BarGraphRenderer barGraphRendererTop = new BarGraphRenderer(4,
        // paint2, false);
        CircleBarRenderer barGraphRendererTop = new CircleBarRenderer(paint2, 4);
        mVisualizerView.clearRenderers();
        mVisualizerView.addRenderer(barGraphRendererTop);
    }

    //add circle renderer
    private void addCircleRenderer()
    {
        Paint paint = new Paint();
        paint.setStrokeWidth(3f);
        paint.setAntiAlias(true);
        paint.setColor(Color.argb(255, 222, 92, 143));
        CircleRenderer circleRenderer = new CircleRenderer(paint, true);
        mVisualizerView.addRenderer(circleRenderer);
    }

    /**
     * Links the visualizer to a player
     * @param player - MediaPlayer instance to link to
     */
    public void linkVisualizer(MediaPlayer player)
    {
        if(player != null) {
            Log.e("getAudioSessionId",String.valueOf(player.getAudioSessionId()));
            // Create the Visualizer object and attach it to our media player.
            mVisualizer = new Visualizer(player.getAudioSessionId());
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

            // Pass through Visualizer data to VisualizerView
            Visualizer.OnDataCaptureListener captureListener = new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
                                                  int samplingRate) {
                    mVisualizerView.updateVisualizer(bytes);
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] bytes,
                                             int samplingRate) {
                    mVisualizerView.updateVisualizerFFT(bytes);
                }
            };

            mVisualizer.setDataCaptureListener(captureListener,
                    Visualizer.getMaxCaptureRate() / 2, true, true);

            // Enabled Visualizer and disable when we're done with the stream
            mVisualizer.setEnabled(true);
        }else{
            Log.e("getAudioSessionId","null");
        }
    }

    public void releaseVisualizer() {
        if(mVisualizer!=null)
            mVisualizer.release();
    }

    private void initializeSeekBar() {
        mSeekbar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int userSelectedPosition = 0;

                    @Override
                    public void onStartTrackingTouch(@NonNull final SeekBar seekBar) {
                        mUserIsSeeking = true;
                    }

                    @Override
                    public void onProgressChanged(@NonNull final SeekBar seekBar, final int progress, final boolean fromUser) {
                        if (fromUser) {
                            userSelectedPosition = progress;
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(@NonNull final SeekBar seekBar) {
                        mUserIsSeeking = false;
                        if(mPlayerAdapter.getState()!= PlaybackInfoListener.State.LOADING){
                            int totalDuration = mPlayerAdapter.getPlayerDuration();
                            int currentPosition = progressToTimer(userSelectedPosition, totalDuration);
                            mPlayerAdapter.seekTo(currentPosition);

                            if (mPlayerAdapter.getState() == PlaybackInfoListener.State.PAUSED)
                                startText.setText(DateUtils.formatElapsedTime(mPlayerAdapter.getPlayerPosition() / 1000));
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.repeatButton:
                PreferenceSettings.set_song_repeat_mode();
                set_repeat_mode();
                break;
        }
    }

    @Override
    public void OnItemClick(Media media, String type) {
        if(playingListDialog!=null)
        playingListDialog.dismiss();

        //check if user account is blocked and user cant access media
        if(utility.isUserBlocked()){
            return;
        }

        //if media requires subscription and user is not on active subscription
        //show alert to user
        if(utility.requiresUserSubscription(media)){
            utility.show_play_subscribe_alert(media.getMedia_type());
        }else {
            mPlayerAdapter.playSelectedSong(media);
        }
    }

    @Override
    public void OnOptionClick(Media media, View view) {
       //do nothing
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
        if(localMessage.getId()==R.id.audioActivity_subscription_alert){
            utility.show_play_subscribe_alert(getString(R.string.audio));
        }
        if(localMessage.getId()==R.id.open_equalizer){
            if (EqualizerUtils.hasEqualizer(this)) {
                if (checkIsPlayer()) {
                    mPlayerAdapter.openEqualizer(AudioPlayerActivity.this);
                }
            } else {
                Toast.makeText(this, getString(R.string.no_eq), Toast.LENGTH_SHORT).show();
            }
        }
        if(localMessage.getId() == R.id.update_media_comment_count || localMessage.getId() == R.id.updateMediaTotalLikesAndCommentsViews){
            Media _media = (Media) localMessage.getObject();
            Media playing_media = mPlayerAdapter.getCurrentMedia();
            if(_media!=null && playing_media.getId() == _media.getId()) {
                playing_media.setComments_count(_media.getComments_count());
                playing_media.setLikes_count(_media.getLikes_count());
                set_comments_count(playing_media);
                set_likes_count(playing_media);
            }
        }
        if(localMessage.getId()== R.id.reverse_media_reaction){
            Media __media = (Media) localMessage.getObject();
            Media playing_media = mPlayerAdapter.getCurrentMedia();
            if(__media!=null && playing_media.getId() == __media.getId()) {
                playing_media.setLikes_count(__media.getLikes_count());
                playing_media.setUserLiked(__media.isUserLiked());
                set_likes_count(playing_media);
                set_likes_button_color(__media.isUserLiked());
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
            if (!mUserIsSeeking) {
                mSeekbar.setProgress(getProgressPercentage(position, totalDuration));
            }
            int currentPosition = position / 1000;
            startText.setText(DateUtils.formatElapsedTime(currentPosition));
        }

        @Override
        public void onStateChanged(@State int state) {

            updatePlayingStatus();
            if(mPlayerAdapter.getState()==State.PLAYING){
                if(progressBar.getVisibility()==View.VISIBLE)
                hideLoader();
                int duration = mPlayerAdapter.getPlayerDuration();
                endText.setText(DateUtils.formatElapsedTime(duration / 1000));
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
        mSeekbar.setProgress(0);
        mSeekbar.setMax(100);
        startText.setText("0.00");
        endText.setText("0.00");
        song_title.post(() -> song_title.setText(selectedMedia.getTitle()));
        load_image_thumbnail(selectedMedia);
    }

    /**
     * load coverphoto for the selected media
     * extract the vibrant color and set the background color
     * @param selectedMedia
     */
    private void load_image_thumbnail(Media selectedMedia){
        ImageLoader.loadThumbnail(thumbnail,selectedMedia.getCover_photo());
        enableDisableSocialLayout();
        Glide.with(App.getContext())
                .asBitmap().load(selectedMedia.getCover_photo())
                .apply(getDisplayRequestOptions())
                .listener(new RequestListener<Bitmap>() {
                              @Override
                              public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Bitmap> target, boolean b) {
                                  return false;
                              }

                              @Override
                              public boolean onResourceReady(Bitmap bitmap, Object o, Target<Bitmap> target, DataSource dataSource, boolean b) {
                                  runOnUiThread(() -> {
                                      findViewById(R.id.parent_view)
                                              .setBackground(new BitmapDrawable(App.getContext().getResources(), bitmap));
                                      Palette.from(bitmap).generate(palette -> {
                                          if(palette!=null) {
                                              int vibrantColor = palette.getVibrantColor(getResources().getColor(R.color.primary));
                                              findViewById(R.id.background_color).setBackgroundColor(darkenColor(vibrantColor));
                                              findViewById(R.id.background_color).getBackground().setAlpha(51);
                                          }
                                      });
                                  });
                                  return false;
                              }
                          }
                ).submit();
    }

    /**
     * set the current player status
     */
    private void currentPlayerStatus(){
        if(mPlayerAdapter.isMediaPlayer() && (mPlayerAdapter.getState()==PlaybackInfoListener.State.PLAYING
                || mPlayerAdapter.getState()==PlaybackInfoListener.State.PAUSED || mPlayerAdapter.getState()==PlaybackInfoListener.State.RESUMED )){
            final Media selectedMedia = mPlayerAdapter.getCurrentMedia();
            song_title.post(() -> song_title.setText(selectedMedia.getTitle()));
            int totalDuration = mPlayerAdapter.getPlayerDuration();
            int position = mPlayerAdapter.getPlayerPosition();
            mSeekbar.setProgress(getProgressPercentage(position, totalDuration));
            int currentPosition = position / 1000;
            startText.setText(DateUtils.formatElapsedTime(currentPosition));
            int duration = mPlayerAdapter.getPlayerDuration();
            if (duration != 0) {
                endText.setText(DateUtils.formatElapsedTime(duration / 1000));
            } else {
                endText.setText("0:00");
            }
            updatePlayingStatus();
            load_image_thumbnail(selectedMedia);
            enableDisableSocialLayout();
            hideLoader();
        }else if(mPlayerAdapter.isMediaPlayer() && mPlayerAdapter.getState() == PlaybackInfoListener.State.LOADING){
            initPlayerInfo();
            showLoader();
        }
    }

    private void updatePlayingStatus() {
        final int drawable = mPlayerAdapter.getState() != PlaybackInfoListener.State.PAUSED ? R.drawable.music_pause_button : R.drawable.music_play_button;
        play_pause.post(() -> play_pause.setImageResource(drawable));
    }

    private void updatePlayingInfo(final boolean restore, final boolean startPlay) {

        if (startPlay) {
            mPlayerAdapter.getMediaPlayer().start();
            new Handler().postDelayed(() -> mMusicService.startForeground(AudioNotificationManager.NOTIFICATION_ID, mMusicNotificationManager.createNotification()), 250);
        }


        if (restore) {
            mSeekbar.setProgress(mPlayerAdapter.getPlayerPosition());
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

    private void enableVisualizer(){
        linkVisualizer(mPlayerAdapter.getMediaPlayer());
        // Start with just line renderer
        //addLineRenderer();
        addCircleBarRenderer();
        addCircleRenderer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlaybackListener = null;
        if(mVisualizerView!=null) {
            releaseVisualizer();
        }
        doUnbindService();
        LocalMessageManager.getInstance().removeListener(this);
        PreferenceSettings.setAudioActivityStatus(false);
        utility.hide_dialog();
    }

    /**
     * request permission to display audio visualizer
     */
    public void request_permission() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.request_permission_hint)
                .setMessage(R.string.request_audio_msg)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    //Prompt the user once explanation has been shown
                    ActivityCompat.requestPermissions(AudioPlayerActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
                    dialogInterface.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .setCancelable(false)
                .create()
                .show();
    }

    /**
     * onrequest permissions callback
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableVisualizer();
                } else {
                    Toast.makeText(AudioPlayerActivity.this, "Allow Permission from settings", Toast.LENGTH_LONG).show();
                }
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showLoader(){
        progressBar.setVisibility(View.VISIBLE);
        play_pause.setVisibility(View.GONE);
    }

    private void hideLoader(){
        progressBar.setVisibility(View.GONE);
        play_pause.setVisibility(View.VISIBLE);
    }

    /**
     * when the user clicks on the repeat image button
     * we get the current repeat mode from our shared preferences and
     * set the appropiate repeat mode
     */
    private void set_repeat_mode(){
        switch(PreferenceSettings.get_song_repeat_mode()){
            case 1:
                repeatButton.setColorFilter(getResources().getColor(R.color.white));
                repeat_indicator.setText("");
                break;
            case 2:
                repeatButton.setColorFilter(getResources().getColor(R.color.colorAccent));
                repeat_indicator.setText("1");
                break;
            case 3:
                repeatButton.setColorFilter(getResources().getColor(R.color.colorAccent));
                repeat_indicator.setText("A");
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        animate_finish();
    }

    private void animate_finish(){
        overridePendingTransition(R.anim.still,R.anim.slide_down);
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
