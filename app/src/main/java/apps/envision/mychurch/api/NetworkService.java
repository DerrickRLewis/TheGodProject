package apps.envision.mychurch.api;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by ray on 21/08/2018.
 */

public interface NetworkService {

    @GET("authorize")
    Call<String> getAuthorizeKey();

    @GET("fetch_categories")
    Call<String> categories();

    @FormUrlEncoded
    @POST("discover")
    Call<String> discover(@Field("data") String data);

    @FormUrlEncoded
    @POST("updateUserSocialFcmToken")
    Call<String> updateUserSocialFcmToken(@Field("data") String data);

    @FormUrlEncoded
    @POST("get_bible")
    Call<String> get_bible(@Field("data") String data);

    @FormUrlEncoded
    @POST("devotionals")
    Call<String> devotionals(@Field("data") String data);

    @FormUrlEncoded
    @POST("get_article_content")
    Call<String> get_article_content(@Field("data") String data);

    @FormUrlEncoded
    @POST("fetch_events")
    Call<String> fetch_events(@Field("data") String data);

    @FormUrlEncoded
    @POST("fetch_inbox")
    Call<String> fetch_inbox(@Field("data") String data);

    @FormUrlEncoded
    @POST("search")
    Call<String> search(@Field("data") String data);

    @FormUrlEncoded
    @POST("fetch_media")
    Call<String> fetch_media(@Field("data") String data);

    @FormUrlEncoded
    @POST("fetch_hymns")
    Call<String> fetch_hymns(@Field("data") String data);

    @FormUrlEncoded
    @POST("fetch_livestreams")
    Call<String> fetch_livestreams(@Field("data") String data);

    @FormUrlEncoded
    @POST("fetch_categories_media")
    Call<String> fetch_categories_media(@Field("data") String data);

    @FormUrlEncoded
    @POST("getTrendingMedia")
    Call<String> getTrendingMedia(@Field("data") String data);

    @GET
    @Streaming
    Call<ResponseBody> downloadMedia(@Url String fileUrl, @Header("HTTP_RANGE") String range);

    @GET("uploads/AMP.json")//{json_path}
    @Streaming
    Call<ResponseBody> downloadBibleVersion(/*@Path(value = "json_path", encoded = true) String json_path*/);

    @GET("uploads/AMP.json")
    Call<String> getBibleData();

    @FormUrlEncoded
    @POST("loginUser")
    Call<String> login(@Field("data") String data);

    @FormUrlEncoded
    @POST("registerUser")
    Call<String> register(@Field("data") String data);

    @FormUrlEncoded
    @POST("resetPassword")
    Call<String> resetPassword(@Field("data") String data);

    @FormUrlEncoded
    @POST("resendVerificationMail")
    Call<String> resendVerificationMail(@Field("data") String data);

    @FormUrlEncoded
    @POST("storefcmtoken")
    Call<String> sendUserToken(@Field("data") String data);

    @FormUrlEncoded
    @POST("updatefcmtoken")
    Call<String> updatefcmtoken(@Field("data") String data);

    @FormUrlEncoded
    @POST("subscription")
    Call<String> verifyUserSubscription(@Field("data") String data);

    @FormUrlEncoded
    @POST("editcomment")
    Call<String> editcomment(@Field("data") String data);

    @FormUrlEncoded
    @POST("deletecomment")
    Call<String> deletecomment(@Field("data") String data);

    @FormUrlEncoded
    @POST("makecomment")
    Call<String> makecomment(@Field("data") String data);

    @FormUrlEncoded
    @POST("loadcomments")
    Call<String> loadcomments(@Field("data") String data);

    @FormUrlEncoded
    @POST("editreply")
    Call<String> editreply(@Field("data") String data);

    @FormUrlEncoded
    @POST("deletereply")
    Call<String> deletereply(@Field("data") String data);

    @FormUrlEncoded
    @POST("replycomment")
    Call<String> replycomment(@Field("data") String data);

    @FormUrlEncoded
    @POST("loadreplies")
    Call<String> loadreplies(@Field("data") String data);

    @FormUrlEncoded
    @POST("reportcomment")
    Call<String> reportcomment(@Field("data") String data);

    @FormUrlEncoded
    @POST("likeunlikemedia")
    Call<String> likeunlikemedia(@Field("data") String data);

    @FormUrlEncoded
    @POST("likeunlikepost")
    Call<String> likeunlikepost(@Field("data") String data);

    @FormUrlEncoded
    @POST("pinunpinpost")
    Call<String> pinunpinpost(@Field("data") String data);

