package apps.envision.mychurch.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import apps.envision.mychurch.App;
import apps.envision.mychurch.pojo.Bible;
import apps.envision.mychurch.pojo.Branches;
import apps.envision.mychurch.pojo.Categories;
import apps.envision.mychurch.pojo.Coins;
import apps.envision.mychurch.pojo.Comments;
import apps.envision.mychurch.pojo.Devotionals;
import apps.envision.mychurch.pojo.Events;
import apps.envision.mychurch.pojo.Hymns;
import apps.envision.mychurch.pojo.Inbox;
import apps.envision.mychurch.pojo.Livestreams;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.pojo.Radio;
import apps.envision.mychurch.pojo.Replies;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.pojo.UserNotifications;
import apps.envision.mychurch.pojo.UserPosts;
import timber.log.Timber;

/**
 * Created by ray
 * helper class to parse jsonobjects to pojo classes
 */

public class JsonParser {

    /**
     * @param objArray
     * @return parse and return bible list
     */
    public static List<Bible> getBible(JSONArray objArray, String book_name) {
        List<Bible> bibleList = new ArrayList<>();
        try {
            for (int i = 0; i < objArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) objArray.get(i);
                Bible bible = new Bible();
                bible.setId(jsonObject.getLong("id"));
                bible.setBook(jsonObject.getString("book"));
                bible.setChapter(jsonObject.getInt("chapter"));
                bible.setVerse(jsonObject.getInt("verse"));
                switch (book_name){
                    case Constants.BIBLE_VERSIONS.AMP:
                        bible.setAMP(jsonObject.getString(Constants.BIBLE_VERSIONS.AMP));
                        break;
                    case Constants.BIBLE_VERSIONS.KJV:
                        bible.setKJV(jsonObject.getString(Constants.BIBLE_VERSIONS.KJV));
                        break;
                    case Constants.BIBLE_VERSIONS.MSG:
                        bible.setMSG(jsonObject.getString(Constants.BIBLE_VERSIONS.MSG));
                        break;
                    case Constants.BIBLE_VERSIONS.NIV:
                        bible.setNIV(jsonObject.getString(Constants.BIBLE_VERSIONS.NIV));
                        break;
                    case Constants.BIBLE_VERSIONS.NKJV:
                        bible.setNKJV(jsonObject.getString(Constants.BIBLE_VERSIONS.NKJV));
                        break;
                    case Constants.BIBLE_VERSIONS.NLT:
                        bible.setNLT(jsonObject.getString(Constants.BIBLE_VERSIONS.NLT));
                        break;
                    case Constants.BIBLE_VERSIONS.NRSV:
                        bible.setNRSV(jsonObject.getString(Constants.BIBLE_VERSIONS.NRSV));
                        break;
                }
                bibleList.add(bible);
            }

        } catch (JSONException e) {
            Timber.e(e);
        }
        return bibleList;
    }

    /**
     * @param jsonObject
     * @return parse and return devotionals
     */
    public static Devotionals getDevotionals(JSONObject jsonObject) {
        Devotionals devotionals = new Devotionals();
        try {
            devotionals.setId(jsonObject.getLong("id"));
            devotionals.setTitle(jsonObject.getString("title"));
            devotionals.setBible_reading(jsonObject.getString("bible_reading"));
            devotionals.setContent(jsonObject.getString("content"));
            devotionals.setAuthor(jsonObject.getString("author"));
            devotionals.setConfession(jsonObject.getString("confession"));
            devotionals.setStudies(jsonObject.getString("studies"));
            devotionals.setDate(jsonObject.getString("date"));
            devotionals.setThumbnail(jsonObject.getString("thumbnail"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return devotionals;
    }

    /**
     * @param objArray
     * @return parse and return events arraylist
     */
    public static List<Events> getEvents(JSONArray objArray) {
        List<Events> eventsList = new ArrayList<>();
        try {
            for (int i = 0; i < objArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) objArray.get(i);
                Events events = new Events();
                events.setId(jsonObject.getLong("id"));
                events.setTitle(jsonObject.getString("title"));
                events.setThumbnail(jsonObject.getString("thumbnail"));
                events.setDetails(jsonObject.getString("details"));
                events.setDate(jsonObject.getString("date"));
                events.setTime(jsonObject.getString("time"));
                eventsList.add(events);
            }

        } catch (JSONException e) {
            Timber.e(e);
        }
        return eventsList;
    }

    /**
     * @param objArray
     * @return parse and return events arraylist
     */
    public static List<Hymns> getHymns(JSONArray objArray) {
        List<Hymns> hymnsList = new ArrayList<>();
        try {
            for (int i = 0; i < objArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) objArray.get(i);
                Hymns hymns = new Hymns();
                hymns.setId(jsonObject.getLong("id"));
                hymns.setTitle(jsonObject.getString("title"));
                hymns.setThumbnail(jsonObject.getString("thumbnail"));
                hymns.setContent(jsonObject.getString("content"));
                hymnsList.add(hymns);
            }

        } catch (JSONException e) {
            Timber.e(e);
        }
        return hymnsList;
    }

    /**
     * @param objArray
     * @return parse and return events arraylist
     */
    public static List<UserPosts> getUserPosts(JSONArray objArray) {
        List<UserPosts> userPostsList = new ArrayList<>();
        try {
            for (int i = 0; i < objArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) objArray.get(i);
                UserPosts userPosts = new UserPosts();
                userPosts.setId(jsonObject.getInt("id"));
                userPosts.setUserId(jsonObject.getInt("userId"));
                userPosts.setName(jsonObject.getString("name"));
                userPosts.setEmail(jsonObject.getString("email"));
                userPosts.setAvatar(jsonObject.getString("avatar"));
                userPosts.setCover_photo(jsonObject.getString("cover_photo"));
                JSONArray mediaArray = jsonObject.getJSONArray("media");
                List<String> mediaStrings = new ArrayList<>();
                for(int k=0; k<mediaArray.length(); k++){
                    mediaStrings.add(mediaArray.getString(k));
                }
                Gson gson = new Gson();
                String json = gson.toJson(mediaStrings);
                userPosts.setMedia(json);
                userPosts.setContent(jsonObject.getString("content"));
                userPosts.setComments_count(jsonObject.getLong("comments_count"));
                userPosts.setLikes_count(jsonObject.getLong("likes_count"));
                userPosts.setLiked(jsonObject.getBoolean("isLiked"));
                userPosts.setPinned(jsonObject.getBoolean("isPinned"));
                userPosts.setVisibility(jsonObject.getString("visibility"));
                userPosts.setTimestamp(jsonObject.getLong("timestamp"));
                userPosts.setEdited(jsonObject.getInt("edited"));
                userPostsList.add(userPosts);
            }

        } catch (JSONException e) {
            Timber.e(e);
        }
        return userPostsList;
    }


    /**
     * @param jsonObject
     * @return parse and return jsonObject
     */
    public static UserPosts getUserPost(JSONObject jsonObject) {
        UserPosts userPosts = null;
        try {
            userPosts = new UserPosts();
            userPosts.setId(jsonObject.getInt("id"));
            userPosts.setUserId(jsonObject.getInt("userId"));
            userPosts.setName(jsonObject.getString("name"));
            userPosts.setEmail(jsonObject.getString("email"));
            userPosts.setAvatar(jsonObject.getString("avatar"));
            userPosts.setCover_photo(jsonObject.getString("cover_photo"));
            JSONArray mediaArray = jsonObject.getJSONArray("media");
            List<String> mediaStrings = new ArrayList<>();
            for(int k=0; k<mediaArray.length(); k++){
                mediaStrings.add(mediaArray.getString(k));
            }
            Gson gson = new Gson();
            String json = gson.toJson(mediaStrings);
            userPosts.setMedia(json);
            userPosts.setContent(jsonObject.getString("content"));
            userPosts.setComments_count(jsonObject.getLong("comments_count"));
            userPosts.setLikes_count(jsonObject.getLong("likes_count"));
            userPosts.setLiked(jsonObject.getBoolean("isLiked"));
            userPosts.setPinned(jsonObject.getBoolean("isPinned"));
            userPosts.setVisibility(jsonObject.getString("visibility"));
            userPosts.setTimestamp(jsonObject.getLong("timestamp"));
            userPosts.setEdited(jsonObject.getInt("edited"));

        } catch (JSONException e) {
            Timber.e(e);
            userPosts = null;
        }
        return userPosts;
    }


    /**
     * @param objArray
     * @return parse and return inox arraylist
     */
    public static List<Inbox> getInbox(JSONArray objArray) {
        List<Inbox> inboxList = new ArrayList<>();
        try {
            for (int i = 0; i < objArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) objArray.get(i);
                Inbox inbox = new Inbox();
                inbox.setId(jsonObject.getLong("id"));
                inbox.setTitle(jsonObject.getString("title"));
                inbox.setMessage(jsonObject.getString("message"));
                inbox.setDate(jsonObject.getLong("date"));
                inboxList.add(inbox);
            }

        } catch (JSONException e) {
            Timber.e(e);
        }
        return inboxList;
    }

    /**
     * @param objArray
     * @return parse and return categories arraylist
     */
    public static List<Categories> getCategories(JSONArray objArray) {
        List<Categories> categoriesList = new ArrayList<>();
        try {
            for (int i = 0; i < objArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) objArray.get(i);
                Categories categories = new Categories();
                categories.setId(jsonObject.getLong("id"));
                categories.setTitle(jsonObject.getString("name"));
                categories.setThumbnail(jsonObject.getString("thumbnail"));
                categories.setMedia_count(jsonObject.getInt("media_count"));
                categoriesList.add(categories);
            }

        } catch (JSONException e) {
            Timber.e(e);
        }
        return categoriesList;
    }

    public static List<Categories> getSubCategories(JSONArray objArray) {
        List<Categories> categoriesList = new ArrayList<>();
        try {
            for (int i = 0; i < objArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) objArray.get(i);
                Categories categories = new Categories();
                //Log.e("category id",String.valueOf(jsonObject.getLong("id")));
                categories.setId(jsonObject.getLong("id"));
                categories.setTitle(jsonObject.getString("name"));
                categoriesList.add(categories);
            }

        } catch (JSONException e) {
            Timber.e(e);
        }
        return categoriesList;
    }

    /**
     * parse and return livestreams
     * @param jsonObject
     * @return
     */
    public static Livestreams getLiveStreams(JSONObject jsonObject) {
        Livestreams livestreams = new Livestreams();
        try {
            livestreams.setId(jsonObject.getLong("id"));
            livestreams.setTitle(jsonObject.getString("title"));
            livestreams.setStream_url(jsonObject.getString("stream_url"));
            livestreams.setType(jsonObject.getString("type"));
            livestreams.setStatus(jsonObject.getInt("status")==0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return livestreams;
    }

    public static Livestreams getLiveStreams(String json) {
        Livestreams livestreams = new Livestreams();
        try {
            JSONObject jsonObject = new JSONObject(json);
            livestreams.setId(jsonObject.getLong("id"));
            livestreams.setTitle(jsonObject.getString("title"));
            livestreams.setStream_url(jsonObject.getString("stream_url"));
            livestreams.setType(jsonObject.getString("type"));
            livestreams.setStatus(jsonObject.getInt("status")==0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return livestreams;
    }

    /**
     * parse and return livestreams
     * @param jsonObject
     * @return
     */
    public static Radio getRadio(JSONObject jsonObject) {
        Radio radio = new Radio();
        try {
            radio.setId(jsonObject.getLong("id"));
            radio.setTitle(jsonObject.getString("title"));
            radio.setStream_url(jsonObject.getString("stream_url"));
            radio.setCover_photo(jsonObject.getString("thumbnail"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return radio;
    }

    /**
     * parse and return media arraylist
     * @param jsonArray
     * @return
     */
    public static List<Media> getMedia(JSONArray jsonArray) {
        List<Media> mediaList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                Media media = new Media();
                media.setId(jsonObject.getLong("id"));
                media.setCategory(jsonObject.getString("category"));
                media.setTitle(jsonObject.getString("title"));
                media.setCover_photo(jsonObject.getString("cover_photo"));
                media.setDescription(jsonObject.getString("description"));
                media.setStream_url(jsonObject.getString("stream"));
                media.setDownload_url(jsonObject.getString("download"));
                media.setDuration(jsonObject.getInt("duration"));
                media.setCan_preview(jsonObject.getInt("can_preview") == 0);
                media.setCan_download(jsonObject.getInt("can_download") == 0);
                media.setPreview_duration(jsonObject.getLong("preview_duration"));
                media.setMedia_type(jsonObject.getString("type"));
                media.setVideo_type(jsonObject.getString("video_type"));
                media.setComments_count(jsonObject.getLong("comments_count"));
                media.setLikes_count(jsonObject.getLong("likes_count"));
                media.setViews_count(jsonObject.getLong("views_count"));
                media.setUserLiked(jsonObject.getBoolean("user_liked"));
                media.setIs_free(jsonObject.getInt("is_free") == 0);
                media.setHttp(true);
                mediaList.add(media);
            }

        } catch (JSONException e) {
            Timber.e(e);
        }
        return mediaList;
    }

    /**
     * get single media from json response
     * @param response
     * @return
     */
    public static Media getMedia(String response) {
        Media media = new Media();
        try {
            JSONObject jsonObject = new JSONObject(response);
            media.setId(jsonObject.getLong("id"));
            media.setCategory(jsonObject.getString("category"));
            media.setTitle(jsonObject.getString("title"));
            media.setCover_photo(jsonObject.getString("cover_photo"));
            media.setDescription(jsonObject.getString("description"));
            media.setStream_url(jsonObject.getString("stream"));
            media.setDownload_url(jsonObject.getString("download"));
            media.setDuration(jsonObject.getInt("duration"));
            media.setCan_preview(jsonObject.getInt("can_preview") == 0);
            media.setCan_download(jsonObject.getInt("can_download") == 0);
            media.setPreview_duration(jsonObject.getLong("preview_duration"));
            media.setMedia_type(jsonObject.getString("type"));
            media.setVideo_type(jsonObject.getString("video_type"));
            media.setComments_count(jsonObject.getLong("comments_count"));
            media.setLikes_count(jsonObject.getLong("likes_count"));
            media.setViews_count(jsonObject.getLong("views_count"));
            media.setUserLiked(jsonObject.getBoolean("user_liked"));
            media.setIs_free(jsonObject.getInt("is_free") == 0);
            media.setHttp(true);

        } catch (JSONException e) {
            Timber.e(e);
        }
        return media;
    }

    /**
     * get single inbox from json response
     * @param response
     * @return
     */
    public static Inbox getInbox(String response) {
        Inbox inbox = new Inbox();
        try {
            JSONObject jsonObject = new JSONObject(response);
            inbox.setId(jsonObject.getLong("id"));
            inbox.setTitle(jsonObject.getString("title"));
            inbox.setMessage(jsonObject.getString("message"));
            inbox.setDate(jsonObject.getLong("date"));

        } catch (JSONException e) {
            Timber.e(e);
        }
        return inbox;
    }

    /**
     * get single events from json response
     * @param response
     * @return
     */
    public static Events getEvents(String response) {
        Events events = new Events();
        try {
            JSONObject jsonObject = new JSONObject(response);
            events.setId(jsonObject.getLong("id"));
            events.setTitle(jsonObject.getString("title"));
            events.setThumbnail(jsonObject.getString("thumbnail"));
            events.setDetails(jsonObject.getString("details"));
            events.setDate(jsonObject.getString("date"));
            events.setTime(jsonObject.getString("time"));

        } catch (JSONException e) {
            Timber.e(e);
        }
        return events;
    }

    /**
     * get userdata from json response
     * @param context
     * @param response
     * @return
     */
    public static UserData getUser(Context context, JSONObject response) {
        UserData userData = new UserData();
        try {
            JSONObject jsonObject = response.getJSONObject("user");
            userData.setName(jsonObject.getString("name"));
            userData.setEmail(jsonObject.getString("email"));
            userData.setBlocked(jsonObject.getInt("blocked") == 0);
            userData.setVerified(jsonObject.getInt("verified") == 0);
            userData.setSubscribed(jsonObject.getInt("subscribed") == 0);
            userData.setSubscriptionExpirydate(Long.parseLong(jsonObject.getString("subscribe_expiry_date")));
            userData.setSubscription_plan(jsonObject.getString("subscribe_plan"));
            if(jsonObject.getInt("activated") == 0){
                userData.setAbout_me(jsonObject.getString("about_me"));
                userData.setPhone(jsonObject.getString("phone"));
                userData.setGender(jsonObject.getString("gender"));
                userData.setActivated(true);
                userData.setDate_of_birth(jsonObject.getString("date_of_birth"));
                userData.setAvatar(jsonObject.getString("avatar"));
                userData.setCover_photo(jsonObject.getString("cover_photo"));
                userData.setFacebook(jsonObject.getString("facebook"));
                userData.setTwitter(jsonObject.getString("twitter"));
                userData.setLinkdln(jsonObject.getString("linkdln"));
                userData.setQualification(jsonObject.getString("qualification"));
                userData.setLocation(jsonObject.getString("location"));
            }
        } catch (JSONException e) {
            Timber.e(e);
        }

        //we check if the logged in user have an active subscription and assign to user
        //if you do not wish users to retain premium services on multiple devices
        //comment out out the statements below
        if(userData.isSubscribed()){
            Utility.setUserBillingData(context,userData.getSubscription_plan(),userData.getSubscriptionExpirydate());
        }

        //update user fcm token for sending notifications
        if(userData.isActivated()){
            App.updateUserSocialToken(userData.getEmail());
        }

        return userData;
    }

    /**
     * get userdata from json response
     * @param response
     * @return
     */
    public static UserData getUpdatedUser(JSONObject response) {
        UserData userData = new UserData();
        try {
            JSONObject jsonObject = response.getJSONObject("user");
            userData.setName(jsonObject.getString("name"));
            userData.setEmail(jsonObject.getString("email"));
            userData.setBlocked(jsonObject.getInt("blocked") == 0);
            userData.setVerified(jsonObject.getInt("verified") == 0);
            userData.setSubscribed(jsonObject.getInt("subscribed") == 0);
            userData.setSubscriptionExpirydate(Long.parseLong(jsonObject.getString("subscribe_expiry_date")));
            userData.setSubscription_plan(jsonObject.getString("subscribe_plan"));
            userData.setAbout_me(jsonObject.getString("about_me"));
            userData.setPhone(jsonObject.getString("phone"));
            userData.setGender(jsonObject.getString("gender"));
            userData.setActivated(true);
            userData.setDate_of_birth(jsonObject.getString("date_of_birth"));
            userData.setAvatar(jsonObject.getString("avatar"));
            userData.setCover_photo(jsonObject.getString("cover_photo"));
            userData.setFacebook(jsonObject.getString("facebook"));
            userData.setTwitter(jsonObject.getString("twitter"));
            userData.setLinkdln(jsonObject.getString("linkdln"));
            userData.setQualification(jsonObject.getString("qualification"));
            userData.setLocation(jsonObject.getString("location"));
        } catch (JSONException e) {
            Timber.e(e);
            Log.e("user error",e.getLocalizedMessage());
        }

        return userData;
    }

    /**
     * @param objArray
     * @return parse and return events arraylist
     */
    public static List<UserData> getUsers(JSONArray objArray) {
        List<UserData> userDataList = new ArrayList<>();
        try {
            for (int i = 0; i < objArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) objArray.get(i);
                UserData userData = new UserData();
                userData.setName(jsonObject.getString("name"));
                userData.setEmail(jsonObject.getString("email"));
                userData.setBlocked(false);
                userData.setVerified(false);
                userData.setSubscribed(false);
                userData.setSubscriptionExpirydate(0);
                userData.setSubscription_plan("");
                userData.setAbout_me(jsonObject.getString("about_me"));
                userData.setPhone(jsonObject.getString("phone"));
                userData.setGender(jsonObject.getString("gender"));
                userData.setActivated(true);
                userData.setDate_of_birth(jsonObject.getString("date_of_birth"));
                userData.setAvatar(jsonObject.getString("avatar"));
                userData.setCover_photo(jsonObject.getString("cover_photo"));
                userData.setFacebook(jsonObject.getString("facebook"));
                userData.setTwitter(jsonObject.getString("twitter"));
                userData.setLinkdln(jsonObject.getString("linkdln"));
                userData.setQualification(jsonObject.getString("qualification"));
                userData.setLocation(jsonObject.getString("location"));
                userData.setFollowing(jsonObject.getInt("following") == 0);
                userDataList.add(userData);
            }

        } catch (JSONException e) {
            Log.e("parse users error",e.getLocalizedMessage());
            Timber.e(e);
        }
        return userDataList;
    }

    /**
     * parse and return comments arraylist from json response
     * @param response
     * @return
     */
    public static List<Comments> getComments(JSONObject response) {
        List<Comments> commentsList = new ArrayList<>();
        try {

            JSONArray videoArray = response.getJSONArray("comments");
            for (int i = 0; i < videoArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) videoArray.get(i);
                Comments comments = new Comments();
                comments.setId(jsonObject.getLong("id"));
                comments.setMedia_id(jsonObject.getLong("media_id"));
                comments.setEmail(jsonObject.getString("email"));
                comments.setName(jsonObject.getString("name"));
                comments.setAvatar(jsonObject.getString("avatar"));
                comments.setCover_photo(jsonObject.getString("cover_photo"));
                comments.setContent(Utility.getBase64DEcodedString(jsonObject.getString("content")));
                comments.setDate(jsonObject.getLong("date"));
                comments.setReplies(jsonObject.getInt("replies"));
                comments.setEdited(jsonObject.getInt("edited"));
                commentsList.add(comments);
            }

        } catch (JSONException e) {
            Timber.e(e);
        }
        return commentsList;
    }

    /**
     * get comment from json response
     * @param response
     * @return
     */
    public static Comments getComment(JSONObject response) {
        Comments comments = new Comments();
        try {
            JSONObject jsonObject = (JSONObject) response.getJSONObject("comment");
            comments.setId(jsonObject.getLong("id"));
            comments.setMedia_id(jsonObject.getLong("media_id"));
            comments.setEmail(jsonObject.getString("email"));
            comments.setAvatar(jsonObject.getString("avatar"));
            comments.setCover_photo(jsonObject.getString("cover_photo"));
            comments.setName(jsonObject.getString("name"));
            comments.setContent(Utility.getBase64DEcodedString(jsonObject.getString("content")));
            comments.setDate(jsonObject.getLong("date"));
            comments.setReplies(jsonObject.getInt("replies"));
            comments.setEdited(jsonObject.getInt("edited"));

        } catch (JSONException e) {
            Timber.e(e);
        }
        return comments;
    }

    /**
     * parse and return replies arraylist from json response
     * @param response
     * @return
     */
    public static List<Replies> getReplies(JSONObject response) {
        List<Replies> commentsList = new ArrayList<>();
        try {

            JSONArray videoArray = response.getJSONArray("comments");
            for (int i = 0; i < videoArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) videoArray.get(i);
                Replies comments = new Replies();
                comments.setId(jsonObject.getLong("id"));
                comments.setComment_id(jsonObject.getLong("comment_id"));
                comments.setAvatar(jsonObject.getString("avatar"));
                comments.setCover_photo(jsonObject.getString("cover_photo"));
                comments.setEmail(jsonObject.getString("email"));
                comments.setName(jsonObject.getString("name"));
                comments.setContent(Utility.getBase64DEcodedString(jsonObject.getString("content")));
                comments.setDate(jsonObject.getLong("date"));
                comments.setEdited(jsonObject.getInt("edited"));
                commentsList.add(comments);
            }

        } catch (JSONException e) {
            Timber.e(e);
        }
        return commentsList;
    }

    /**
     * get reply from json response
     * @param response
     * @return
     */
    public static Replies getReply(JSONObject response) {
        Replies comments = new Replies();
        try {
            JSONObject jsonObject = (JSONObject) response.getJSONObject("comment");
            comments.setId(jsonObject.getLong("id"));
            comments.setComment_id(jsonObject.getLong("comment_id"));
            comments.setEmail(jsonObject.getString("email"));
            comments.setAvatar(jsonObject.getString("avatar"));
            comments.setCover_photo(jsonObject.getString("cover_photo"));
            comments.setName(jsonObject.getString("name"));
            comments.setContent(Utility.getBase64DEcodedString(jsonObject.getString("content")));
            comments.setDate(jsonObject.getLong("date"));
            comments.setEdited(jsonObject.getInt("edited"));

        } catch (JSONException e) {
            Timber.e(e);
        }
        return comments;
    }

    /**
     * parse and return comments arraylist from json response
     * @param response
     * @return
     */
    public static List<Comments> getPostsComments(JSONObject response) {
        List<Comments> commentsList = new ArrayList<>();
        try {

            JSONArray videoArray = response.getJSONArray("comments");
            for (int i = 0; i < videoArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) videoArray.get(i);
                Comments comments = new Comments();
                comments.setId(jsonObject.getLong("id"));
                comments.setMedia_id(jsonObject.getLong("post_id"));
                comments.setEmail(jsonObject.getString("email"));
                comments.setName(jsonObject.getString("name"));
                comments.setAvatar(jsonObject.getString("avatar"));
                comments.setCover_photo(jsonObject.getString("cover_photo"));
                comments.setContent(Utility.getBase64DEcodedString(jsonObject.getString("content")));
                comments.setDate(jsonObject.getLong("date"));
                comments.setReplies(jsonObject.getInt("replies"));
                comments.setEdited(jsonObject.getInt("edited"));
                commentsList.add(comments);
            }

        } catch (JSONException e) {
            Timber.e(e);
        }
        return commentsList;
    }

    /**
     * get comment from json response
     * @param response
     * @return
     */
    public static Comments getPostComment(JSONObject response) {
        Comments comments = new Comments();
        try {
            JSONObject jsonObject = (JSONObject) response.getJSONObject("comment");
            comments.setId(jsonObject.getLong("id"));
            comments.setMedia_id(jsonObject.getLong("post_id"));
            comments.setEmail(jsonObject.getString("email"));
            comments.setAvatar(jsonObject.getString("avatar"));
            comments.setCover_photo(jsonObject.getString("cover_photo"));
            comments.setName(jsonObject.getString("name"));
            comments.setContent(Utility.getBase64DEcodedString(jsonObject.getString("content")));
            comments.setDate(jsonObject.getLong("date"));
            comments.setReplies(jsonObject.getInt("replies"));
            comments.setEdited(jsonObject.getInt("edited"));

        } catch (JSONException e) {
            Timber.e(e);
        }
        return comments;
    }

    /**
     * parse and return comments arraylist from json response
     * @param response
     * @return
     */
    public static List<UserNotifications> getUserNotifications(JSONObject response) {
        List<UserNotifications> userNotificationsList = new ArrayList<>();
        try {

            JSONArray videoArray = response.getJSONArray("notifications");
            for (int i = 0; i < videoArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) videoArray.get(i);
                UserNotifications userNotifications = new UserNotifications();
                userNotifications.setId(jsonObject.getLong("id"));
                userNotifications.setItem(jsonObject.getLong("itm_id"));
                userNotifications.setEmail(jsonObject.getString("email"));
                userNotifications.setName(jsonObject.getString("name"));
                userNotifications.setAvatar(jsonObject.getString("avatar"));
                userNotifications.setCover_photo(jsonObject.getString("cover_photo"));
                userNotifications.setMessage(jsonObject.getString("message"));
                userNotifications.setType(jsonObject.getString("type"));
                userNotifications.setTimestamp(jsonObject.getLong("timestamp"));
                if(jsonObject.has("post")){
                    userNotifications.setPost_data(new Gson().toJson(getUserPost(jsonObject.getJSONObject("post"))));
                }
                userNotificationsList.add(userNotifications);
            }

        } catch (JSONException e) {
            Timber.e(e);
        }
        return userNotificationsList;
    }


    /**
     * parse and return comments arraylist from json response
     * @param response
     * @return
     */
    public static List<Branches> getBranches(JSONObject response) {
        List<Branches> branchesList = new ArrayList<>();
        try {

            JSONArray videoArray = response.getJSONArray("branches");
            for (int i = 0; i < videoArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) videoArray.get(i);
                Branches branches = new Branches();
                branches.setAddress(jsonObject.getString("address"));
                branches.setName(jsonObject.getString("name"));
                branches.setPastor(jsonObject.getString("pastor"));
                branches.setPhone(jsonObject.getString("phone"));
                branches.setEmail(jsonObject.getString("email"));
                branches.setLatitude(jsonObject.getDouble("latitude"));
                branches.setLongitude(jsonObject.getDouble("longitude"));
                branchesList.add(branches);
            }

        } catch (JSONException e) {
            Timber.e(e);
        }
        return branchesList;
    }
}