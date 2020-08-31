package apps.envision.mychurch.db;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.pojo.Bible;
import apps.envision.mychurch.pojo.BibleDownload;
import apps.envision.mychurch.pojo.MakePosts;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.pojo.SBible;
import apps.envision.mychurch.pojo.Slider;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.utils.Constants;


/*
* utility class to save user settings to SharedPreferences
 */
public class PreferenceSettings {


    public static String getDefaultSelectedVersion(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getString("default_selected_version","");
    }

    public static void setDefaultSelectedVersion(String version){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putString("default_selected_version", version);
        editor.apply();
    }

    public static String getDefaultSelectedBook(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getString("default_selected_book","Genesis");
    }

    public static void setDefaultSelectedBook(String book){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putString("default_selected_book", book);
        editor.apply();
    }

    public static int getDefaultSelectedChapter(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getInt("default_selected_chapter",0);
    }

    public static void setDefaultSelectedChapter(int chapter){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putInt("default_selected_chapter", chapter);
        editor.apply();
    }

    public static int getDefaultSelectedFont(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getInt("default_selected_font",20);
    }

    public static void setDefaultSelectedFont(int font){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putInt("default_selected_font", font);
        editor.apply();
    }

    public static boolean getRadioPlayerFlag(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getBoolean("is_radio_player",false);
    }

    public static void setRadioPlayerFlag(boolean ongoing){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putBoolean("is_radio_player", ongoing);
        editor.apply();
    }

    public static void setOnboardingCompleted(boolean completed){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putBoolean("user_onboarding_completed", completed);
        editor.apply();
    }

    public static Boolean getOnboardingCompleted(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getBoolean("user_onboarding_completed", false);
    }

    public static void setUserData(UserData userData){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(userData);
        editor.putString("user_data", json);
        editor.apply();
    }

    public static UserData getUserData(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        Gson gson = new Gson();
        String json = sharedPrefs.getString("user_data", "");
        return gson.fromJson(json, UserData.class);
    }

    public static boolean isUserLoggedIn(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getBoolean("is_user_logged_in",false);
    }

    public static void setIsUserLoggedIn(boolean isUserLoggedIn){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putBoolean("is_user_logged_in", isUserLoggedIn);
        editor.apply();
    }

    //get refresh time to fetch new media for home fragment
    public static long getHomeFragmentLastRefreshTime(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getLong("home_fragment_last_refresh_time", 0);
    }

    //set refresh time to fetch new media for home fragment
    public static void setHomeFragmentLastRefreshTime(long time){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putLong("home_fragment_last_refresh_time", time);
        editor.apply();
    }

    //get refresh time to fetch new media for audio fragment
    public static long getCategoriesFragmentLastRefreshTime(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getLong("categories_fragment_last_refresh_time", 0);
    }

    //set refresh time to fetch new media for audio fragment
    public static void setCategoriesFragmentLastRefreshTime(long time){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putLong("categories_fragment_last_refresh_time", time);
        editor.apply();
    }

    //get refresh time to fetch new media for audio fragment
    public static long getAudioFragmentLastRefreshTime(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getLong("audio_fragment_last_refresh_time", 0);
    }

    //set refresh time to fetch new media for audio fragment
    public static void setAudioFragmentLastRefreshTime(long time){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putLong("audio_fragment_last_refresh_time", time);
        editor.apply();
    }

    //get refresh time to fetch new media for video fragment
    public static long getVideoFragmentLastRefreshTime(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getLong("video_fragment_last_refresh_time", 0);
    }

    //set refresh time to fetch new media for audio fragment
    public static void setVideoFragmentLastRefreshTime(long time){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putLong("video_fragment_last_refresh_time", time);
        editor.apply();
    }

    //save offset of last search
    public static int get_search_offset(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getInt(Constants.SETTINGS.SEARCH_OFFSET, 0);
    }

    public static void set_search_offset(int offset){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putInt(Constants.SETTINGS.SEARCH_OFFSET, offset);
        editor.apply();
    }

    //verify if user opted to remove ads
    public static String get_search_query(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getString(Constants.SETTINGS.SEARCH_QUERY, "");
    }

    public static void set_search_query(String query){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putString(Constants.SETTINGS.SEARCH_QUERY, query);
        editor.apply();
    }

    public static int get_song_repeat_mode(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getInt(Constants.SETTINGS.AUDIO_PLAYER_REPEAT_MODE,1);
    }

