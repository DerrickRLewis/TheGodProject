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

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private AppCompatEditText email;
    private Utility utility;
    private boolean onboarder = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent().getStringExtra("onboarding")!=null){
            onboarder = true;
        }
        utility = new Utility(this);
        setContentView(R.layout.forgot_password_layout);
        email = findViewById(R.id.email);
        findViewById(R.id.login).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.login:
                Intent intent = new Intent(ForgotPasswordActivity.this,LoginActivity.class);
                if(onboarder) {
                    intent.putExtra("onboarding", "true");
                }
                startActivity(intent);
                overridePendingTransition(R.anim.slide_right_out,R.anim.slide_right_in);
                finish();
                //supportFinishAfterTransition();
                break;
        }
    }

    public void password_reset(View view){
       String email_ = email.getText().toString().trim();
       if(!utility.emailValidator(email_)){
           utility.show_alert(getString(R.string.error),getString(R.string.invalid_email_hint));
           return;
       }
        recoverPassword(email_);
    }

    //recover user
    private void recoverPassword(String email){
        utility.show_loader();
        NetworkService service = StringApiClient.createService(NetworkService.class);
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("email", email);
            String requestBody = jsonData.toString();
            Log.d("final requestbody",requestBody);

            Call<String> callAsync = service.resetPassword(requestBody);
            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.d("response",String.valueOf(response.body()));
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
    }
}
