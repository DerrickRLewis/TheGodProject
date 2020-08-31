package apps.envision.mychurch.socials.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import apps.envision.mychurch.App;
import apps.envision.mychurch.BuildConfig;
import apps.envision.mychurch.R;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.socials.SocialActivity;
import apps.envision.mychurch.socials.UsersDataActivity;
import apps.envision.mychurch.socials.UsersProfileActivity;
import apps.envision.mychurch.ui.activities.LoginActivity;
import apps.envision.mychurch.ui.activities.RegisterActivity;
import apps.envision.mychurch.ui.activities.SubscriptionActivity;
import apps.envision.mychurch.utils.ImageLoader;
import apps.envision.mychurch.utils.TimUtil;
import apps.envision.mychurch.utils.Utility;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static apps.envision.mychurch.utils.Constants.Terms_URL;

/**
 * Fragment to load some basic app settings.
 */
public class SocialSettingsFragment extends Fragment implements View.OnClickListener{

    private View view;
    private UserData userData;
    private AppCompatCheckBox phone_toggle, dob_toggle, follow_notify_toggle, comment_notify_toggle, like_notify_toggle;
    private Utility utility;

    public static SocialSettingsFragment newInstance() {
       return new SocialSettingsFragment();
    }

    /**
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_social_settings, container, false);
        utility = new Utility(getActivity());
        userData = PreferenceSettings.getUserData();

        init_views();
        fetch_user_settings();
        return view;
    }

    //init views and set click listeners
    private void init_views(){
        ImageView user_avatar = view.findViewById(R.id.user_avatar);
        TextView name = view.findViewById(R.id.name);
        if(userData !=null){
            ImageLoader.loadUserAvatar(user_avatar, userData.getAvatar());
            name.setText(userData.getName());
        }


        view.findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                Gson gson = new Gson();
                String myJson = gson.toJson(userData);
                intent = new Intent(getActivity(), UsersProfileActivity.class);
                intent.putExtra("userdata", myJson);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.my_pins).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                Gson gson = new Gson();
                String userdata = gson.toJson(userData);
                String option = gson.toJson(UsersDataActivity.OPTIONS.PINS);
                intent = new Intent(getActivity(), UsersDataActivity.class);
                intent.putExtra("userdata", userdata);
                intent.putExtra("option", option);
                startActivity(intent);
            }
        });

        phone_toggle = view.findViewById(R.id.phone_toggle);
        dob_toggle = view.findViewById(R.id.dob_toggle);
        follow_notify_toggle = view.findViewById(R.id.follow_notify_toggle);
        comment_notify_toggle = view.findViewById(R.id.comment_notify_toggle);
        like_notify_toggle = view.findViewById(R.id.like_notify_toggle);

        view.findViewById(R.id.update_settings).setOnClickListener(this);

        view.findViewById(R.id.phone_layout).setOnClickListener(this);
        view.findViewById(R.id.dob_layout).setOnClickListener(this);
        view.findViewById(R.id.follow_layout).setOnClickListener(this);
        view.findViewById(R.id.comment_layout).setOnClickListener(this);
        view.findViewById(R.id.like_layout).setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.update_settings:
                update_user_settings();
                break;
            case R.id.phone_layout: case R.id.phone_toggle:
                if(phone_toggle.isChecked()){
                    phone_toggle.setChecked(false);
                }else{
                    phone_toggle.setChecked(true);
                }
                break;
            case R.id.dob_layout: case R.id.dob_toggle:
                if(dob_toggle.isChecked()){
                    dob_toggle.setChecked(false);
                }else{
                    dob_toggle.setChecked(true);
                }
                break;
            case R.id.follow_layout: case R.id.follow_notify_toggle:
                if(follow_notify_toggle.isChecked()){
                    follow_notify_toggle.setChecked(false);
                }else{
                    follow_notify_toggle.setChecked(true);
                }
                break;
            case R.id.comment_layout: case R.id.comment_notify_toggle:
                if(comment_notify_toggle.isChecked()){
                    comment_notify_toggle.setChecked(false);
                }else{
                    comment_notify_toggle.setChecked(true);
                }
                break;
            case R.id.like_layout: case R.id.like_notify_toggle:
                if(like_notify_toggle.isChecked()){
                    like_notify_toggle.setChecked(false);
                }else{
                    like_notify_toggle.setChecked(true);
                }
                break;
        }
    }

    public void fetch_user_settings() {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("email",userData.getEmail());

            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);
            Call<String> callAsync = service.fetch_user_settings(requestBody);
            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response", String.valueOf(response.body()));
                    if (response.body() == null) {
                        return;
                    }
                    try {
                        JSONObject jsonObj = new JSONObject(response.body());
                        JSONObject user = jsonObj.getJSONObject("user");
                        dob_toggle.setChecked(user.getInt("show_dateofbirth") == 0);
                        phone_toggle.setChecked(user.getInt("show_phone") == 0);
                        follow_notify_toggle.setChecked(user.getInt("notify_follows") == 0);
                        comment_notify_toggle.setChecked(user.getInt("notify_comments") == 0);
                        like_notify_toggle.setChecked(user.getInt("notify_likes") == 0);

                    } catch (JSONException e) {
                        Log.e("Error", e.toString());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                }
            });
        }catch (JSONException e) {
            Log.e("parse error",e.getMessage());
            e.printStackTrace();
        }
    }

    public void update_user_settings() {
        try {
            utility.show_loader();
            JSONObject jsonData = new JSONObject();
            jsonData.put("email",userData.getEmail());
            jsonData.put("show_dateofbirth",dob_toggle.isChecked()?0:1);
            jsonData.put("show_phone",phone_toggle.isChecked()?0:1);
            jsonData.put("notify_follows",follow_notify_toggle.isChecked()?0:1);
            jsonData.put("notify_comments",comment_notify_toggle.isChecked()?0:1);
            jsonData.put("notify_likes",like_notify_toggle.isChecked()?0:1);

            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);
            Call<String> callAsync = service.update_user_settings(requestBody);
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
                        if(status.equalsIgnoreCase("ok")){
                            utility.show_alert(getString(R.string.success),jsonObj.getString("msg"));
                        }else{
                            utility.show_alert(getString(R.string.error),jsonObj.getString("msg"));
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



}

