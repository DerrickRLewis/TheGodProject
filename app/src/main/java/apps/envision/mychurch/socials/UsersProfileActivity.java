package apps.envision.mychurch.socials;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.libs.frisson.FrissonView;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.Categories;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.socials.fragments.BioInfoFragment;
import apps.envision.mychurch.socials.fragments.PeopleFragment;
import apps.envision.mychurch.socials.fragments.UserDetailedPostsFragment;
import apps.envision.mychurch.ui.fragments.CategoriesMediaFragment;
import apps.envision.mychurch.utils.ImageLoader;
import apps.envision.mychurch.utils.JsonParser;
import apps.envision.mychurch.utils.Utility;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static apps.envision.mychurch.utils.Utility.darkenColor;

public class UsersProfileActivity extends AppCompatActivity implements View.OnClickListener, LocalMessageCallback {

    private CollapsingToolbarLayout collapsingToolbar;
    private Utility utility;
    UserData userData = null, thisUser = null;
    private Chip posts;
    private Chip followers;
    private Chip following;
    private Chip bio;
    private TextView follow_unfollow;
    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_profile);
        thisUser = PreferenceSettings.getUserData();
        utility = new Utility(this);

        Gson gson = new Gson();
        userData = gson.fromJson(getIntent().getStringExtra("userdata"), UserData.class);
        if(userData==null){
            finish();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.anim_toolbar);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbar.setTitle("");
        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            if (Math.abs(verticalOffset)- appBarLayout1.getTotalScrollRange() == 0) {
                //  Collapsed
                collapsingToolbar.setTitle(getString(R.string.user_profile));
            } else {
                //Expanded
                collapsingToolbar.setTitle("");
            }
        });
        //appBarLayout.setExpanded(false);
        setProfileDetails();
        getUserInfo();

        posts = findViewById(R.id.posts);
        posts.setOnClickListener(this);
        bio = findViewById(R.id.bio);
        bio.setOnClickListener(this);
        followers = findViewById(R.id.followers);
        followers.setOnClickListener(this);
        following = findViewById(R.id.following);
        following.setOnClickListener(this);

        follow_unfollow = findViewById(R.id.follow_unfollow);
        follow_unfollow.setOnClickListener(this);
        if(thisUser.getEmail().equalsIgnoreCase(userData.getEmail())){
            follow_unfollow.setText(R.string.edit_user_profile);
            follow_unfollow.setVisibility(View.VISIBLE);
        }


        setFragment(BioInfoFragment.newInstance(new Gson().toJson(userData)));
    }

    private void setProfileDetails(){
        TextView name = findViewById(R.id.name);
        name.setText(userData.getName());
        ImageView avatar = findViewById(R.id.avatar);
        avatar.setOnClickListener(this);
        ImageLoader.loadUserAvatar(avatar, userData.getAvatar());
        FrissonView header_img = findViewById(R.id.wave_head);
        header_img.setOnClickListener(this);
        Glide.with(App.getContext())
                .asBitmap().load(userData.getCover_photo())
                .listener(new RequestListener<Bitmap>() {
                              @Override
                              public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Bitmap> target, boolean b) {
                                  //header_img.setImageSource(R.drawable.screen1);
                                  return false;
                              }

                              @Override
                              public boolean onResourceReady(Bitmap bitmap, Object o, Target<Bitmap> target, DataSource dataSource, boolean b) {
                                  header_img.setBitmap(bitmap);
                                  Palette.from(bitmap).generate(palette -> {
                                      if (palette != null) {
                                          int vibrantColor = palette.getVibrantColor(getResources().getColor(R.color.primary));
                                          collapsingToolbar.setContentScrimColor(vibrantColor);
                                          collapsingToolbar.setStatusBarScrimColor(getResources().getColor(R.color.black_trans80));
                                          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                              getWindow().setStatusBarColor(
                                                      darkenColor(vibrantColor));
                                          }
                                      }
                                  });
                                  return false;
                              }
                          }
                ).submit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    protected void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.wave_head:
                utility.showLargeCoverPhoto(userData.getCover_photo());
                break;
            case R.id.avatar:
                utility.showLargeAvatar(userData.getAvatar());
                break;
            case R.id.posts:
                Intent intent;
                Gson gson = new Gson();
                String userdata = gson.toJson(userData);
                String option = gson.toJson(UsersDataActivity.OPTIONS.POSTS);
                intent = new Intent(UsersProfileActivity.this, UsersDataActivity.class);
                intent.putExtra("userdata", userdata);
                intent.putExtra("option", option);
                startActivity(intent);
                break;
            case R.id.bio:
                bio.setChecked(true);
                //setFragment(BioInfoFragment.newInstance(new Gson().toJson(userData)));
                break;
            case R.id.followers:
                Intent intent2;
                Gson gson2 = new Gson();
                String userdata2 = gson2.toJson(userData);
                String option2 = gson2.toJson(UsersDataActivity.OPTIONS.FOLLOWERS);
                intent2 = new Intent(UsersProfileActivity.this, UsersDataActivity.class);
                intent2.putExtra("userdata", userdata2);
                intent2.putExtra("option", option2);
                startActivity(intent2);
                break;
            case R.id.following:
                Intent intent3;
                Gson gson3 = new Gson();
                String userdata3 = gson3.toJson(userData);
                String option3 = gson3.toJson(UsersDataActivity.OPTIONS.FOLLOWINGS);
                intent3 = new Intent(UsersProfileActivity.this, UsersDataActivity.class);
                intent3.putExtra("userdata", userdata3);
                intent3.putExtra("option", option3);
                startActivity(intent3);
                break;
            case R.id.follow_unfollow:
                if(thisUser.getEmail().equalsIgnoreCase(userData.getEmail())){
                    startActivity(new Intent(UsersProfileActivity.this, EditProfileActivity.class));
                }else{
                    follow_unfollow();
                }
                break;
        }
    }

    private void setFollowingStatus(){
        if(isFollowing){
            follow_unfollow.setText(getString(R.string.unfollow));
        }else{
            follow_unfollow.setText(getString(R.string.follow));
        }
        follow_unfollow.setVisibility(View.VISIBLE);
    }

    private void getUserInfo(){
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("email",userData.getEmail());
            jsonData.put("viewer", thisUser.getEmail());

            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = service.userFollowPostCount(requestBody);

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response", String.valueOf(response.body()));
                    if (response.body() == null) {
                        return;
                    }
                    try {
                        JSONObject jsonObj = new JSONObject(response.body());
                        String status = jsonObj.getString("status");
                        if (status.equalsIgnoreCase("ok")) {
                            int post_count = jsonObj.getInt("post_count");
                            int followers_count = jsonObj.getInt("followers_count");
                            int following_count = jsonObj.getInt("following_count");
                            isFollowing = jsonObj.getInt("isFollowing")==0;

                            if(post_count!=0) {
                                posts.setText(getString(R.string.posts) +" ("+post_count +")");
                            }else{
                                posts.setText(getString(R.string.posts));
                            }
                            if(followers_count!=0){
                                followers.setText(getString(R.string.followers) +" ("+followers_count +")");
                            }else{
                                followers.setText(getString(R.string.followers));
                            }
                            if(following_count!=0){
                                following.setText(getString(R.string.following) +" ("+following_count +")");
                            }else{
                                following.setText(getString(R.string.following));
                            }
                            if(!thisUser.getEmail().equalsIgnoreCase(userData.getEmail())){
                               setFollowingStatus();
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("Error", e.toString());

                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    //System.out.println(throwable);
                    Log.e("error", String.valueOf(throwable.getMessage()));

                    //setNetworkError();
                }
            });
        }catch (JSONException e) {
            Log.e("parse error",e.getMessage());
            e.printStackTrace();
        }
    }


    public void follow_unfollow() {
        try {
            utility.show_loader();
            JSONObject jsonData = new JSONObject();
            jsonData.put("user",userData.getEmail());
            jsonData.put("follower",thisUser.getEmail());
            if(isFollowing){
                jsonData.put("action","unfollow");
            }else{
                jsonData.put("action","follow");
            }
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);
            Call<String> callAsync = service.follow_unfollow_user(requestBody);
            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response", String.valueOf(response.body()));
                    utility.hide_loader();
                    if (response.body() == null) {
                        utility.show_alert(getString(R.string.error), getString(R.string.could_not_process_action_hint));
                        return;
                    }
                    try {
                        JSONObject jsonObj = new JSONObject(response.body());
                        String status = jsonObj.getString("status");
                        String action = jsonObj.getString("action");
                        if (status.equalsIgnoreCase("ok")) {
                            isFollowing = action.equalsIgnoreCase("follow");
                            setFollowingStatus();
                        }else{
                            utility.show_alert(getString(R.string.error), getString(R.string.could_not_process_action_hint));
                        }
                    } catch (JSONException e) {
                        Log.e("Error", e.toString());
                        utility.show_alert(getString(R.string.error), getString(R.string.could_not_process_action_hint));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    utility.hide_loader();
                    //System.out.println(throwable);
                    Log.e("error", String.valueOf(throwable.getMessage()));
                    utility.show_alert(getString(R.string.error), getString(R.string.could_not_process_action_hint));
                }
            });
        }catch (JSONException e) {
            Log.e("parse error",e.getMessage());
            utility.hide_loader();
            utility.show_alert(getString(R.string.error), getString(R.string.could_not_process_action_hint));
            e.printStackTrace();
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
    }

    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
        if(localMessage.getId() == R.id.update_userdata){
            UserData userData = PreferenceSettings.getUserData();
            if(userData!=null && userData.getEmail().equalsIgnoreCase(thisUser.getEmail())){
                this.userData = userData;
                setProfileDetails();
            }
        }

        if(localMessage.getId() == R.id.update_userfollow_data){
            getUserInfo();
        }
    }
}
