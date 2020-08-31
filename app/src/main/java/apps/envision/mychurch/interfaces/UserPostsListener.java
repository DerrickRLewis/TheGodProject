package apps.envision.mychurch.interfaces;


import android.app.Activity;

import androidx.fragment.app.FragmentActivity;

import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.pojo.UserPosts;

/**
 * Created by Ray on 6/25/2018.
 */

public interface UserPostsListener {
    FragmentActivity getPostActivity();
    void likePost(UserPosts userPosts, String action, int position);
    void pinPost(UserPosts userPosts, String action);
    void startCommentsActivity(UserPosts userPosts, int position);
    void editPost(UserPosts userPosts, int position);
    void deletePost(UserPosts userPosts, int position);
    void view_profile(UserData userData);
    void view_likes(UserPosts userPosts);
}