    public static void set_song_repeat_mode() {
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        switch(get_song_repeat_mode()){
            case 1:
                editor.putInt(Constants.SETTINGS.AUDIO_PLAYER_REPEAT_MODE, 2);
                break;
            case 2:
                editor.putInt(Constants.SETTINGS.AUDIO_PLAYER_REPEAT_MODE, 3);
                break;
            case 3:
                editor.putInt(Constants.SETTINGS.AUDIO_PLAYER_REPEAT_MODE, 1);
                break;
        }
        editor.apply();
    }

    public static String getAuthorizationKey(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getString("Auth_jwt_key", "");
    }

    public static void setAuthorizationKey(String key){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putString("Auth_jwt_key", key);
        editor.apply();
    }

    //get user subscription expiry date
    public static long getUserSubscriptionExpiryDate(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getLong("user_subscription_expiry_date", 0);
    }

    //set user subscription expiry date
    public static void setUserSubscriptionExpiryDate(long time){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putLong("user_subscription_expiry_date", time);
        editor.apply();
    }

    public static Boolean getIsUserSubscribed(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getBoolean("is_user_subscribed", false);
    }

    public static void setIsUserSubscribed(boolean set){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putBoolean("is_user_subscribed", set);
        editor.apply();
    }


    //we can only allow one download at a time, so we set a flag to check if
    // download is ongoing
    public static boolean isDownloadInProgress(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getBoolean("is_download_in_progress",false);
    }

    //we can only allow one download at a time, so we set a flag to check if
    // download is ongoing
    public static void setDownloadInProgress(boolean downloadInProgress){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putBoolean("is_download_in_progress", downloadInProgress);
        editor.apply();
    }

    //if user pauses or cancels an ongoing download,
    // we set a flag to check if we should clear uncomplete download
    public static boolean getShouldClearUncompletedDownloadedFile(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getBoolean("clear_uncompleted_downloaded_file",false);
    }

    //if user pauses or cancels an ongoing download,
    // we set a flag to check if we should clear uncomplete download
    public static void setShouldClearUncompletedDownloadedFile(boolean clear){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putBoolean("clear_uncompleted_downloaded_file", clear);
        editor.apply();
    }

    //if user pauses or cancels an ongoing download,
    // we set a flag so user wont be shown a download error message
    public static boolean getIsUserInitiatedProcess(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getBoolean("is_user_initiated_process",false);
    }

    //if user pauses or cancels an ongoing download,
    // we set a flag so user wont be shown a download error message
    public static void setIsUserInitiatedProcess(boolean initiatedProcess){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putBoolean("is_user_initiated_process", initiatedProcess);
        editor.apply();
    }

    public static String getFcmToken(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getString("device_fcm_token", "");
    }

    public static void setFcmToken(String token){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putString("device_fcm_token", token);
        editor.apply();
    }

    public static String getAppVersion(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getString("current_app_version", "v1");
    }

    public static void setAppVersion(String version){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putString("current_app_version", version);
        editor.apply();
    }

    public static boolean getUserFcmTokenSentToServer(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getBoolean("token_sent_to_server",false);
    }

    public static void setUserFcmTokenSentToServer(boolean sent){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putBoolean("token_sent_to_server", sent);
        editor.apply();
    }

    public static boolean getUserFcmTokenUpdated(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getBoolean("token_version_updated",false);
    }

    public static void setUserFcmTokenUpdated(boolean updated){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putBoolean("token_version_updated", updated);
        editor.apply();
    }

    public static void setAudioActivityStatus(boolean status){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putBoolean("audio_activity_status", status);
        editor.apply();
    }

    public static Boolean getAudioActivityStatus(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getBoolean("audio_activity_status", false);
    }

    public static boolean getBillingOngoing(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getBoolean("billing_ongoing",false);
    }

    public static void setBillingOngoing(boolean ongoing){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putBoolean("billing_ongoing", ongoing);
        editor.apply();
    }

    public static void setUserMediaViews(List<String> userMediaViews){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(userMediaViews);
        editor.putString("user_media_views", json);
        editor.apply();
    }

    public static List getUserMediaViews(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        Gson gson = new Gson();
        String json = sharedPrefs.getString("user_media_views", "");
        return gson.fromJson(json, List.class);
    }

    //bible downloads flag
  /*  public static int getCurrentBibleDownloadPage(String key){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getInt(key,0);
    }

    public static void setCurrentBibleDownloadPage(String key, int page){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putInt(key, page);
        editor.apply();
    }*/

