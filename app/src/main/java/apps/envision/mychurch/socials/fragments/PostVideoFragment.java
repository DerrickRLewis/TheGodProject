package apps.envision.mychurch.socials.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.exoplayer2.ui.PlayerView;

import net.alexandroid.utils.exoplayerhelper.ExoPlayerHelper;
import net.alexandroid.utils.exoplayerhelper.ExoPlayerListener;
import net.alexandroid.utils.exoplayerhelper.ExoThumbListener;

import org.jetbrains.annotations.NotNull;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.RecyclerviewState;
import apps.envision.mychurch.utils.ExoPlayerFullScreenHandler;
import apps.envision.mychurch.utils.ImageLoader;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PostVideoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostVideoFragment extends Fragment implements ExoPlayerListener, ExoThumbListener, LocalMessageCallback {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String url;
    private int position = -1;
    //exoplayer
    private PlayerView exoPlayerView;
    private ExoPlayerHelper mExoPlayerHelper = null;
    private Bundle savedInstanceState;
    private boolean isPlaying = false;
    private FrameLayout video_layout;

    public PostVideoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param url Parameter 1.
     * @return A new instance of fragment PostVideoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PostVideoFragment newInstance(String url, int position) {
        PostVideoFragment fragment = new PostVideoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, url);
        args.putInt(ARG_PARAM2, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        if (getArguments() != null) {
            url = getArguments().getString(ARG_PARAM1);
            position = getArguments().getInt(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post_video, container, false);

        video_layout = view.findViewById(R.id.video_layout);
        init_thumnail_layout();


        // exoPlayerView = view.findViewById(R.id.exoPlayerView);
       // loadVideoUrl();
        return view;
    }

    private void init_thumnail_layout(){
        clearLayout();
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.fragment_post_video_thumbnail, null);
        ImageView img = rowView.findViewById(R.id.img);
        ImageLoader.loadVideoThumbnail(img, url);
        video_layout.addView(rowView, 0);
        rowView.findViewById(R.id.img_holder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                init_video_layout();
            }
        });
        isPlaying = false;
    }

    private void init_video_layout(){
        clearLayout();
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.video_player, null);
        exoPlayerView = rowView.findViewById(R.id.exoPlayerView);
        // Add the new row before the add field button.
        video_layout.addView(rowView, 0);
        loadVideoUrl();
    }

    public void clearLayout() {
        if(mExoPlayerHelper!=null)mExoPlayerHelper.releasePlayer();
        video_layout.removeAllViews();
    }

    private void loadVideoUrl(){
        //if(mExoPlayerHelper!=null)mExoPlayerHelper.releasePlayer();
        mExoPlayerHelper = new ExoPlayerHelper.Builder(getActivity(), exoPlayerView)
                .setVideoUrls(url)
                //.setSubTitlesUrls(livestreams.getTitle())
                .setRepeatModeOn(false)
                .setAutoPlayOn(true)
                //.enableLiveStreamSupport()
                .setUiControllersVisibility(true)
                .addSavedInstanceState(savedInstanceState)
                .addProgressBarWithColor(getResources().getColor(R.color.colorAccent))
               // .setFullScreenBtnVisible()
                .addMuteButton(false, false)
                .setMuteBtnVisible()
                .setExoPlayerEventsListener(this)
               // .setThumbImageViewEnabled(this)
                .createAndPrepare();
        LocalMessageManager.getInstance().send(R.id.play_video_tapped, position);
    }

    /**
     * ExoPlayerListener
     */

    @Override
    public void onThumbImageViewReady(ImageView imageView) {
        //ImageLoader.loadThumbnail(imageView,url);
        //ImageLoader.loadVideoThumbnail(imageView, url);
    }

    @Override
    public void onLoadingStatusChanged(boolean isLoading, long bufferedPosition, int bufferedPercentage) {
    }

    @Override
    public void onPlayerPlaying(long duration) {
        isPlaying = true;
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
        Log.e("tap button","tap button pressed");
        return false;
    }

    @Override
    public boolean onPauseBtnTap() {
        return false;
    }

    @Override
    public void onFullScreenBtnTap() {
        //exoPlayerFullScreenHandler.fullscreenToggle(video_layout,exoPlayerView);
    }

    @Override
    public void onTracksChanged(int currentWindowIndex, int nextWindowIndex, boolean isPlayBackStateReady) { }

    @Override
    public void onMuteStateChanged(boolean isMuted) {

    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        if(mExoPlayerHelper==null)return;
        mExoPlayerHelper.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mExoPlayerHelper!=null)mExoPlayerHelper.onActivityDestroy();
        LocalMessageManager.getInstance().removeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        init_thumnail_layout();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mExoPlayerHelper!=null)mExoPlayerHelper.onActivityStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        //if(mExoPlayerHelper!=null)mExoPlayerHelper.onActivityResume();
        //loadVideoUrl();
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalMessageManager.getInstance().addListener(this);
    }

    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
        if(localMessage.getId() == R.id.log_recyclerview_pos){
            RecyclerviewState recyclerviewState = (RecyclerviewState) localMessage.getObject();
            if(recyclerviewState!=null) {

                //Log.e("this position", "position: " + this.position);
                int position = recyclerviewState.currentPosition;
               // Log.e("player position", "position: " + position);

                if(isPlaying && this.position == position) {
                    int percentage = recyclerviewState.visiblePercentage;
                    Log.e("visible position", "percentage: " + percentage);
                    //Log.e("recycler pos", "recyclerview pos " + itm_pos);
                    //Log.e("visible pos", "recyclerview pos " + visible);
                    if (percentage < 10) {
                        if (mExoPlayerHelper != null) {
                            //init_thumnail_layout();
                            mExoPlayerHelper.playerPause();
                        }
                    }
                }
            }
        }

        if(localMessage.getId() == R.id.play_video_tapped){
            int pos = localMessage.getArg1();
            if(pos!=this.position){
                init_thumnail_layout();
            }
        }
    }
}
