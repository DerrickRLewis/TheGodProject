package apps.envision.mychurch.ui.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;

import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.utils.JsonParser;
import apps.envision.mychurch.utils.Utility;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private AppCompatEditText email,password;
    private Utility utility;
    private AlertDialog dialog;
    private boolean onboarder = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        utility = new Utility(this);
        if(getIntent().getStringExtra("onboarding")!=null){
            onboarder = true;
        }
        setContentView(R.layout.login_activity);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        findViewById(R.id.signup).setOnClickListener(this);
        findViewById(R.id.forgot_password).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.signup:
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                if(onboarder) {
                    intent.putExtra("onboarding", "true");
                }
                startActivity(intent);
                overridePendingTransition(R.anim.slide_right_out, R.anim.slide_right_in);
                finish();
                break;
            case R.id.forgot_password:
                Intent intent2 = new Intent(LoginActivity.this,ForgotPasswordActivity.class);
                if(onboarder) {
                    intent2.putExtra("onboarding", "true");
                }
                startActivity(intent2);
                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
                finish();
                break;
        }
    }

    public void login_user(View view) {
        String email_ = email.getText().toString().trim();
        String password_ = password.getText().toString();
        if(!utility.emailValidator(email_)){
            utility.show_alert(getString(R.string.error),getString(R.string.invalid_email_hint));
            return;
        }
        if(password_.equalsIgnoreCase("")){
            utility.show_alert(getString(R.string.error),getString(R.string.password_error_hint));
            return;
        }
        loginUser(email_,password_,"","");
    }

    //login user
    private void loginUser(final String email, String password, String name, String type){
        utility.show_loader();
        NetworkService service = StringApiClient.createService(NetworkService.class);
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("email", email);
            jsonData.put("password", password);
            if(!type.equalsIgnoreCase("")) {
                jsonData.put("name",name);
                jsonData.put("type", type);
            }
            jsonData.put("packageName", getPackageName());
            String requestBody = jsonData.toString();
            Log.d("login requestbody",requestBody);

            Call<String> callAsync = service.login(requestBody);
            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response",String.valueOf(response.body()));
                    utility.hide_loader();
                    try {
                        JSONObject res = new JSONObject(response.body());
                        // Add Your Logic
                        if(res.getString("status").equalsIgnoreCase("ok")){
                            UserData userData = JsonParser.getUser(LoginActivity.this,res);
                            //save user data to shared preferences
                            PreferenceSettings.setUserData(userData);
                            PreferenceSettings.setIsUserLoggedIn(true);
                            if(!userData.isVerified()) {
                                //if user is yet to verify his email, display message for user to verify his mail
                                verify_mail_alert(email,getString(R.string.success), res.getString("message"));
                            }else {
                                //take user to main page
                                if(onboarder){
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                }else{
                                    LocalMessageManager.getInstance().send(R.id.recreate_mainactivity);
                                }
                                finish();
                            }
                        }else{
                            utility.show_alert(getString(R.string.error),res.getString("message"));
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                        Log.e("json error",e.getMessage());
                        utility.show_alert(getString(R.string.error),e.getMessage());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    //System.out.println(throwable);
                    Log.e("throw error",String.valueOf(throwable.getMessage()));
                    utility.hide_loader();
                    utility.show_alert(getString(R.string.error),throwable.getMessage());
                }
            });

        } catch (JSONException e) {
            utility.hide_loader();
            utility.show_alert(getString(R.string.error),e.getMessage());
            Log.e("parse error",e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearFields(){
        email.setText("");
        password.setText("");
    }

    public void verify_mail_alert(final String email, String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Resend Activation Link",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // positive button logic
                        resendVerificationEmail(email);
                    }
                });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog = builder.create();
        // display dialog
        dialog.show();
    }


    //resend verfication link to user mail
    private void resendVerificationEmail(String email){
        utility.show_loader();
        NetworkService service = StringApiClient.createService(NetworkService.class);
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("email", email);
            String requestBody = jsonData.toString();
            Log.d("final requestbody",requestBody);

            Call<String> callAsync = service.resendVerificationMail(requestBody);
            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.d("response",String.valueOf(response.body()));
                    utility.hide_loader();
                    try {
                        JSONObject res = new JSONObject(response.body());
                        // Add Your Logic
                        if(res.getString("status").equalsIgnoreCase("ok")){
                            utility.show_alert(getString(R.string.success), res.getString("message"));
                        }else{
                            utility.show_alert(getString(R.string.error),res.getString("message"));
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                        Log.e("error",e.getMessage());
                        utility.show_alert(getString(R.string.error),e.getMessage());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    //System.out.println(throwable);
                    Log.e("error",String.valueOf(throwable.getMessage()));
                    utility.hide_loader();
                    utility.show_alert(getString(R.string.error),throwable.getMessage());
                }
            });

        } catch (JSONException e) {
            utility.hide_loader();
            utility.show_alert(getString(R.string.error),e.getMessage());
            Log.e("parse error",e.getMessage());
            e.printStackTrace();
        }
    }


    public void skip_login(View view) {
        if(onboarder) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }else{
            //send event to restart mainactivity
            LocalMessageManager.getInstance().send(R.id.recreate_mainactivity);
        }
        finish();
    }
}
