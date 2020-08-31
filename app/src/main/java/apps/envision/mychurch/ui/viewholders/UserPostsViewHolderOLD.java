package apps.envision.mychurch.ui.viewholders;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import apps.envision.mychurch.R;
import apps.envision.mychurch.interfaces.UserPostsListener;
import apps.envision.mychurch.pojo.UserPosts;
import apps.envision.mychurch.utils.ImageLoader;
import apps.envision.mychurch.utils.TimUtil;
import apps.envision.mychurch.utils.TouchImageView;
import apps.envision.mychurch.utils.Utility;


public class UserPostsViewHolderOLD extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView name, time;
    private TextView content;
    private ImageView avatar;
    private UserPostsListener userPostsListener;
    private UserPosts userPosts;
    private ViewPager viewPager;
    private MediaPagerAdapter mediaPagerAdapter;
    private List<String> mediaList = new ArrayList<>();
    private long playbackPosition = 0;
    private int currentWindow = 0;

    public UserPostsViewHolderOLD(View itemView, UserPostsListener userPostsListener) {
        super(itemView);

        time = itemView.findViewById(R.id.time);
        name = itemView.findViewById(R.id.name);
        content = itemView.findViewById(R.id.content);
        avatar = itemView.findViewById(R.id.avatar);
        viewPager = itemView.findViewById(R.id.viewPager);

        this.userPostsListener = userPostsListener;
    }

    public void bind(UserPosts userPosts){
        this.userPosts = userPosts;
        name.setText(userPosts.getName());
        ImageLoader.loadUserAvatar(avatar, userPosts.getAvatar());
        time.setText(TimUtil.timeAgo(userPosts.getTimestamp()));
        if(userPosts.getContent().equalsIgnoreCase("")){
            content.setVisibility(View.GONE);
        }else{
            content.setVisibility(View.VISIBLE);
            content.setText(Utility.getBase64DEcodedString(userPosts.getContent()));
        }
        mediaList = getMediaLinks(userPosts.getMedia());
        if(mediaList.size()==0){
            viewPager.setVisibility(View.GONE);
        }else{
            viewPager.setVisibility(View.VISIBLE);
            PagerAdapter pagerAdapter = new MediaPagerAdapter(userPostsListener.getPostActivity(), mediaList);
            viewPager.setAdapter(pagerAdapter);
            viewPager.setOffscreenPageLimit(0);
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    String item = mediaList.get(position);
                    String ext = getFileExtension(item);
                    if (ext.equalsIgnoreCase("mp4")) {
                        View viewTag = viewPager.findViewWithTag("view" + position);
                        PlayerView simpleExoPlayerView = viewTag.findViewById(R.id.exoPlayerView);
                        if (simpleExoPlayerView.getPlayer() != null) {
                            SimpleExoPlayer player = (SimpleExoPlayer) simpleExoPlayerView.getPlayer();
                            playbackPosition = player.getCurrentPosition();
                            currentWindow = player.getCurrentWindowIndex();
                            player.setPlayWhenReady(false);
                        }
                    }
                }

                @Override
                public void onPageSelected(int position) {
                    String item = mediaList.get(position);
                    String ext = getFileExtension(item);
                    if (ext.equalsIgnoreCase("mp4")) {
                         View viewTag = viewPager.findViewWithTag("view" + position);
                        PlayerView simpleExoPlayerView = viewTag.findViewById(R.id.exoPlayerView);
                        if (simpleExoPlayerView.getPlayer() != null) {
                            SimpleExoPlayer player = (SimpleExoPlayer) simpleExoPlayerView.getPlayer();
                            Uri mediaUri = Uri.parse(item);
                            MediaSource mediaSource = new ExtractorMediaSource.Factory(
                                    new DefaultHttpDataSourceFactory("media-slider-view")).
                                    createMediaSource(mediaUri);
                            playbackPosition = player.getCurrentPosition();
                            currentWindow = player.getCurrentWindowIndex();
                            player.prepare(mediaSource, true, true);
                            player.setPlayWhenReady(false);
                            player.seekTo(0, 0);

                        }
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){

        }
    }

    private class MediaPagerAdapter extends PagerAdapter {
        private Activity activity;
        private List<String> mediaList;
        TouchImageView imageView;
        //exoplayer
        SimpleExoPlayer player;
        PlayerView simpleExoPlayerView;
        MediaSource mediaSource;


        private MediaPagerAdapter(Activity activity, List<String> mediaList) {
            this.activity = activity;
            this.mediaList = mediaList;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(activity.LAYOUT_INFLATER_SERVICE);
            View view = null;
            String item = mediaList.get(position);
            String ext = getFileExtension(item);
            if (ext.equalsIgnoreCase("mp4")) {
                view = inflater.inflate(R.layout.post_video_player, null);
                simpleExoPlayerView = view.findViewById(R.id.exoPlayerView);
                simpleExoPlayerView.setTag("view" + position);

                TrackSelection.Factory videoTrackSelectionFactory =
                        new AdaptiveTrackSelection.Factory();
                DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
                player = ExoPlayerFactory.newSimpleInstance(activity, trackSelector);
                Uri mediaUri = Uri.parse(item);
                mediaSource = new ExtractorMediaSource.Factory(
                        new DefaultHttpDataSourceFactory("media-slider-view")).
                        createMediaSource(mediaUri);
                simpleExoPlayerView.setPlayer(player);
                player.prepare(mediaSource, true, true);
                player.setPlayWhenReady(false);
                player.seekTo(0, 0);



            } else {
                view = inflater.inflate(R.layout.post_image_item, container, false);
                imageView = view.findViewById(R.id.mBigImage);
                ImageLoader.loadPostImage(imageView,item);
            }
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return mediaList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return (view == o);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    private static List<String> getMediaLinks(String mdia){
        List<String> sliderList = new ArrayList<>();
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        List list = new Gson().fromJson(mdia, listType);
        if(list!=null){
            sliderList = list;
        }
        return sliderList;
    }

    private String getFileExtension(String link){
        String ext = "mp4";
        if(link.contains(".")) {
            ext = link.substring(link.lastIndexOf("."));
        }
        return ext.replace(".","");
    }
}
