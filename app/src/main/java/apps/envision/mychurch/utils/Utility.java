package apps.envision.mychurch.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.collections4.ListUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import apps.envision.mychurch.App;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.AppDb;
import apps.envision.mychurch.db.DataInterfaceDao;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.libs.spans.CustomTypefaceSpan;
import apps.envision.mychurch.libs.spans.Spanny;
import apps.envision.mychurch.pojo.Bible;
import apps.envision.mychurch.pojo.Items;
import apps.envision.mychurch.pojo.LikeUpdate;
import apps.envision.mychurch.pojo.Livestreams;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.pojo.PlaylistMedias;
import apps.envision.mychurch.pojo.SBible;
import apps.envision.mychurch.pojo.Uploads;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.pojo.UserPosts;
import apps.envision.mychurch.ui.activities.LoginActivity;
import apps.envision.mychurch.ui.activities.RegisterActivity;
import apps.envision.mychurch.ui.activities.SubscriptionActivity;
import apps.envision.mychurch.socials.NewPostActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Utility {

    private Context context;
    private AlertDialog dialog, imgDialog;
    private ProgressDialog progressDialog;

    public Utility(Context context){
        this.context = context;
    }

    public static List<Items> getMenuItems(Context context){
        List<Items> itemsList = new ArrayList<>();
        Resources res = context.getResources();
        String[] wizards = res.getStringArray(R.array.menuOptions);
        String[] wizards_hint = res.getStringArray(R.array.menuOptions);
        TypedArray icons = context.getResources().obtainTypedArray(R.array.menuIcons);
        for (int i=0; i<wizards.length; i++) {
            itemsList.add(new Items(wizards[i],wizards_hint[i],res.getDrawable(icons.getResourceId(i, -1))));
        }
        return itemsList;
    }

    public static String getCurrentCountryCode() {
        String countryCode;

        // try to get country code from TelephonyManager service
        TelephonyManager tm = (TelephonyManager) App.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        if(tm != null) {
            // query first getSimCountryIso()
            countryCode = tm.getSimCountryIso();
            if (countryCode != null && countryCode.length() == 2)
                return countryCode.toLowerCase();

            if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
                // special case for CDMA Devices
                countryCode = getCDMACountryIso();
            } else {
                // for 3G devices (with SIM) query getNetworkCountryIso()
                countryCode = tm.getNetworkCountryIso();
            }

            if (countryCode != null && countryCode.length() == 2)
                return countryCode.toLowerCase();
        }

        // if network country not available (tablets maybe), get country code from Locale class
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            countryCode = App.getContext().getResources().getConfiguration().getLocales().get(0).getCountry();
        } else {
            countryCode = App.getContext().getResources().getConfiguration().locale.getCountry();
        }

        if (countryCode != null && countryCode.length() == 2)
            return  countryCode.toLowerCase();

        // general fallback to "us"
        return "wo";
    }

    @SuppressLint("PrivateApi")
    private static String getCDMACountryIso() {
        try {
            // try to get country code from SystemProperties private class
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            Method get = systemProperties.getMethod("get", String.class);

            // get homeOperator that contain MCC + MNC
            String homeOperator = ((String) get.invoke(systemProperties,
                    "ro.cdma.home.operator.numeric"));

            // first 3 chars (MCC) from homeOperator represents the country code
            int mcc = 0;
            if (homeOperator != null) {
                mcc = Integer.parseInt(homeOperator.substring(0, 3));
            }

            // mapping just countries that actually use CDMA networks
            switch (mcc) {
                case 330: return "PR";
                case 310: return "US";
                case 311: return "US";
                case 312: return "US";
                case 316: return "US";
                case 283: return "AM";
                case 460: return "CN";
                case 455: return "MO";
                case 414: return "MM";
                case 619: return "SL";
                case 450: return "KR";
                case 634: return "SD";
                case 434: return "UZ";
                case 232: return "AT";
                case 204: return "NL";
                case 262: return "DE";
                case 247: return "LV";
                case 255: return "UA";
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | NullPointerException ignored) {
        }

        return null;
    }

    public static String randomStringGenerator() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(20);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    public static String stripHtml(String html) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return Html.fromHtml(html).toString();
        }
    }

    public static String stripHtmlNotes(String html) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString().trim();
        } else {
            return Html.fromHtml(html).toString().trim();
        }
    }

    /**
     * @param color
     * @return
     */
    public static int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    public static int dpToPx(int dp) {
        float density = App.getContext().getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }

    public static void set_video_parent_to_match_parent(View view){
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
        layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
    }

    public static void set_video_parent_height(View view){
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
        layoutParams.height =  (int) App.getContext().getResources().getDimension(R.dimen.video_player_height);
        view.setLayoutParams(layoutParams);
    }

    public static void set_video_height(View view){
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)view.getLayoutParams();
        layoutParams.height =  dpToPx(250);
        //layoutParams.bottomMargin = 0;
        //view.setLayoutParams(layoutParams);
        //view.getLayoutParams().height= ViewGroup.LayoutParams.MATCH_PARENT;
    }

    /**
     * get Onboarding Activity Items from string
     * @param context
     * @return
     */
    public static List<Items> getItems(Context context){
        List<Items> itemsList = new ArrayList<>();
       /* Resources res = context.getResources();
        String[] wizards = res.getStringArray(R.array.wizards);
        String[] wizards_hint = res.getStringArray(R.array.wizards_hint);
        TypedArray icons = context.getResources().obtainTypedArray(R.array.wizards_images);
        for (int i=0; i<4; i++) {
            itemsList.add(new Items(wizards[i],wizards_hint[i],res.getDrawable(icons.getResourceId(i, -1))));
        }*/
        return itemsList;
    }

    /**
     * set margins to a view
     * @param view
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public static void setMargins (View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    /**
     * convert from dp to px
     * @param context
     * @param valueInDp
     * @return
     */
    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    /**
     * check if email is valid
     * @param email
     * @return
     */
    public boolean emailValidator(String email) {
        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean shouldLoadAds(){
        return !(PreferenceSettings.isUserLoggedIn()
                && PreferenceSettings.getIsUserSubscribed()
                && TimUtil.isValidFutureDate(PreferenceSettings.getUserSubscriptionExpiryDate()));
    }

    /**
     * show alert messages
     * @param title
     * @param message
     */
    public void show_alert(String title, String message){
        if(dialog!=null && dialog.isShowing())dialog.hide();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("ok",
                (dialog, which) -> {
                    // positive button logic
                    dialog.dismiss();
                });

        dialog = builder.create();
        dialog.setCancelable(false);
        // display dialog
        dialog.show();
    }

    /**
     * show alert messages
     * @param title
     * @param message
     */
    public void showUploadSuccessAlert(String title, String message){
        if(dialog!=null && dialog.isShowing())dialog.hide();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("ok",
                (dialog, which) -> {
                    // positive button logic
                    dialog.dismiss();
                    ((NewPostActivity)context).finish();
                });

        dialog = builder.create();
        dialog.setCancelable(false);
        // display dialog
        dialog.show();
    }

    /**
     * show alert messages
     * @param title
     * @param message
     */
    public void show_download_progress_alert(String title, String message){
        if(dialog!=null && dialog.isShowing())return;
        androidx.appcompat.app.AlertDialog.Builder builder;
        if(PreferenceSettings.getBibleThemeMode()==1){
            builder = new androidx.appcompat.app.AlertDialog.Builder(context,R.style.AlertDialogCustomLight);
        }else {
            builder = new androidx.appcompat.app.AlertDialog.Builder(context,R.style.AlertDialogCustomDark);
        }
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("ok",
                (dialog, which) -> {
                    // positive button logic
                    dialog.dismiss();
                });

        dialog = builder.create();
        dialog.setCancelable(false);
        // display dialog
        dialog.show();
    }


    public static boolean isUserSubscribed(){
        return PreferenceSettings.isUserLoggedIn()
                && PreferenceSettings.getIsUserSubscribed()
                && TimUtil.isValidFutureDate(PreferenceSettings.getUserSubscriptionExpiryDate());
    }

    /**
     * if user initiates a download
     * and the media requires user to be subscribed before downloading
     * show subscribe alert to user
     * @param media
     */
    public void show_subscribe_alert(Media media){
        if(!media.getVideo_type().equalsIgnoreCase("mp4_video") || !media.isCan_download()){
            Toast.makeText(App.getContext(),"Sorry, this message is not eligible for downloads",Toast.LENGTH_SHORT).show();
            return;
        }
        if(dialog!=null && dialog.isShowing())return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.subscription_required));
        builder.setMessage(context.getString(R.string.subscription_required_info)+" "+media.getMedia_type()+" "+context.getString(R.string.to_your_device));

        builder.setPositiveButton("Subscribe Now",
                (dialog, which) -> {
                    // positive button logic
                    dialog.dismiss();
                    //add subscribe logic here
                    context.startActivity(new Intent(context, SubscriptionActivity.class));
                });
        builder.setNegativeButton("Use Coupon Code",
                (dialog, which) -> {
                    // positive button logic
                    dialog.dismiss();
                    //add subscribe logic here
                    useCouponDialog();
                });
        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        dialog = builder.create();
        dialog.setCancelable(false);
        // display dialog
        dialog.show();
    }


    /**
     * alert user to create account or login
     */
    public void show_create_account_alert(){
        if(dialog!=null && dialog.isShowing())return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.create_account));
        builder.setMessage(context.getString(R.string.login_required));

        builder.setPositiveButton("Create Account",
                (dialog, which) -> {
                    // positive button logic
                    dialog.dismiss();
                    //add subscribe logic here
                    context.startActivity(new Intent(context, RegisterActivity.class));
                });
        builder.setNegativeButton("Login",
                (dialog, which) -> {
                    // positive button logic
                    dialog.dismiss();
                    //add subscribe logic here
                    context.startActivity(new Intent(context, LoginActivity.class));
                });
        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        dialog = builder.create();
        dialog.setCancelable(false);
        // display dialog
        dialog.show();
    }

    /**
     * alert user to create account or login
     */
    public void show_create_account_social_alert(String title, String msg){
        if(dialog!=null && dialog.isShowing())return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);

        builder.setPositiveButton("Create Account",
                (dialog, which) -> {
                    // positive button logic
                    dialog.dismiss();
                    //add subscribe logic here
                    context.startActivity(new Intent(context, RegisterActivity.class));
                });
        builder.setNegativeButton("Login",
                (dialog, which) -> {
                    // positive button logic
                    dialog.dismiss();
                    //add subscribe logic here
                    context.startActivity(new Intent(context, LoginActivity.class));
                });
        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        dialog = builder.create();
        dialog.setCancelable(false);
        // display dialog
        dialog.show();
    }

    /**
     * if a currently playing media requires user to subscribe before playing full length
     * alert user
     * @param media_type
     */
    public void show_play_subscribe_alert(String media_type){
        if(dialog!=null && dialog.isShowing())return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(App.getContext().getString(R.string.subscription_required));
        if(media_type.equalsIgnoreCase("livetv")){
            builder.setMessage(App.getContext().getString(R.string.live_tv_subscription_alert));
        }else {
            builder.setMessage(App.getContext().getString(R.string.play_subscription_required_info) + " " + media_type);
        }
        builder.setPositiveButton("Subscribe Now",
                (dialog, which) -> {
                    // positive button logic
                    dialog.dismiss();
                    //add subscribe logic here
                    context.startActivity(new Intent(context, SubscriptionActivity.class));
                });
        builder.setNegativeButton("Use Coupon Code",
                (dialog, which) -> {
                    // positive button logic
                    dialog.dismiss();
                    //add subscribe logic here
                    useCouponDialog();
                });
        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        dialog = builder.create();
        dialog.setCancelable(false);
        // display dialog
        dialog.show();
    }

    public void hide_dialog(){
        if(dialog!=null && dialog.isShowing()){
            dialog.dismiss();
        }
    }



    public void hide_loader(){
        if(progressDialog!=null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

    public void show_loader(){
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Processing, please wait.");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    /**
     * verify if user account is blocked and user cant access media file
     * show blocked account alert
     * and return true to stop all media related actions
     * @return
     */
    public boolean isUserBlocked(){
        UserData userData = PreferenceSettings.getUserData();
        if(userData!=null){
            // if user account is blocked, alert user
            if(userData.isBlocked()){
                show_alert(App.getContext().getResources().getString(R.string.account_blocked),
                        App.getContext().getResources().getString(R.string.account_blocked_hint));
                return true;
            }
        }
        return false;
    }

    /**
     * verify if user account is blocked and user cant access media file
     * dont show blocked account alert
     * and return true to stop all media related actions
     * @return
     */
    public static boolean isUserAccountBlocked(){
        UserData userData = PreferenceSettings.getUserData();
        if(userData!=null){
            return userData.isBlocked();
        }
        return false;
    }

    /**
     * check eligibility to play this media
     * @param media
     * @return
     */
    public boolean requiresUserSubscription(Media media){
        return !media.isIs_free()//if media is not free to preview
                && !media.isCan_preview() //user cannot preview media
                && (!PreferenceSettings.getIsUserSubscribed()
                && !TimUtil.isValidFutureDate(PreferenceSettings.getUserSubscriptionExpiryDate()));
    }

    public boolean requiresUserSubscription(Livestreams livestreams){
        return !livestreams.isIs_free()//if media is not free to preview
                && (!PreferenceSettings.getIsUserSubscribed()
                && !TimUtil.isValidFutureDate(PreferenceSettings.getUserSubscriptionExpiryDate()));
    }

    /**
     * check eligibility to play this media
     * @param media
     * @return
     */
    public static boolean mediaRequiresUserSubscription(Media media){
        return !media.isIs_free()//if media is not free to preview
                && !media.isCan_preview() //user cannot preview media
                && (!PreferenceSettings.getIsUserSubscribed()
                && !TimUtil.isValidFutureDate(PreferenceSettings.getUserSubscriptionExpiryDate()));
    }

    public static boolean checkUserSubscriptionStatus(){
        return (PreferenceSettings.getIsUserSubscribed()
                && TimUtil.isValidFutureDate(PreferenceSettings.getUserSubscriptionExpiryDate()));
    }

    /**
     * check eligibility to download this media
     * @param media
     * @return
     */
    public boolean canUserDownloadMedia(Media media){
        if(media==null)return false;
        if(!media.getVideo_type().equalsIgnoreCase("mp4_video"))return false;
        // if(!media.getVideo_type().equalsIgnoreCase("mp4_video"))return true;
        return (media.isIs_free() && media.isCan_download()) //if media streaming is free and is free to download
                || (media.isCan_download()  //if media streaming is not free and is free to download for subscribed users
                && PreferenceSettings.getIsUserSubscribed()
                && TimUtil.isValidFutureDate(PreferenceSettings.getUserSubscriptionExpiryDate()));
    }

    /**
     * check eligibility to play full media length
     * @param media
     * @return
     */
    public static boolean isMediaPreviewDuration(Media media, long duration){
        return (!media.isIs_free() && !(PreferenceSettings.getIsUserSubscribed()
                && TimUtil.isValidFutureDate(PreferenceSettings.getUserSubscriptionExpiryDate()))
                && duration > media.getPreview_duration());
    }

    /**
     * set user subscribption status after successfully verfying user subscription data
     * @param mActivity
     * @param purchaseSku
     * @param expiryDate
     */
    public static void setUserBillingData(Context mActivity, String purchaseSku, long expiryDate){
        expiryDate = expiryDate/1000L;
        if(TimUtil.isValidFutureDate(expiryDate)){
            String billing_success = "";
            PreferenceSettings.setUserSubscriptionExpiryDate(expiryDate);
            PreferenceSettings.setIsUserSubscribed(true);
            switch (purchaseSku){
                case Constants.BILLING.ONE_WEEK_SUB:
                        billing_success = String.format(App.getContext().getString(R.string.sub_success), App.getContext().getString(R.string.one_week_plan), TimUtil.formatExpiryDate(PreferenceSettings.getUserSubscriptionExpiryDate()));
                    break;
                case Constants.BILLING.ONE_MONTH_SUB:
                        billing_success = String.format(App.getContext().getString(R.string.sub_success), App.getContext().getString(R.string.one_month_plan), TimUtil.formatExpiryDate(PreferenceSettings.getUserSubscriptionExpiryDate()));
                    break;
                case Constants.BILLING.THREE_MONTHS_SUB:
                        billing_success = String.format(App.getContext().getString(R.string.sub_success), App.getContext().getString(R.string.three_months_plan), TimUtil.formatExpiryDate(PreferenceSettings.getUserSubscriptionExpiryDate()));
                    break;
                case Constants.BILLING.SIX_MONTHS_SUB:
                        billing_success = String.format(App.getContext().getString(R.string.sub_success), App.getContext().getString(R.string.six_months_plan), TimUtil.formatExpiryDate(PreferenceSettings.getUserSubscriptionExpiryDate()));
                    break;
                case Constants.BILLING.ONE_YEAR_SUB:
                        billing_success = String.format(App.getContext().getString(R.string.sub_success), App.getContext().getString(R.string.one_year_plan), TimUtil.formatExpiryDate(PreferenceSettings.getUserSubscriptionExpiryDate()));
                    break;
            }

            //if user initiated the payment process
            //show success alert to user and send event to close subscription activity
            if(PreferenceSettings.getBillingOngoing()){
                PreferenceSettings.setBillingOngoing(false);
                LocalMessageManager.getInstance().send(R.id.inapp_billing_completed);
                new Utility(mActivity).show_subscribe_success_alert(App.getContext().getString(R.string.congrats),billing_success);
            }else{
                //recreate mainactivity to reflect the users current subscription status
                LocalMessageManager.getInstance().send(R.id.recreate_mainactivity);
            }
        }else{
            PreferenceSettings.setIsUserSubscribed(false);
            //recreate mainactivity to reflect the users current subscription status
            LocalMessageManager.getInstance().send(R.id.recreate_mainactivity);
        }
    }

    public static void update_app_database(Media media){
        AppDb db = AppDb.getDatabase(App.getContext());
        DataInterfaceDao dataInterfaceDao = db.dataDbInterface();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() ->{
            //we check if media exists and update
            //the insert statement will update if already exists
            if(dataInterfaceDao.getMedia(media.getId())!=null){
                dataInterfaceDao.insertMedia(media);
            }
            //update bookmarks table
            if(dataInterfaceDao.getBookmark(media.getId())!=null){
                dataInterfaceDao.bookmarkMedia(ObjectMapper.mapBoomarkFromMedia(media));
            }

            //update playlists table
            PlaylistMedias playlistMedias = dataInterfaceDao.fetchPlaylistMedia(media.getId());
            if(playlistMedias!=null){
                dataInterfaceDao.addMediaToPlaylist(ObjectMapper.mapPlaylistMedia(playlistMedias,media));
            }
        });
    }

    /**
     * get media total likes, comments and views from remote server
     * @param media
     */
    public static void fetchMediaTotalLikesAndComments(Media media){
        if(media==null)return;
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("media", media.getId());
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);
            Call<String> callAsync = service.getmediatotallikesandcommentsviews(requestBody);
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
                        if (status.equalsIgnoreCase("ok")) {
                            int comments_count = jsonObj.getInt("total_comments");
                            int likes_count = jsonObj.getInt("total_likes");
                            int views_count = jsonObj.getInt("total_views");
                            if(comments_count!=media.getComments_count() || likes_count!=media.getLikes_count()) {
                                media.setComments_count(comments_count);
                                media.setLikes_count(likes_count);
                                media.setViews_count(views_count);
                                LocalMessageManager.getInstance().send(R.id.updateMediaTotalLikesAndCommentsViews, media);
                                update_app_database(media);
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

    /**
     * when user likes/unlikes media. send media like status to our remote server
     * @param media
     */
    public static void likeunlikemedia(Media media){
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("media", media.getId());
            jsonData.put("email", PreferenceSettings.getUserData().getEmail());
            jsonData.put("action",media.isUserLiked()?"like":"unlike");
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);
            Call<String> callAsync = service.likeunlikemedia(requestBody);
            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response", String.valueOf(response.body()));
                    if(response.body()==null){
                        reverse_media_reaction(media);
                        return;
                    }

                    try {
                        JSONObject jsonObj = new JSONObject(response.body());
                        String status = jsonObj.getString("status");
                        if (status.equalsIgnoreCase("ok")) {
                            update_app_database(media);

                        }else{
                            reverse_media_reaction(media);
                        }
                    } catch (JSONException e) {
                        Log.e("Error", e.toString());
                        reverse_media_reaction(media);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    Log.e("error", String.valueOf(throwable.getMessage()));
                    reverse_media_reaction(media);
                }
            });
        }catch (JSONException e) {
            e.printStackTrace();
            reverse_media_reaction(media);
        }
    }

    /**
     * when a user views a media, update views count on our remote server
     * @param media
     */
    public static void update_media_total_views(Media media){
        if(verifyUserViewedMedia(media))return;//if user have already viewed this media, return
        media.setViews_count(media.getViews_count() + 1);
        update_app_database(media);
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("media", media.getId());
            String requestBody = jsonData.toString();
            Log.e("views requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);
            Call<String> callAsync = service.update_media_total_views(requestBody);
            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("views count response", String.valueOf(response.body()));
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

    /**
     * fetch from shared preferences and verify if user have already viewed this media
     * add media id to list of user viewed media and save in shared preferences
     * @param media
     * @return
     */
    private static boolean verifyUserViewedMedia(Media media){
        List<String> mediaViews = PreferenceSettings.getUserMediaViews();
        if(mediaViews == null){
            mediaViews = new ArrayList<>();
        }
        for(int i=0; i<mediaViews.size(); i++){
            long itm = Long.parseLong(mediaViews.get(i));
            if(itm == media.getId()){
                return true;
            }
        }
        mediaViews.add(String.valueOf(media.getId()));
        PreferenceSettings.setUserMediaViews(mediaViews);
        return false;
    }

    /**
     * if there is network issues in sending media like status to the server
     * reverse actions on our app database
     * @param media
     */
    private static void reverse_media_reaction(Media media){
        if(media.isUserLiked()){
            media.setUserLiked(false);
            media.setLikes_count(media.getLikes_count()-1);
        }else{
            media.setUserLiked(true);
            media.setLikes_count(media.getLikes_count()+1);
        }
        LocalMessageManager.getInstance().send(R.id.reverse_media_reaction,media);
        Toast.makeText(App.getContext(),App.getContext().getString(R.string.like_media_error),Toast.LENGTH_SHORT).show();
    }

    /**
     * convert total count to string
     * @param count
     * @return
     */
    public static String reduceCountToString(long count){
        if(count < 1000){
            return count+" ";
        }else{
            long res = (long)count/1000;
            long remainder = (long)count % 1000;
            if(remainder==0){
                return res+"k ";
            }else{
                return res+"k+ ";
            }
        }
    }


    public static List<SBible> arrange_selected_bible_verses(List<SBible> bibleList){
        Collections.sort(bibleList, (lhs, rhs) -> Long.compare(lhs.getId(),rhs.getId()));
        return bibleList;
    }

    public static String get_bibleverses_as_string(List<SBible> bibleList){
        if(bibleList.size()==0)return "";
        Spanny spanny = new Spanny(bibleList.get(0).getBook()+" Chapter "+bibleList.get(0).getChapter()+"\n\n", new StyleSpan(Typeface.BOLD));
        for (SBible bible: bibleList) {
            spanny.append(String.valueOf(bible.getVerse())+": ", new StyleSpan(Typeface.BOLD));
            spanny.append(bible.getContent()+"\n");
            spanny.append(String.valueOf(bible.getVersion())+"\n\n", new StyleSpan(Typeface.ITALIC));
        }
        return spanny.toString();
    }

    public static String get_bibleverses_as_string_for_speech(List<Bible> bibleList){
        if(bibleList.size()==0)return "";
        Spanny spanny = new Spanny();
        for (Bible bible: bibleList) {
            spanny.append(String.valueOf(bible.getVerse())+"\n");
            switch (PreferenceSettings.getDefaultSelectedVersion()){
                case Constants.BIBLE_VERSIONS.AMP:
                    spanny.append(bible.getAMP()+"\n\n");
                    break;
                case Constants.BIBLE_VERSIONS.KJV:
                    spanny.append(bible.getKJV()+"\n\n");
                    break;
                case Constants.BIBLE_VERSIONS.NKJV:
                    spanny.append(bible.getNKJV()+"\n\n");
                    break;
                case Constants.BIBLE_VERSIONS.NIV:
                    spanny.append(bible.getNIV()+"\n\n");
                    break;
                case Constants.BIBLE_VERSIONS.NLT:
                    spanny.append(bible.getNLT()+"\n\n");
                    break;
                case Constants.BIBLE_VERSIONS.MSG:
                    spanny.append(bible.getMSG()+"\n\n");
                    break;
                case Constants.BIBLE_VERSIONS.NRSV:
                    spanny.append(bible.getNRSV()+"\n\n");
                    break;
            }
        }
        //Log.e("read",spanny.toString());
        return spanny.toString();
    }

    // Generic function to split a list using Apache Commons Collections
    public static<T> List split(List<T> list, int size) {
        // partition the List into two sublists
        List<List<T>> lists = ListUtils.partition(list, (list.size() + 1) / size);
        // return an array containing both lists
        return lists;
    }

    public static String get_bibleverses_as_string_for_bookmarks(List<SBible> bibleList){
        if(bibleList.size()==0)return "";
        Spanny spanny = new Spanny(bibleList.get(0).getBook()+" Chapter "+bibleList.get(0).getChapter()+"<br>", new StyleSpan(Typeface.BOLD));
        for (SBible bible: bibleList) {
            spanny.append(String.valueOf(bible.getVerse())+": ", new StyleSpan(Typeface.BOLD));
            spanny.append(bible.getContent()+"<br>");
            spanny.append(String.valueOf(bible.getVersion())+"<br>", new StyleSpan(Typeface.ITALIC));
        }
        return spanny.toString();
    }

    public void useCouponDialog() {
        UserData userData = PreferenceSettings.getUserData();
        if(userData==null){
            show_create_account_alert();
        }else {
            //
            final android.app.AlertDialog dialogBuilder = new android.app.AlertDialog.Builder(context).create();
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.coupon_code, null);

            final EditText editText = (EditText) dialogView.findViewById(R.id.edt_comment);
            Button button1 = (Button) dialogView.findViewById(R.id.buttonSubmit);
            Button button2 = (Button) dialogView.findViewById(R.id.buttonCancel);

            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogBuilder.dismiss();
                }
            });
            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // DO SOMETHINGS
                    String code = editText.getText().toString();
                    if(!code.equalsIgnoreCase("")){
                        dialogBuilder.dismiss();
                        subscribeCoupon(code);
                    }
                }
            });

            dialogBuilder.setView(dialogView);
            dialogBuilder.show();
        }
    }

    //subscribe user with coupon code
    private void subscribeCoupon(String code){
        UserData userData = PreferenceSettings.getUserData();
        if(userData==null){
            return;
        }
        show_loader();
        NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("email", userData.getEmail());
            jsonData.put("code", code);
            String requestBody = jsonData.toString();
            Log.e("verify requestBody",requestBody);

            Call<String> callAsync = service.subscribeCoupon(requestBody);

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    hide_loader();
                    Log.e("verify response",String.valueOf(response.body()));
                    if(response.body()==null){
                        show_alert(App.getContext().getString(R.string.sub_not_successful),App.getContext().getString(R.string.coupon_verify_error));
                        return;
                    }

                    try {
                        JSONObject res = new JSONObject(response.body());
                        if(res.getString("status").equalsIgnoreCase("ok")){
                            PreferenceSettings.setBillingOngoing(true);
                            setUserBillingData(context,res.getString("period"),res.getLong("expiry_date"));
                        }else{
                            show_alert(App.getContext().getString(R.string.sub_not_successful),res.getString("msg"));
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                        show_alert(App.getContext().getString(R.string.sub_not_successful),e.getMessage());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    hide_loader();
                    //System.out.println(throwable);
                    Log.e("error",String.valueOf(throwable.getMessage()));
                    show_alert(App.getContext().getString(R.string.sub_not_successful),throwable.getMessage());
                }
            });

        } catch (JSONException e) {
            hide_loader();
            Log.e("billing error",e.getMessage());
            show_alert(App.getContext().getString(R.string.sub_not_successful),e.getMessage());
        }
    }

    public void load_subscriptions(){
        if(dialog!=null && dialog.isShowing())return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.subscription_method);
        builder.setMessage(R.string.choose_subscription_method);
        builder.setPositiveButton("Google Billing",
                (dialog, which) -> {
                    // positive button logic
                    dialog.dismiss();
                    //add subscribe logic here
                    context.startActivity(new Intent(context, SubscriptionActivity.class));
                });
        builder.setNegativeButton("Use Coupon Code",
                (dialog, which) -> {
                    // positive button logic
                    dialog.dismiss();
                    //add subscribe logic here
                    useCouponDialog();
                });
        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        dialog = builder.create();
        dialog.setCancelable(false);
        // display dialog
        dialog.show();
    }

    /**
     * show alert messages
     * @param title
     * @param message
     */
    public void show_subscribe_success_alert(String title, String message){
        if(dialog!=null && dialog.isShowing())return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("ok",
                (dialog, which) -> {
                    // positive button logic
                    dialog.dismiss();
                    //recreate mainactivity to reflect the users current subscription status
                    LocalMessageManager.getInstance().send(R.id.recreate_mainactivity);
                });

        dialog = builder.create();
        dialog.setCancelable(false);
        // display dialog
        dialog.show();
    }

    public static String getBase64EncodedString(String text){
        byte[] encodeValue = Base64.encode(text.getBytes(), Base64.DEFAULT);
        return new String(encodeValue);
    }

    public static String getBase64DEcodedString(String text){
        byte[] decodeValue = Base64.decode(text, Base64.DEFAULT);
        return new String(decodeValue);
    }

    public static List<Uploads> getUploadsMedia(String uploadString){
        Log.e("uploadString",uploadString +" here");
        List<Uploads> uploadsList = new ArrayList<>();
        Type listType = new TypeToken<List<Uploads>>() {
        }.getType();
        List list = new Gson().fromJson(uploadString, listType);
        if(list!=null){
            uploadsList = list;
        }
        return uploadsList;
    }

    /**
     * when user likes/unlikes media. send media like status to our remote server
     */
    public static void likeunlikepost(UserPosts userPosts, String action, int position, boolean refresh){
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("id", userPosts.getId());
            jsonData.put("user", userPosts.getEmail());
            jsonData.put("email", PreferenceSettings.getUserData().getEmail());
            jsonData.put("action",action);
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);
            Call<String> callAsync = service.likeunlikepost(requestBody);
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
                           int count = jsonObj.getInt("count");
                            LikeUpdate likeUpdate = new LikeUpdate(position, count, action, refresh);
                            LocalMessageManager.getInstance().send(R.id.update_user_likes_count, likeUpdate);
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

    public static void pinunpinpost(UserPosts userPosts, String action){
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("id", userPosts.getId());
            jsonData.put("email", PreferenceSettings.getUserData().getEmail());
            jsonData.put("action",action);
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);
            Call<String> callAsync = service.pinunpinpost(requestBody);
            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response", String.valueOf(response.body()));
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

    public void showLargeAvatar(String url) {
        if(imgDialog!=null && imgDialog.isShowing())imgDialog.dismiss();
        AlertDialog.Builder dialogBuilder  = new AlertDialog.Builder(context,R.style.AlertDialogCustomLight);
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View view = inflater.inflate(R.layout.zoom_imageview, null);
        dialogBuilder.setView(view);
        ImageView imageView = view.findViewById(R.id.imageview);
        ImageLoader.loadUserAvatar(imageView, url);

        AlertDialog imgDialog = dialogBuilder.create();
        imgDialog.getWindow().setGravity(Gravity.CENTER);
        imgDialog.show();
    }

    public void showLargeCoverPhoto(String url) {
        if(imgDialog!=null && imgDialog.isShowing())imgDialog.dismiss();
        AlertDialog.Builder dialogBuilder  = new AlertDialog.Builder(context,R.style.AlertDialogCustomLight);
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View view = inflater.inflate(R.layout.zoom_imageview, null);
        dialogBuilder.setView(view);
        ImageView imageView = view.findViewById(R.id.imageview);
        ImageLoader.loadPostImage(imageView, url);

        AlertDialog imgDialog = dialogBuilder.create();
        imgDialog.getWindow().setGravity(Gravity.CENTER);
        imgDialog.show();
    }
}
