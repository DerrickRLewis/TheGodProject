package apps.envision.mychurch.socials;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.yashoid.instacropper.InstaCropperActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import apps.envision.mychurch.R;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.libs.frisson.FrissonView;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.utils.FileUtils;
import apps.envision.mychurch.utils.ImageLoader;
import apps.envision.mychurch.utils.JsonParser;
import apps.envision.mychurch.utils.Utility;
import gun0912.tedimagepicker.builder.TedImagePicker;
import gun0912.tedimagepicker.builder.type.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText full_name, date_of_birth, phone,
            location, qualification, about_me, facebook, twitter, linkedln;
    private RadioGroup gender;

    private String _avatar = "", _cover_photo = "";
    private UserData userData;
    private FrissonView cover_photo;
    private ImageView avatar;
    private Utility utility;
    private Uri avatar_uri, cover_photo_uri;

    private int state = 0;
    private boolean onboarding = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        userData = PreferenceSettings.getUserData();
        utility = new Utility(this);

        if(getIntent().getStringExtra("onboarding")!=null){
            onboarding = true;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.edit_profile));

        cover_photo = findViewById(R.id.cover_photo);
        avatar = findViewById(R.id.avatar);
        full_name = findViewById(R.id.full_name);
        date_of_birth = findViewById(R.id.date_of_birth);
        phone = findViewById(R.id.phone);
        location = findViewById(R.id.location);
        qualification = findViewById(R.id.qualification);
        about_me = findViewById(R.id.about_me);
        facebook = findViewById(R.id.facebook);
        twitter = findViewById(R.id.twitter);
        linkedln = findViewById(R.id.linkedln);
        gender = (RadioGroup) findViewById(R.id.gender);
        RadioButton male = findViewById(R.id.male);
        RadioButton female = findViewById(R.id.female);

        if(userData!=null){
            full_name.setText(userData.getName());
            date_of_birth.setText(userData.getDate_of_birth());
            phone.setText(userData.getPhone());
            location.setText(userData.getLocation());
            qualification.setText(userData.getQualification());
            about_me.setText(userData.getAbout_me());
            facebook.setText(userData.getFacebook());
            twitter.setText(userData.getTwitter());
            linkedln.setText(userData.getLinkdln());

            if(!userData.getGender().equalsIgnoreCase("") && userData.getGender().equalsIgnoreCase("male")){
                male.setChecked(true);
            }
            if(!userData.getGender().equalsIgnoreCase("") && userData.getGender().equalsIgnoreCase("female")){
                female.setChecked(true);
            }

            if(!userData.getAvatar().equalsIgnoreCase("")){
                _avatar = userData.getAvatar();
                ImageLoader.loadUserAvatar(avatar,_avatar);
            }

            if(!userData.getCover_photo().equalsIgnoreCase("")){
                _cover_photo = userData.getCover_photo();
                ImageLoader.loadCoverPhotoImage(cover_photo,_cover_photo);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            case R.id.done:
                // app icon in action bar clicked; goto parent activity.
                get_input_and_submit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void get_input_and_submit(){

        String _full_name = full_name.getText().toString().trim();
        String _date_of_birth = date_of_birth.getText().toString().trim();
        String _phone = phone.getText().toString().trim();
        String _location = location.getText().toString().trim();
        String _qualification = qualification.getText().toString().trim();
        String _about_me = about_me.getText().toString().trim();
        String _facebook = facebook.getText().toString().trim();
        String _twitter = twitter.getText().toString().trim();
        String _linkedln = linkedln.getText().toString().trim();
        String _gender = "";

        int selectedRadioButtonID = gender.getCheckedRadioButtonId();
        if (selectedRadioButtonID != -1) {
            RadioButton selectedRadioButton = (RadioButton) findViewById(selectedRadioButtonID);
            _gender = selectedRadioButton.getText().toString();
        }

        if(_full_name.equalsIgnoreCase("") || _gender.equalsIgnoreCase("")
                || (_avatar.equalsIgnoreCase("") && avatar_uri==null)
                || _date_of_birth.equalsIgnoreCase("") || _location.equalsIgnoreCase("")){
            utility.show_alert(getString(R.string.error),getString(R.string.required_fields));
        }else{
           updateUserProfileOnServer(_full_name, _date_of_birth, _phone, _gender,
                   _location, _qualification, _about_me, _facebook,_twitter, _linkedln);
        }
    }

    public void pickAvatar(View v) {
        state = 0;
       /* Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);*/
        TedImagePicker.with(this)
                .mediaType(MediaType.IMAGE)
                .start(uri -> {
                    File file = new File(uri.getPath());
                    //Intent intent = InstaCropperActivity.getIntent(this, data.getData(), Uri.fromFile(new File(getExternalCacheDir(), "test.jpg")), 720, 50);
                    Intent intent = InstaCropperActivity.getIntent(this, Uri.fromFile(file), Uri.fromFile(file), 720, 50);
                    startActivityForResult(intent, 2);
                });
    }

    public void pickCoverPhoto(View v) {
        state = 1;
        /*Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);*/
        //new GligarPicker().requestCode(1).limit(1).withActivity(this).show();
        TedImagePicker.with(this)
                .mediaType(MediaType.IMAGE)
                .start(uri -> {
                    File file = new File(uri.getPath());
                    //Intent intent = InstaCropperActivity.getIntent(this, data.getData(), Uri.fromFile(new File(getExternalCacheDir(), "test.jpg")), 720, 50);
                    Intent intent = InstaCropperActivity.getIntent(this, Uri.fromFile(file), Uri.fromFile(file), 720, 50);
                    startActivityForResult(intent, 2);
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
              /*  if (resultCode == RESULT_OK) {
                    String pathsList[]= data.getExtras().getStringArray(GligarPicker.IMAGES_RESULT); // return list of selected images paths.
                    if(pathsList.length>0){
                        File file = new File(pathsList[0]);
                        //Intent intent = InstaCropperActivity.getIntent(this, data.getData(), Uri.fromFile(new File(getExternalCacheDir(), "test.jpg")), 720, 50);
                        Intent intent = InstaCropperActivity.getIntent(this, Uri.fromFile(file), Uri.fromFile(file), 720, 50);
                        startActivityForResult(intent, 2);
                    }
                }*/
                return;
            case 2:
                if (resultCode == RESULT_OK) {
                   // File file = FileUtils.getFile(EditProfileActivity.this, data.getData());
                    Log.e("image",String.valueOf(data.getData()));
                    if(state==0){
                        avatar_uri = data.getData();
                        avatar.setImageURI(data.getData());
                    }else{
                        cover_photo_uri = data.getData();
                        ImageLoader.loadCoverPhotoImage(cover_photo,data.getData());
                    }
                }
                return;
        }
    }

    private void updateUserProfileOnServer(String _full_name,String _date_of_birth,String _phone,String _gender,
                                           String _location,String _qualification,String _about_me,
                                           String _facebook,String _twitter,String _linkedln){
        utility.show_loader();
        // create upload service client
        NetworkService service = StringApiClient.createService(NetworkService.class);
        MultipartBody.Part body1 = null;
        if(avatar_uri!=null){
            body1 = FileUtils.prepareFilePart("avatar", avatar_uri);
        }
        MultipartBody.Part body2 = null;
        if(cover_photo_uri!=null){
            body2 = FileUtils.prepareFilePart("cover_photo", cover_photo_uri);
        }

        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("email", FileUtils.createPartFromString(userData.getEmail()));
        map.put("fullname", FileUtils.createPartFromString(_full_name));
        map.put("date_of_birth", FileUtils.createPartFromString(_date_of_birth));
        map.put("phone", FileUtils.createPartFromString(_phone));
        map.put("gender", FileUtils.createPartFromString(_gender));
        map.put("location", FileUtils.createPartFromString(_location));
        map.put("qualification", FileUtils.createPartFromString(_qualification));
        map.put("about_me", FileUtils.createPartFromString(Utility.getBase64EncodedString(_about_me)));
        map.put("facebook", FileUtils.createPartFromString(_facebook));
        map.put("twitter", FileUtils.createPartFromString(_twitter));
        map.put("linkedln", FileUtils.createPartFromString(_linkedln));
        map.put("notify_token", FileUtils.createPartFromString(PreferenceSettings.getFcmToken()));
        Log.e("body", String.valueOf(map));

        // finally, execute the request
        Call<String> call = service.editUserProfile(body1,body2, map);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                utility.hide_loader();
                Log.e("response", String.valueOf(response.body()));
                if (response.body() == null) {
                    utility.show_alert(getString(R.string.error),getString(R.string.error_occured_updating_user));
                    return;
                }
                try {
                    JSONObject jsonObj = new JSONObject(response.body());
                    String status = jsonObj.getString("status");
                    String msg = jsonObj.getString("msg");
                    if(status.equalsIgnoreCase("ok")){
                        PreferenceSettings.setUserData(JsonParser.getUpdatedUser(jsonObj));
                       if(onboarding){
                           Intent intent = new Intent(EditProfileActivity.this,FollowUsersActivity.class);
                           intent.putExtra("onboarding", "true_");
                           startActivity(intent);
                       }else{
                           LocalMessageManager.getInstance().send(R.id.update_userdata);
                       }
                       finish();
                    }else{
                        utility.show_alert(getString(R.string.error),msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                utility.hide_loader();
                Log.e("error",String.valueOf(t.getLocalizedMessage()));
                utility.show_alert(getString(R.string.error),t.getLocalizedMessage());
            }
        });
    }
}
