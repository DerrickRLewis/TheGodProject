package apps.envision.mychurch.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.R;
import apps.envision.mychurch.utils.Utility;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private AppCompatEditText email,name,password;
    private Utility utility;
    private boolean onboarder = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        utility = new Utility(this);
        if(getIntent().getStringExtra("onboarding")!=null){
            onboarder = true;
        }
        setContentView(R.layout.register_activity);
        email = findViewById(R.id.email);
        name = findViewById(R.id.name);
        password = findViewById(R.id.password);
        findViewById(R.id.login).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.login:
                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                if(onboarder) {
                    intent.putExtra("onboarding", "true");
                }
                startActivity(intent);
                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
                finish();
               // supportFinishAfterTransition();
                break;
        }
    }

    public void register_user(View view){
        String email_ = email.getText().toString().trim();
        String name_ = name.getText().toString().trim();
        String password_ = password.getText().toString();
        if(!utility.emailValidator(email_)){
            utility.show_alert(getString(R.string.error),getString(R.string.invalid_email_hint));
            return;
        }

        if(name_.equalsIgnoreCase("")){
            utility.show_alert(getString(R.string.error),getString(R.string.name_error));
            return;
        }

        if(password_.equalsIgnoreCase("")){
            utility.show_alert(getString(R.string.error),getString(R.string.password_error_hint));
            return;
        }

        if(password_.length()<6){
            utility.show_alert(getString(R.string.error),getString(R.string.password_too_short_hint));
            return;
        }

        registerUser(email_,name_,password_);
    }

    //recover user
    private void registerUser(String email,String name, String password){
        utility.show_loader();
        NetworkService service = StringApiClient.createService(NetworkService.class);
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("email", email);
            jsonData.put("name", name);
            jsonData.put("password", password);
            String requestBody = jsonData.toString();
            Log.d("final requestbody",requestBody);

            Call<String> callAsync = service.register(requestBody);
            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response",String.valueOf(response.body()));
                    utility.hide_loader();
                    try {
                        JSONObject res = new JSONObject(response.body());
                        // Add Your Logic
                        if(res.getString("status").equalsIgnoreCase("ok")){
                            utility.show_alert(getString(R.string.success),res.getString("message"));
                            clearFields();
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

    private void clearFields(){
        email.setText("");
        name.setText("");
        password.setText("");
    }


    public void skip_register(View view) {
        if(onboarder) {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        }
        finish();
    }
}