    @FormUrlEncoded
    @POST("update_media_total_views")
    Call<String> update_media_total_views(@Field("data") String data);

    @FormUrlEncoded
    @POST("purchase_media")
    Call<String> purchase_media(@Field("data") String data);

    @FormUrlEncoded
    @POST("getmediatotallikesandcomments")
    Call<String> getmediatotallikesandcommentsviews(@Field("data") String data);

    @FormUrlEncoded
    @POST("fetch_user_purchases")
    Call<String> fetch_user_purchases(@Field("data") String data);

    @FormUrlEncoded
    @POST("subscribeCoupon")
    Call<String> subscribeCoupon(@Field("data") String data);

    @FormUrlEncoded
    @POST("saveDonation")
    Call<String> saveDonation(@Field("data") String data);

    @Multipart
    @POST("updateProfile")
    Call<String> editUserProfile(
            @Part MultipartBody.Part avatar,
            @Part MultipartBody.Part cover_photo,
            @PartMap() Map<String, RequestBody> partMap);

    @FormUrlEncoded
    @POST("get_users_to_follow")
    Call<String> get_users_to_follow(@Field("data") String data);

    @FormUrlEncoded
    @POST("follow_unfollow_user")
    Call<String> follow_unfollow_user(@Field("data") String data);

    @Multipart
    @POST("make_post")
    Call<String> make_post(
            @Part List<MultipartBody.Part> files,
            @PartMap() Map<String, RequestBody> partMap);

    @FormUrlEncoded
    @POST("fetch_posts")
    Call<String> fetch_posts(@Field("data") String data);

    @FormUrlEncoded
    @POST("editpostcomment")
    Call<String> editpostcomment(@Field("data") String data);

    @FormUrlEncoded
    @POST("deletepostcomment")
    Call<String> deletepostcomment(@Field("data") String data);

    @FormUrlEncoded
    @POST("makepostcomment")
    Call<String> makepostcomment(@Field("data") String data);

    @FormUrlEncoded
    @POST("loadpostcomments")
    Call<String> loadpostcomments(@Field("data") String data);

    @FormUrlEncoded
    @POST("reportpostcomment")
    Call<String> reportpostcomment(@Field("data") String data);

    @FormUrlEncoded
    @POST("replypostcomment")
    Call<String> replypostcomment(@Field("data") String data);

    @FormUrlEncoded
    @POST("deletepost")
    Call<String> deletepost(@Field("data") String data);

    @FormUrlEncoded
    @POST("editpostreply")
    Call<String> editpostreply(@Field("data") String data);

    @FormUrlEncoded
    @POST("deletepostreply")
    Call<String> deletepostreply(@Field("data") String data);

    @FormUrlEncoded
    @POST("loadpostreplies")
    Call<String> loadpostreplies(@Field("data") String data);

    @FormUrlEncoded
    @POST("editpost")
    Call<String> editpost(@Field("data") String data);

    @FormUrlEncoded
    @POST("userBioInfo")
    Call<String> userBioInfo(@Field("data") String data);

    @FormUrlEncoded
    @POST("userFollowPostCount")
    Call<String> userFollowPostCount(@Field("data") String data);

    @FormUrlEncoded
    @POST("fetchUserPosts")
    Call<String> fetchUserPosts(@Field("data") String data);

    @FormUrlEncoded
    @POST("users_follow_people")
    Call<String> users_follow_people(@Field("data") String data);

    @FormUrlEncoded
    @POST("update_user_settings")
    Call<String> update_user_settings(@Field("data") String data);

    @FormUrlEncoded
    @POST("fetch_user_settings")
    Call<String> fetch_user_settings(@Field("data") String data);

    @FormUrlEncoded
    @POST("fetchUserPins")
    Call<String> fetchUserPins(@Field("data") String data);

    @FormUrlEncoded
    @POST("post_likes_people")
    Call<String> post_likes_people(@Field("data") String data);

    @FormUrlEncoded
    @POST("userNotifications")
    Call<String> userNotifications(@Field("data") String data);

    @FormUrlEncoded
    @POST("deleteNotification")
    Call<String> deleteNotification(@Field("data") String data);

    @FormUrlEncoded
    @POST("setSeenNotifications")
    Call<String> setSeenNotifications(@Field("data") String data);

    @FormUrlEncoded
    @POST("getUnSeenNotifications")
    Call<String> getUnSeenNotifications(@Field("data") String data);

    @FormUrlEncoded
    @POST("church_branches")
    Call<String> church_branches(@Field("data") String data);

}
