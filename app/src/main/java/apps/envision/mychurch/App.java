package apps.envision.mychurch;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;

import android.os.Bundle;
import android.util.Log;

import com.stripe.android.PaymentConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.utils.Constants;
import co.paystack.android.PaystackSdk;
import glimpse.core.Glimpse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class App extends Application implements Application.ActivityLifecycleCallbacks  {

    @SuppressLint("StaticFieldLeak")
    private static Context context = null;
    private int activityReferences = 0;
    private boolean isActivityChangingConfigurations = false;
    public static boolean isForeground = false;

    @Override
    public void onCreate(){
        super.onCreate();
        context = getApplicationContext();
        registerActivityLifecycleCallbacks(this);
        //Log.e("progress",String.valueOf( Math.round(((float)10/(float)63) * 100)));
        if(PreferenceSettings.getAuthorizationKey().equalsIgnoreCase("")){
            getAuthorizationKey();
        }
        //set current app version
        //this will help us to serve appropiate media files to users
        //since older version wont support new media playbacks
        PreferenceSettings.setAppVersion("v2");

        createNotificationChannel();

        //send token to server
        if(!PreferenceSettings.getUserFcmTokenSentToServer()){
            sendUserTokenToServer(false);
        }else{
            if(!PreferenceSettings.getUserFcmTokenUpdated()){
                sendUserTokenToServer(true);
            }
        }
        PaystackSdk.initialize(getApplicationContext());
        //glimpse
        Glimpse.init(this);

        //stripe
        PaymentConfiguration.init(
                getApplicationContext(),
                getString(R.string.Stripe_publishable_key)
        );
    }

    public static Context getContext() {
        if (context != null) {
            return context;
        } else {
            throw new Error("App was not successfully created");
        }
    }

    /**
     * fetch and store json web token for making requests to our remote server
     */
    private void getAuthorizationKey(){
        NetworkService service = StringApiClient.createService(NetworkService.class);
        Call<String> callAsync = service.getAuthorizeKey();

        callAsync.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                Log.e("key","key = "+response.body());
                if(response.body() == null)return;
                PreferenceSettings.setAuthorizationKey(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                Log.e("error",throwable.getMessage());
                System.out.println(throwable);
            }
        });
    }

    /**
     * Because you must create the notification channel before posting any notifications on Android 8.0 and higher,
     * you should execute this code as soon as your app starts.
     * It's safe to call this repeatedly because creating an existing notification channel performs no operation.
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            String description = getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(Constants.NOTIFICATION.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * if user have a valid FCM Token, we send to server and store
     */
    public static void sendUserTokenToServer(boolean isUpdate){
        String token = PreferenceSettings.getFcmToken();
        Log.e("token","="+token);
        if(token.equalsIgnoreCase("")){
            return;
        }

        NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("token", token);
            jsonData.put("version", PreferenceSettings.getAppVersion());
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);

            Call<String> callAsync;
            if(!isUpdate) {
                callAsync = service.sendUserToken(requestBody);
            }else{
                callAsync = service.updatefcmtoken(requestBody);
            }

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response",String.valueOf(response.body()));
                    if(response.body()==null){
                        return;
                    }

                    try {
                        JSONObject res = new JSONObject(response.body());
                        // Add Your Logic
                        if(res.getString("status").equalsIgnoreCase("ok")){
                            PreferenceSettings.setUserFcmTokenSentToServer(true);
                            PreferenceSettings.setUserFcmTokenUpdated(true);
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    //System.out.println(throwable);
                    Log.e("error",String.valueOf(throwable.getMessage()));
                }
            });

        } catch (JSONException e) {
            Log.e("parse error",e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground
            isForeground = true;
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations();
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App enters background
            isForeground=false;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    /**
     * if user have a valid FCM Token, we send to server and store
     */
    public static void updateUserSocialToken(String email){
        String token = PreferenceSettings.getFcmToken();
        Log.e("token","="+token);
        if(token.equalsIgnoreCase("")){
            return;
        }

        NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("token", token);
            jsonData.put("email", email);
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);

            Call<String> callAsync = service.updateUserSocialFcmToken(requestBody);
            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response",String.valueOf(response.body()));
                    if(response.body()==null){
                        return;
                    }

                    try {
                        JSONObject res = new JSONObject(response.body());
                        // Add Your Logic
                        if(res.getString("status").equalsIgnoreCase("ok")){
                            PreferenceSettings.setUserFcmTokenSentToServer(true);
                            PreferenceSettings.setUserFcmTokenUpdated(true);
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    //System.out.println(throwable);
                    Log.e("error",String.valueOf(throwable.getMessage()));
                }
            });

        } catch (JSONException e) {
            Log.e("parse error",e.getMessage());
            e.printStackTrace();
        }
    }
}
