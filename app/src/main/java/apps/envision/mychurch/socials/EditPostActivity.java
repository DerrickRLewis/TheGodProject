package apps.envision.mychurch.socials;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.gson.Gson;
import com.yashoid.instacropper.InstaCropperActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import apps.envision.mychurch.R;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.libs.post_worker.PostWorker;
import apps.envision.mychurch.pojo.MakePosts;
import apps.envision.mychurch.pojo.Uploads;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.pojo.UserPosts;
import apps.envision.mychurch.socials.adapters.UploadsAdapter;
import apps.envision.mychurch.utils.FileManager;
import apps.envision.mychurch.utils.Utility;
import gun0912.tedimagepicker.builder.TedImagePicker;
import gun0912.tedimagepicker.builder.type.MediaType;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditPostActivity extends AppCompatActivity {

    private AppCompatEditText text;
    private RadioGroup visibility;
    private UserData userData;
    private Utility utility;
    private String _visibility = "";
    RadioButton _public;
    RadioButton _private;
    private MenuItem done;
    private UserPosts userPosts;
    private int post_position = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        userData = PreferenceSettings.getUserData();
        utility = new Utility(this);

        if(getIntent().getStringExtra("post")!=null) {
            Gson gson = new Gson();
            userPosts = gson.fromJson(getIntent().getStringExtra("post"), UserPosts.class);
            post_position = getIntent().getIntExtra("post_position",-1);
        }else{
            finish();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.new_post);

        visibility = findViewById(R.id.visibility);
        _public = findViewById(R.id._public);
        _private = findViewById(R.id._private);
        text = findViewById(R.id.text);
        findViewById(R.id.attachments_layout).setVisibility(View.GONE);
        set_existing_post_data();
    }

    private void set_existing_post_data(){
        if(userPosts!=null){
            text.setText(Utility.getBase64DEcodedString(userPosts.getContent()));
            if(userPosts.getVisibility().equalsIgnoreCase("public")){
                _public.setChecked(true);
                _private.setChecked(false);
            }else{
                _private.setChecked(true);
                _public.setChecked(false);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_profile, menu);
        done = menu.findItem(R.id.done);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                finish();
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
        String _text = text.getText().toString().trim();
        int selectedRadioButtonID = visibility.getCheckedRadioButtonId();
        if (selectedRadioButtonID != -1) {
            RadioButton selectedRadioButton = (RadioButton) findViewById(selectedRadioButtonID);
            _visibility = selectedRadioButton.getText().toString();
        }

        if(_text.equalsIgnoreCase("")){
            return;
        }
        edit_comment(_text, _visibility);
    }

    /**
     * construct and send a new comment to the server
     */
    private void edit_comment(String comment, String visibility){
        try {
            utility.show_loader();
            JSONObject jsonData = new JSONObject();
            jsonData.put("content", Utility.getBase64EncodedString(comment));
            jsonData.put("visibility", visibility);
            jsonData.put("id", userPosts.getId());
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = service.editpost(requestBody);

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response", String.valueOf(response.body()));
                    utility.hide_loader();
                    if(response.body()==null){
                        utility.show_alert(getString(R.string.error), getString(R.string.edit_post_error_hint));
                        return;
                    }

                    try {
                        JSONObject jsonObj = new JSONObject(response.body());
                        String status = jsonObj.getString("status");
                        if (status.equalsIgnoreCase("ok")) {
                            userPosts.setContent(Utility.getBase64EncodedString(comment));
                            userPosts.setAdapterPosition(post_position);
                            LocalMessageManager.getInstance().send(R.id.update_edited_post, userPosts);
                            utility.show_alert(getString(R.string.success),jsonObj.getString("message"));
                        }else{
                            utility.show_alert(getString(R.string.error), getString(R.string.edit_post_error_hint));
                        }
                    } catch (JSONException e) {
                        Log.e("Error", e.toString());
                        utility.show_alert(getString(R.string.error), getString(R.string.edit_post_error_hint));
                    }

                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    utility.hide_loader();
                    utility.show_alert(getString(R.string.error), getString(R.string.edit_post_error_hint));
                    Log.e("error", String.valueOf(throwable.getMessage()));
                }
            });
        }catch (JSONException e) {
            utility.hide_loader();
            utility.show_alert(getString(R.string.error), getString(R.string.edit_post_error_hint));
        }

    }
}
