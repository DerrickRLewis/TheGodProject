package apps.envision.mychurch.socials;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.socials.adapters.SectionsPagerAdapter;
import apps.envision.mychurch.utils.CustomViewPager;
import apps.envision.mychurch.utils.ImageLoader;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SocialActivity extends AppCompatActivity implements LocalMessageCallback {

    private AppCompatEditText search_field;
    private UserData userData;
    private MeowBottomNavigation bottomNavigation;
    private CustomViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social);
        userData = PreferenceSettings.getUserData();
        findViewById(R.id.finish_activity).setOnClickListener(v -> finish());
        ImageView profile = findViewById(R.id.profile);
        ImageView user_avatar = findViewById(R.id.user_avatar);
        if(PreferenceSettings.isUserLoggedIn() && userData !=null){
            ImageLoader.loadUserAvatar(profile, userData.getAvatar());
            ImageLoader.loadUserAvatar(user_avatar, userData.getAvatar());
        }else{
            Toast.makeText(App.getContext(), getString(R.string.logged_in_error_hint), Toast.LENGTH_SHORT).show();
            finish();
        }

        RelativeLayout input_post_layout = findViewById(R.id.input_post_layout);
        RelativeLayout search_people = findViewById(R.id.search_people);
        search_field = findViewById(R.id.search_field);
        search_field.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    search_users(v);
                }
                return false;
            }
        });



        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(4);
        viewPager.setPagingEnabled(false);
        viewPager.setAdapter(sectionsPagerAdapter);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                Gson gson = new Gson();
                String myJson = gson.toJson(userData);
                intent = new Intent(SocialActivity.this, UsersProfileActivity.class);
                intent.putExtra("userdata", myJson);
                startActivity(intent);
            }
        });

        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.ic_home));
        bottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.ic_message));
        bottomNavigation.add(new MeowBottomNavigation.Model(3, R.drawable.ic_account));
        bottomNavigation.add(new MeowBottomNavigation.Model(4, R.drawable.ic_notification));
        bottomNavigation.add(new MeowBottomNavigation.Model(5, R.drawable.ic_explore));

        bottomNavigation.setOnClickMenuListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {
                switch (model.getId()){
                    case 1:
                        input_post_layout.setVisibility(View.VISIBLE);
                        search_people.setVisibility(View.GONE);
                        viewPager.setCurrentItem(0);
                        break;
                    case 2:
                        input_post_layout.setVisibility(View.GONE);
                        search_people.setVisibility(View.GONE);
                        viewPager.setCurrentItem(1);
                        break;
                    case 3:
                        input_post_layout.setVisibility(View.GONE);
                        search_people.setVisibility(View.VISIBLE);
                        viewPager.setCurrentItem(2);
                        break;
                    case 4:
                        input_post_layout.setVisibility(View.GONE);
                        search_people.setVisibility(View.GONE);
                        viewPager.setCurrentItem(3);
                        updateUsersSeenNotifications();
                        bottomNavigation.clearCount(4);
                        break;
                    case 5:
                        input_post_layout.setVisibility(View.GONE);
                        search_people.setVisibility(View.GONE);
                        viewPager.setCurrentItem(4);
                        break;
                }
                return null;
            }
        });

        bottomNavigation.setOnReselectListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {
                switch (model.getId()){
                    case 1:
                        LocalMessageManager.getInstance().send(R.id.scroll_posts_to_top);
                        break;
                }
                return null;
            }
        });

        bottomNavigation.setOnShowListener(model -> {
            // YOUR CODES
            return null;
        });

        if(getIntent().getStringExtra("notify")!=null) {
            bottomNavigation.show(4,true);
            input_post_layout.setVisibility(View.GONE);
            viewPager.setCurrentItem(3);
        }else{
            bottomNavigation.show(1,true);
            getUserNotificationCount();
        }

        if(userData!=null){
            App.updateUserSocialToken(userData.getEmail());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(!PreferenceSettings.isUserLoggedIn() || userData ==null){
            Toast.makeText(App.getContext(), getString(R.string.logged_in_error_hint), Toast.LENGTH_SHORT).show();
            finish();
        }
        super.onNewIntent(intent);

    }

    public void new_post(View v){
        startActivity(new Intent(SocialActivity.this, NewPostActivity.class));
    }

    public void search_users(View v){
        String query = search_field.getText().toString().trim();
        if(!query.equalsIgnoreCase("")){
            findViewById(R.id.cancel_users_search).setVisibility(View.VISIBLE);
            findViewById(R.id.search_users).setVisibility(View.GONE);
            LocalMessageManager.getInstance().send(R.id.search_people,query);
        }
    }

    public void search_users_cancel(View v){
        findViewById(R.id.cancel_users_search).setVisibility(View.GONE);
        findViewById(R.id.search_users).setVisibility(View.VISIBLE);
        search_field.setText("");
        LocalMessageManager.getInstance().send(R.id.search_people_cancel);
    }

    private void getUserNotificationCount(){
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("email", userData.getEmail());
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = service.getUnSeenNotifications(requestBody);

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response", String.valueOf(response.body()));
                    if(response.body()==null){
                        return;
                    }

                    try {
                        JSONObject jsonObj = new JSONObject(response.body());
                        String status = jsonObj.getString("status");
                        if(status.equalsIgnoreCase("ok")){
                            int count = jsonObj.getInt("count");
                            if(count!=0){
                                String total_unseen = String.valueOf(count);
                                if(count>9){
                                    total_unseen = "9+";
                                }
                                bottomNavigation.setCount(4,total_unseen);
                                bottomNavigation.setCountBackgroundColor(getResources().getColor(R.color.colorAccent));
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("Error", e.toString());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    Log.e("error", String.valueOf(throwable.getMessage()));
                }
            });
        }catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void updateUsersSeenNotifications(){
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("email", userData.getEmail());
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = service.setSeenNotifications(requestBody);

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response", String.valueOf(response.body()));
                    if(response.body()==null){
                        return;
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    Log.e("error", String.valueOf(throwable.getMessage()));
                }
            });
        }catch (JSONException e) {
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
        if(localMessage.getId() == R.id.reload_notifications){
            if(viewPager.getCurrentItem()!=3){
                getUserNotificationCount();
            }
        }
    }
}