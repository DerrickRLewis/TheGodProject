package apps.envision.mychurch.ui.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;

/**
 * Created by ray on 06/06/2018.
 */

public class SplashActivity extends AppCompatActivity {

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        }
        //if user have previously seen the onboarding activity, we take the user to MainActivity
        if(PreferenceSettings.getOnboardingCompleted()){
            intent = new Intent(this, MainActivity.class);
        }else{
            intent = new Intent(this, OnboardingActivity.class);
        }
        startActivity(intent);
        finish();
    }
}