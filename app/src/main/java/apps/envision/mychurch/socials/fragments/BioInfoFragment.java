package apps.envision.mychurch.socials.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import apps.envision.mychurch.R;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.utils.JsonParser;
import apps.envision.mychurch.utils.Utility;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BioInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BioInfoFragment extends Fragment implements LocalMessageCallback {

    private static final String ARG_PARAM1 = "param1";
    private UserData userData, thisUser = null;
    private TextView about, dateofbirth, gender, phone, email, location,
                     qualification, facebook, twitter, linkdln;

    public BioInfoFragment() {
        // Required empty public constructor
    }

    public static BioInfoFragment newInstance(String user) {
        BioInfoFragment fragment = new BioInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Gson gson = new Gson();
            userData = gson.fromJson(getArguments().getString(ARG_PARAM1), UserData.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.bioinfo, container, false);
        thisUser = PreferenceSettings.getUserData();

        about = view.findViewById(R.id.about);
        dateofbirth = view.findViewById(R.id.dateofbirth);
        gender = view.findViewById(R.id.gender);
        phone = view.findViewById(R.id.phone);
        email = view.findViewById(R.id.email);
        location = view.findViewById(R.id.location);
        qualification = view.findViewById(R.id.qualification);
        facebook = view.findViewById(R.id.facebook);
        twitter = view.findViewById(R.id.twitter);
        linkdln = view.findViewById(R.id.linkdln);

        setUserInfo();
        getUserInfo();
        return view;
    }

    private void setUserInfo(){
        if(userData!=null){
            if(userData.getAbout_me().equalsIgnoreCase("")){
                about.setText("---");
            }else{
                about.setText(Utility.getBase64DEcodedString(userData.getAbout_me()));
            }
            if(userData.getDate_of_birth().equalsIgnoreCase("")){
                dateofbirth.setText("---");
            }else{
                dateofbirth.setText(userData.getDate_of_birth());
            }
            if(userData.getGender().equalsIgnoreCase("")){
                gender.setText("---");
            }else{
                gender.setText(userData.getGender());
            }
            if(userData.getPhone().equalsIgnoreCase("")){
                phone.setText("---");
            }else{
                phone.setText(userData.getPhone());
            }
            if(userData.getEmail().equalsIgnoreCase("")){
                email.setText("---");
            }else{
                email.setText(userData.getEmail());
            }

            if(userData.getLocation().equalsIgnoreCase("")){
                location.setText("---");
            }else{
                location.setText(userData.getLocation());
            }
            if(userData.getQualification().equalsIgnoreCase("")){
                qualification.setText("---");
            }else{
                qualification.setText(userData.getQualification());
            }
            if(userData.getFacebook().equalsIgnoreCase("")){
                facebook.setText("---");
            }else{
                facebook.setText(userData.getFacebook());
            }
            if(userData.getTwitter().equalsIgnoreCase("")){
                twitter.setText("---");
            }else{
                twitter.setText(userData.getTwitter());
            }
            if(userData.getLinkdln().equalsIgnoreCase("")){
                linkdln.setText("---");
            }else{
                linkdln.setText(userData.getLinkdln());
            }
        }
    }

    private void getUserInfo(){
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("email",userData.getEmail());
            jsonData.put("viewer", PreferenceSettings.getUserData().getEmail());

            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = service.userBioInfo(requestBody);

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
                            userData = JsonParser.getUpdatedUser(jsonObj);
                            setUserInfo();
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

    @Override
    public void onStart() {
        super.onStart();
        LocalMessageManager.getInstance().addListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalMessageManager.getInstance().removeListener(this);
    }

    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
        if(localMessage.getId() == R.id.update_userdata){
            UserData userData = PreferenceSettings.getUserData();
            if(userData!=null && userData.getEmail().equalsIgnoreCase(thisUser.getEmail())){
                this.userData = userData;
                setUserInfo();
            }
        }
    }
}
