package apps.envision.mychurch.ui.viewholders;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.alexandroid.utils.exoplayerhelper.ExoPlayerHelper;
import net.alexandroid.utils.exoplayerhelper.ExoPlayerListener;
import net.alexandroid.utils.exoplayerhelper.ExoThumbListener;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.interfaces.HymnsClickListener;
import apps.envision.mychurch.interfaces.UserPostsListener;
import apps.envision.mychurch.libs.likes.SmallBangView;
import apps.envision.mychurch.pojo.Hymns;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.pojo.UserPosts;
import apps.envision.mychurch.socials.fragments.PostImageFragment;
import apps.envision.mychurch.socials.fragments.PostVideoFragment;
import apps.envision.mychurch.ui.activities.PostsCommentsActivity;
import apps.envision.mychurch.ui.fonts.OpenSansRegularTextView;
import apps.envision.mychurch.utils.ImageLoader;
import apps.envision.mychurch.utils.LetterTileProvider;
import apps.envision.mychurch.utils.NetworkUtil;
import apps.envision.mychurch.utils.TimUtil;
import apps.envision.mychurch.utils.TouchImageView;
import apps.envision.mychurch.utils.Utility;
import me.relex.circleindicator.CircleIndicator;
import me.relex.circleindicator.CircleIndicator3;
import timber.log.Timber;