    //bible downloads flag
    public static BibleDownload getCurrentBibleDownload(String key){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        Gson gson = new Gson();
        String json = sharedPrefs.getString(key, "");
        return gson.fromJson(json, BibleDownload.class);
    }

    public static void setCurrentBibleDownload(String key, BibleDownload bibleDownload){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(bibleDownload);
        editor.putString(key, json);
        editor.apply();
    }


    //we can only allow one download at a time, so we set a flag to check if
    // download is ongoing
    public static boolean isBibleDownloadInProgress(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getBoolean("is_bible_download_in_progress",false);
    }

    //we can only allow one download at a time, so we set a flag to check if
    // download is ongoing
    public static void setBibleDownloadInProgress(boolean downloadInProgress){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putBoolean("is_bible_download_in_progress", downloadInProgress);
        editor.apply();
    }

    public static void setSelectedBibleVerses(List<SBible> bibleList){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(bibleList);
        editor.putString("selected_bible_verses", json);
        editor.apply();
    }

    public static List getSelectedBibleVerses(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        Type listType = new TypeToken<List<SBible>>() {
        }.getType();
        return new Gson().fromJson(sharedPrefs.getString("selected_bible_verses", ""), listType);
    }

    public static void setHighlightedBibleVerses(List<SBible> bibleList){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(bibleList);
        editor.putString("highlighted_bible_verses", json);
        editor.apply();
    }

    public static List getHighlightedBibleVerses(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        Type listType = new TypeToken<List<SBible>>() {
        }.getType();
        return new Gson().fromJson(sharedPrefs.getString("highlighted_bible_verses", ""), listType);
    }

    public static String geFacebookPage(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getString("facebook_page","https://www.facebook.com");
    }

    public static void setFacebookPage(String content){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putString("facebook_page", content);
        editor.apply();
    }

    public static String geYoutubePage(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getString("youtube_page","https://www.youtube.com");
    }

    public static void setYoutubePage(String content){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putString("youtube_page", content);
        editor.apply();
    }

    public static String geTwitterPage(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getString("twitter_page","https://www.twitter.com");
    }

    public static void setTwitterPage(String content){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putString("twitter_page", content);
        editor.apply();
    }

    public static String getInstagramPage(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getString("instagram_page","https://www.instagram.com");
    }

    public static void setInstagramPage(String content){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putString("instagram_page", content);
        editor.apply();
    }

    public static int getAdvertsInterval(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getInt("adverts_interval",15);
    }

    public static void setAdvertsInterval(int interval){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putInt("adverts_interval", interval);
        editor.apply();
    }

    public static int getBibleThemeMode(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getInt("bible_theme_mode",1);
    }

    public static void setBibleThemeMode(int mode){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putInt("bible_theme_mode", mode);
        editor.apply();
    }

    public static int getSelectedHighlightColor(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getInt("selected_highlight_color", R.color.yellow);
    }

    public static void setSelectedHighlightColor(int color){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putInt("selected_highlight_color", color);
        editor.apply();
    }

    public static String getWebsiteURL(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getString("website_url","https://www.envisionaps.net");
    }

    public static void setWebsiteURL(String url){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putString("website_url", url);
        editor.apply();
    }

    public static String getHomePageImages(String path){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getString(path,"");
    }

    public static void setHomePageImages(String path, String name){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putString(path, name);
        editor.apply();
    }

    public static void setSliderMedia(List<Media> bibleList){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(bibleList);
        editor.putString("slider_media", json);
        editor.apply();
    }

    public static List<Media> getSliderMedia(){
        List<Media> sliderList = new ArrayList<>();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        Type listType = new TypeToken<List<Media>>() {
        }.getType();
        List list = new Gson().fromJson(sharedPrefs.getString("slider_media", ""), listType);
        if(list!=null){
            sliderList = list;
        }
        return sliderList;
    }

    public static void setCurrentPostData(MakePosts makePosts){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(makePosts);
        editor.putString("current_user_post", json);
        editor.apply();
    }

    public static MakePosts getCurrentPostData(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        Gson gson = new Gson();
        String json = sharedPrefs.getString("current_user_post", "");
        return gson.fromJson(json, MakePosts.class);
    }

    public static boolean isPostUploadInProgress(){
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getBoolean("is_user_currently_posting",false);
    }

    //we can only allow one download at a time, so we set a flag to check if
    // download is ongoing
    public static void setPostUploadInProgress(boolean status){
        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        editor = sharedPrefs.edit();
        editor.putBoolean("is_user_currently_posting", status);
        editor.apply();
    }
}
