package apps.envision.mychurch.socials;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;

import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.pojo.UserPosts;
import apps.envision.mychurch.socials.fragments.PostLikesFragment;
import apps.envision.mychurch.socials.fragments.UserDetailedPostsFragment;
import apps.envision.mychurch.socials.fragments.UserFollowersFollowingFragment;
import apps.envision.mychurch.socials.fragments.UserPinsFragment;

public class UsersDataActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userdata);

        Gson gson = new Gson();
        UserData userData = gson.fromJson(getIntent().getStringExtra("userdata"), UserData.class);
        UserPosts userPosts = gson.fromJson(getIntent().getStringExtra("userpost"), UserPosts.class);
        OPTIONS options = gson.fromJson(getIntent().getStringExtra("option"), OPTIONS.class);
        if(userData ==null && userPosts==null){
            finish();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.anim_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        switch (options){
            case POSTS:
                UserData thisUser3 = PreferenceSettings.getUserData();
                if(thisUser3 != null && thisUser3.getEmail().equalsIgnoreCase(userData.getEmail())){
                    getSupportActionBar().setTitle(getString(R.string.my)+" "+getString(R.string.posts));
                }else{
                    getSupportActionBar().setTitle(userData.getName() +" "+ getString(R.string.posts));
                }

                setFragment(UserDetailedPostsFragment.newInstance(new Gson().toJson(userData)));
                break;
            case FOLLOWERS:
                UserData thisUser = PreferenceSettings.getUserData();
                if(thisUser != null && thisUser.getEmail().equalsIgnoreCase(userData.getEmail())){
                    getSupportActionBar().setTitle(getString(R.string.my)+" "+getString(R.string.followers));
                }else{
                    getSupportActionBar().setTitle(userData.getName() +" "+ getString(R.string.followers));
                }
                setFragment(UserFollowersFollowingFragment.newInstance(userData.getEmail(),"followers"));
                break;
            case FOLLOWINGS:
                UserData thisUser2 = PreferenceSettings.getUserData();
                if(thisUser2 != null && thisUser2.getEmail().equalsIgnoreCase(userData.getEmail())){
                    getSupportActionBar().setTitle(getString(R.string.my)+" "+getString(R.string.following));
                }else{
                    getSupportActionBar().setTitle(userData.getName() +" "+ getString(R.string.following));
                }
                setFragment(UserFollowersFollowingFragment.newInstance(userData.getEmail(),"following"));
                break;
            case PINS:
                getSupportActionBar().setTitle(R.string.my_pinned_posts);
                setFragment(UserPinsFragment.newInstance());
                break;
            case LIKES:
                getSupportActionBar().setTitle(R.string.likes);
                setFragment(PostLikesFragment.newInstance(new Gson().toJson(userPosts)));
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    protected void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }

    public enum OPTIONS{
        POSTS, FOLLOWERS, FOLLOWINGS, PINS, LIKES;
    }
}