public class UserPostsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView name, time, pager_number, likes_count, comments_count;
    private OpenSansRegularTextView content;
    private ImageView avatar;
    private ImageView likeButton;
    private ImageView pin, options;
    private SmallBangView smallBangView;
    private UserPostsListener userPostsListener;
    private UserPosts userPosts;
    private ViewPager2 viewPager;
    private List<String> mediaList = new ArrayList<>();
    private CircleIndicator3 circleIndicator;
    private RelativeLayout reactions_layout;
    private LinearLayout comments_layout;
    private int adapterPosition;

    public UserPostsViewHolder(View itemView, UserPostsListener userPostsListener) {
        super(itemView);

        time = itemView.findViewById(R.id.time);
        name = itemView.findViewById(R.id.name);
        content = itemView.findViewById(R.id.content);
        avatar = itemView.findViewById(R.id.avatar);
        pin = itemView.findViewById(R.id.pin);
        options = itemView.findViewById(R.id.options);
        ImageView commentButton = itemView.findViewById(R.id.commentButton);
        comments_layout = itemView.findViewById(R.id.comments_layout);
        likeButton = itemView.findViewById(R.id.likeButton);
        likes_count = itemView.findViewById(R.id.likes_count);
        comments_count = itemView.findViewById(R.id.comments_count);
        reactions_layout = itemView.findViewById(R.id.reactions_layout);
        circleIndicator = itemView.findViewById(R.id.indicator);
        viewPager = itemView.findViewById(R.id.viewPager);
        pager_number = itemView.findViewById(R.id.pager_number);
        smallBangView = itemView.findViewById(R.id.smallBangView);

        smallBangView.setOnClickListener(this);
        comments_layout.setOnClickListener(this);
        commentButton.setOnClickListener(this);
        pin.setOnClickListener(this);
        options.setOnClickListener(this);
        name.setOnClickListener(this);
        likes_count.setOnClickListener(this);

        this.userPostsListener = userPostsListener;
    }

    public void bind(UserPosts userPosts, int pos){
        this.userPosts = userPosts;
        adapterPosition = pos;
        name.setText(userPosts.getName());
        ImageLoader.loadUserAvatar(avatar, userPosts.getAvatar());
        time.setText(TimUtil.timeAgo(userPosts.getTimestamp()));
        if(userPosts.getContent().equalsIgnoreCase("")){
            content.setVisibility(View.GONE);
        }else{
            content.setVisibility(View.VISIBLE);
            content.setContent(Utility.getBase64DEcodedString(userPosts.getContent()));
        }

        set_comments_count();
        set_likes_count(userPosts.getLikes_count());
        set_likes_button_color(userPosts.isLiked());
        set_pins_button_color(userPosts.isPinned());
        mediaList = getMediaLinks(userPosts.getMedia());
        if(mediaList.size()==0){
            viewPager.setVisibility(View.GONE);
            circleIndicator.setVisibility(View.GONE);
            pager_number.setVisibility(View.GONE);
        }else{
            viewPager.setVisibility(View.VISIBLE);
            MediaPagerAdapter mediaPagerAdapter = new MediaPagerAdapter(userPostsListener.getPostActivity(), mediaList);
            viewPager.setAdapter(mediaPagerAdapter);
            viewPager.setOffscreenPageLimit(1);
            circleIndicator.setViewPager(viewPager);
            mediaPagerAdapter.registerAdapterDataObserver(circleIndicator.getAdapterDataObserver());
            if(mediaList.size()>1){
                circleIndicator.setVisibility(View.VISIBLE);
                pager_number.setVisibility(View.VISIBLE);
                pager_number.setText("1/"+mediaList.size());
                viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        pager_number.setText((position+1)+"/"+mediaList.size());
                        super.onPageSelected(position);
                    }
                });
            }else{
                circleIndicator.setVisibility(View.GONE);
                pager_number.setVisibility(View.GONE);
            }
        }

        if(userPostsListener.getPostActivity() instanceof PostsCommentsActivity){
            //reactions_layout.setVisibility(View.GONE);
            options.setVisibility(View.GONE);
            comments_layout.setClickable(false);
        }else{
            reactions_layout.setVisibility(View.VISIBLE);
            UserData userData = PreferenceSettings.getUserData();
            if(userData.getEmail().equalsIgnoreCase(userPosts.getEmail())){
                options.setVisibility(View.VISIBLE);
            }else{
                options.setVisibility(View.GONE);
            }
        }
    }

    /**
     * set the color for the like button
     * @param liked
     */
    private void set_likes_button_color(boolean liked){
        Drawable mDrawable;
        if(!liked){
            mDrawable = ContextCompat.getDrawable(App.getContext(),R.drawable.like_icon_outline);
            mDrawable.setColorFilter(new
                    PorterDuffColorFilter(App.getContext().getResources().getColor(R.color.material_grey_700), PorterDuff.Mode.SRC_IN));
        }else{
            mDrawable = ContextCompat.getDrawable(App.getContext(),R.drawable.like_icon_filled);
            mDrawable.setColorFilter(new
                    PorterDuffColorFilter(App.getContext().getResources().getColor(R.color.accent), PorterDuff.Mode.SRC_IN));
        }
        likeButton.setImageDrawable(mDrawable);
    }

    private void set_pins_button_color(boolean pinned){
        Drawable mDrawable;
        if(!pinned){
            mDrawable = ContextCompat.getDrawable(App.getContext(),R.drawable.pin_outline);
            mDrawable.setColorFilter(new
                    PorterDuffColorFilter(App.getContext().getResources().getColor(R.color.material_grey_700), PorterDuff.Mode.SRC_IN));
        }else{
            mDrawable = ContextCompat.getDrawable(App.getContext(),R.drawable.pin_outline);
            mDrawable.setColorFilter(new
                    PorterDuffColorFilter(App.getContext().getResources().getColor(R.color.accent), PorterDuff.Mode.SRC_IN));
        }
        pin.setImageDrawable(mDrawable);
    }

    private void set_likes_count(long count){
        if(count == 0){
            likes_count.setText("");
        }else{
            likes_count.setText(Utility.reduceCountToString(count)
                    + (count>1?App.getContext().getResources().getString(R.string.likes):App.getContext().getResources().getString(R.string.like)));
        }
    }

    private void set_comments_count(){
        if(userPosts.getComments_count() == 0){
            comments_count.setText("");
        }else{
            comments_count.setText(Utility.reduceCountToString(userPosts.getComments_count()));
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.smallBangView:
                if(PreferenceSettings.isUserLoggedIn() && NetworkUtil.hasConnection(App.getContext())){
                    if(userPosts.isLiked()){
                        userPosts.setLikes_count(userPosts.getLikes_count()-1);
                        userPosts.setLiked(false);
                        userPostsListener.likePost(userPosts,"unlike", adapterPosition);
                    }else{
                        userPosts.setLikes_count(userPosts.getLikes_count()+1);
                        userPosts.setLiked(true);
                        smallBangView.likeAnimation(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                            }
                        });
                        userPostsListener.likePost(userPosts,"like", adapterPosition);
                    }
                    set_likes_count(userPosts.getLikes_count());
                    set_likes_button_color(userPosts.isLiked());

                }else{
                    Toast.makeText(App.getContext(), App.getContext().getResources().getText(R.string.post_reactions_error_hint),Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.pin:
                if(PreferenceSettings.isUserLoggedIn() && NetworkUtil.hasConnection(App.getContext())){
                    if(userPosts.isPinned()){
                        userPosts.setPinned(false);
                        userPostsListener.pinPost(userPosts,"unpin");
                    }else{
                        userPosts.setPinned(true);
                        userPostsListener.pinPost(userPosts,"pin");
                    }
                    set_pins_button_color(userPosts.isPinned());
                }else{
                    Toast.makeText(App.getContext(), App.getContext().getResources().getString(R.string.pin_post_eeror_hint),Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.comments_layout: case R.id.commentButton:
                userPostsListener.startCommentsActivity(userPosts, adapterPosition);
                break;
            case R.id.options:
                showPopupMenu();
                break;
            case R.id.name:
                UserData userData = new UserData();
                userData.setEmail(userPosts.getEmail());
                userData.setName(userPosts.getName());
                userData.setAvatar(userPosts.getAvatar());
                userData.setCover_photo(userPosts.getCover_photo());
                userPostsListener.view_profile(userData);
                break;
            case R.id.likes_count:
                userPostsListener.view_likes(userPosts);
                break;
        }
    }

    private void showPopupMenu(){
        PopupMenu popup = new PopupMenu(userPostsListener.getPostActivity(), options);
        Menu menu = popup.getMenu();
        menu.add(0, R.id.edit_post, Menu.FIRST, App.getContext().getResources().getString(R.string.edit_post));
        menu.add(0, R.id.delete_post, Menu.FIRST, App.getContext().getResources().getString(R.string.delete_post));
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            switch(id){
                case R.id.edit_post:
                    userPostsListener.editPost(userPosts,adapterPosition);
                    break;
                case R.id.delete_post:
                    userPostsListener.deletePost(userPosts,adapterPosition);
                    break;
            }
            return true;
        });

        popup.show();
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

    private class MediaPagerAdapter extends FragmentStateAdapter {
        List<String> mediaList = new ArrayList<>();

        public MediaPagerAdapter(FragmentActivity fa, List<String> mediaList) {
            super(fa);
            this.mediaList = mediaList;
        }

        @NotNull
        @Override
        public Fragment createFragment(int position) {
            String file = mediaList.get(position);
            String ext = getFileExtension(file);
            if(ext.equalsIgnoreCase("mp4")){
                return PostVideoFragment.newInstance(file, adapterPosition);
            }else{
                return PostImageFragment.newInstance(file);
            }

        }

        @Override
        public int getItemCount() {
            return mediaList.size();
        }
    }

}
