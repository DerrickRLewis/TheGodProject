package apps.envision.mychurch.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.interfaces.CommentsListener;
import apps.envision.mychurch.interfaces.UserPostsListener;
import apps.envision.mychurch.libs.RotateLoading;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.Comments;
import apps.envision.mychurch.pojo.LikeUpdate;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.pojo.UserPosts;
import apps.envision.mychurch.socials.UsersDataActivity;
import apps.envision.mychurch.socials.UsersProfileActivity;
import apps.envision.mychurch.socials.adapters.PostsCommentsAdapter;
import apps.envision.mychurch.socials.fragments.PostRepliesFragment;
import apps.envision.mychurch.ui.adapters.CommentsAdapter;
import apps.envision.mychurch.ui.fragments.RepliesFragment;
import apps.envision.mychurch.utils.JsonParser;
import apps.envision.mychurch.utils.Utility;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostsCommentsActivity extends AppCompatActivity implements LocalMessageCallback, CommentsListener, View.OnClickListener, UserPostsListener {

    private Toolbar toolbar;
    private RecyclerView recyclerview;
    private PostsCommentsAdapter commentsAdapter;
    private ArrayList<Comments> commentsList = new ArrayList<>();
    private RelativeLayout container,bottom_sheet;
    private UserData userData;
    private UserPosts userPosts;
    private int post_position = -1;
    private AppCompatEditText comment;
    private RotateLoading rotateLoading;
    private ImageView send;

    private boolean editmode = false;
    private Comments edit_comment;
    private int edit_position = 0;
    private Utility utility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        utility = new Utility(this);
        setContentView(R.layout.activity_comments);

        if(getIntent().getStringExtra("post")!=null) {
            Gson gson = new Gson();
            userPosts = gson.fromJson(getIntent().getStringExtra("post"), UserPosts.class);
            post_position = getIntent().getIntExtra("post_position",-1);
        }else{
            finish();
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.comments));

        //initialise views
        init_views();
        userData = PreferenceSettings.getUserData();
        if(userData==null  || userData.isBlocked()){
            hide_add_comment_layout();
        }

        //fetch comments
        load_comments(true);
    }

    private void init_views(){
        container = findViewById(R.id.container);
        bottom_sheet = findViewById(R.id.bottom_sheet);
        comment = findViewById(R.id.comment);
        send = findViewById(R.id.send);
        send.setOnClickListener(this);
        rotateLoading = findViewById(R.id.rotateLoading);
        recyclerview = findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerview.setHasFixedSize(true);
        //recyclerView.setFocusable(false);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        commentsAdapter = new PostsCommentsAdapter(this, this);
        recyclerview.setAdapter(commentsAdapter);
        commentsAdapter.setHeader(userPosts);
        commentsAdapter.fetchLoading();
    }

    /**
     * method to hide bottom download viewer
     * and set bottom margin to our container
     */
    private void hide_add_comment_layout(){
        bottom_sheet.setVisibility(View.GONE);
        setMargins(recyclerview,0);
    }

    private void setMargins (View view, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.bottomMargin = bottom;
            view.requestLayout();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                if(editmode){
                    toggleEditMode();
                }else{
                    this.finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * toggle between make comment and edit comment mode
     */
    private void toggleEditMode(){
        if(editmode){
            editmode = false;
            edit_comment = null;
            getSupportActionBar().setTitle(getString(R.string.comments));
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
            comment.setText("");
            edit_position = 0;
        }else{
            getSupportActionBar().setTitle(getString(R.string.edit_comment));
            toolbar.setNavigationIcon(R.drawable.ic_undo_white_24dp);
            editmode = true;
            comment.setText(edit_comment.getContent());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalMessageManager.getInstance().addListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalMessageManager.getInstance().removeListener(this);
    }

    /**
     * @param localMessage event message
     *  subscribe to updates to ongoing download progress
     */
    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
        switch (localMessage.getId()){
            case R.id.update_comment_replies_count:
                Bundle bundle = (Bundle) localMessage.getObject();
                if(bundle!=null && bundle.containsKey("position") && bundle.containsKey("comment")){
                    Gson gson = new Gson();
                   Comments _comments = gson.fromJson(bundle.getString("comment"), Comments.class);
                   commentsAdapter.setEditedComment(_comments,bundle.getInt("position"));
                }
                break;
        }

    }

    /**
     * load replies of a comment
     * @param comments
     * @param edit_position`
     */
    @Override
    public void viewReplies(Comments comments, int edit_position) {
        PostRepliesFragment repliesFragment = PostRepliesFragment.newInstance(comments,edit_position);
        repliesFragment.show(getSupportFragmentManager(),repliesFragment.getTag());
    }

    /**
     * display edit comment view when user clicks on edit comment button
     * @param comments
     * @param edit_position
     */
    @Override
    public void editComment(Comments comments, int edit_position) {
        if(!editmode) {
            this.edit_position = edit_position;
            edit_comment = comments;
            toggleEditMode();
        }
    }

    /**
     * show alert if user wants to delete a comment
     * @param id
     * @param edit_position
     */
    @Override
    public void deleteComment(long id,int edit_position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.delete_comment));
        builder.setMessage(getString(R.string.delete_comment_hint));

        builder.setPositiveButton("Delete",
                (dialog, which) -> {
                    // positive button logic
                    dialog.dismiss();
                    delete_comment(id,edit_position);
                });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        // display dialog
        dialog.show();
    }

    /**
     * load more comments
     */
    @Override
    public void loadMore() {
       commentsAdapter.setLoading();
       load_comments(false);
    }

    @Override
    public boolean isUserComment(String email) {
        return userData != null && userData.getEmail().equalsIgnoreCase(email) && !userData.isBlocked();
    }

    /**
     * show report comments options for user
     * @param comments
     * @param position
     */
    @Override
    public void reportComment(Comments comments, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PostsCommentsActivity.this);
        builder.setTitle(getString(R.string.report_comment));
        final int[] selected = {0};

        //list of items
        String[] items = getResources().getStringArray(R.array.report_comment_options);
        builder.setSingleChoiceItems(items, 0,
                (dialog, which) -> {
                    // item selected logic
                    selected[0] = which;
                });

        builder.setPositiveButton(getString(R.string.report),
                (dialog, which) -> {
                    // positive button logic
                    report_comment(comments.getId(),position,items[selected[0]]);
                });

        builder.setNegativeButton( getString(android.R.string.cancel),
                (dialog, which) -> {
                    // negative button logic
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.send:
                construct_comment();
                break;
        }
    }

    /**
     * construct and send a new comment to the server
     */
    private void construct_comment(){
        String comment_ = Objects.requireNonNull(comment.getText()).toString().trim();
        if(editmode && comment_.equalsIgnoreCase(""))toggleEditMode();
        if(comment_.equalsIgnoreCase(""))return;

        try {
            show_make_comment_loader();
            JSONObject jsonData = new JSONObject();
            jsonData.put("content", Utility.getBase64EncodedString(comment_));
            jsonData.put("email", userData.getEmail());
            jsonData.put("user", userPosts.getEmail());
            jsonData.put("post", userPosts.getId());
            if(editmode)jsonData.put("id", edit_comment.getId());
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = editmode?service.editpostcomment(requestBody):service.makepostcomment(requestBody);

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response", String.valueOf(response.body()));
                    hide_make_comment_loader();
                    if(response.body()==null){
                        construct_comment_error();
                        return;
                    }

                    try {
                        JSONObject jsonObj = new JSONObject(response.body());
                        String status = jsonObj.getString("status");
                        if (status.equalsIgnoreCase("ok")) {
                            Comments comments = JsonParser.getPostComment(jsonObj);
                            if(editmode){
                                commentsList.set(commentsList.indexOf(edit_comment),comments);
                                commentsAdapter.setEditedComment(comments,edit_position);
                                toggleEditMode();
                            }else{
                                commentsList.add(0,comments);
                                commentsAdapter.setNewComment(comments);
                                recyclerview.scrollToPosition(commentsList.size());
                                comment.setText("");
                                update_comments_count(jsonObj.getInt("total_count"));
                            }
                        }else{
                            Toast.makeText(App.getContext(),jsonObj.getString("message"),Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("Error", e.toString());
                        construct_comment_error();
                    }

                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    hide_make_comment_loader();
                    construct_comment_error();
                    Log.e("error", String.valueOf(throwable.getMessage()));
                }
            });
        }catch (JSONException e) {
            hide_make_comment_loader();
            construct_comment_error();
            e.printStackTrace();
        }

    }

    private void construct_comment_error(){
        if(editmode){
            Toast.makeText(App.getContext(), getString(R.string.edit_comment_error), Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(App.getContext(), getString(R.string.make_comment_error), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * load comments from our remote server
     * @param load_initial
     */
    private void load_comments(boolean load_initial){
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("id", get_top_comment_id());
            jsonData.put("post", userPosts.getId());
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = service.loadpostcomments(requestBody);

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response", String.valueOf(response.body()));
                    if(response.body()==null){
                        fetch_comment_error_response();
                        return;
                    }

                    try {
                        JSONObject jsonObj = new JSONObject(response.body());
                        String status = jsonObj.getString("status");
                        if (status.equalsIgnoreCase("ok")) {
                            commentsAdapter.removeLoader();

                            List<Comments> comments = JsonParser.getPostsComments(jsonObj);
                            if(comments.size()>0){
                                commentsList.addAll(comments);
                                commentsAdapter.setMoreAdapter(comments);
                            }
                            boolean has_more = jsonObj.getBoolean("has_more");
                            Log.e("has_more",String.valueOf(has_more));
                            if(has_more)commentsAdapter.setLoadMore(false);
                            if(load_initial)recyclerview.scrollToPosition(commentsList.size());
                            else recyclerview.scrollToPosition(comments.size());

                            update_comments_count(jsonObj.getInt("total_count"));
                        }else{
                            fetch_comment_error_response();
                        }
                    } catch (JSONException e) {
                        Log.e("Error", e.toString());
                        fetch_comment_error_response();
                    }

                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    Log.e("error", String.valueOf(throwable.getMessage()));
                    fetch_comment_error_response();
                }
            });
        }catch (JSONException e) {
            Log.e("parse error",e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * send a request to delete a comment on our remote server
     * @param id
     * @param edit_position
     */
    private void delete_comment(long id,int edit_position){
        if(editmode)toggleEditMode();

        try {
            utility.show_loader();
            JSONObject jsonData = new JSONObject();
            jsonData.put("id", id);
            jsonData.put("post", userPosts.getId());
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = service.deletepostcomment(requestBody);

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response", String.valueOf(response.body()));
                    utility.hide_loader();
                    if(response.body()==null){
                        Toast.makeText(App.getContext(),getString(R.string.delete_comment_error),Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        JSONObject jsonObj = new JSONObject(response.body());
                        String status = jsonObj.getString("status");
                        if (status.equalsIgnoreCase("ok")) {
                           commentsAdapter.deleteComment(edit_position);
                           update_comments_count(jsonObj.getInt("total_count"));
                        }else{
                            Toast.makeText(App.getContext(),jsonObj.getString("message"),Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("Error", e.toString());
                        Toast.makeText(App.getContext(),getString(R.string.delete_comment_error),Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    Toast.makeText(App.getContext(),getString(R.string.delete_comment_error),Toast.LENGTH_SHORT).show();
                    Log.e("error", String.valueOf(throwable.getMessage()));
                    utility.hide_loader();
                }
            });
        }catch (JSONException e) {
            Toast.makeText(App.getContext(),getString(R.string.delete_comment_error),Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    /**
     * send a request to report a comment to our remote server
     * @param id
     * @param edit_position
     * @param reason
     */
    private void report_comment(long id,int edit_position, String reason){
        if(editmode)toggleEditMode();

        try {
            utility.show_loader();
            JSONObject jsonData = new JSONObject();
            jsonData.put("id", id);
            jsonData.put("email", userData.getEmail());
            jsonData.put("type","post_comments");
            jsonData.put("reason",reason);
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = service.reportpostcomment(requestBody);

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response", String.valueOf(response.body()));
                    utility.hide_loader();
                    if(response.body()==null){
                        Toast.makeText(App.getContext(),getString(R.string.report_comment_error),Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        JSONObject jsonObj = new JSONObject(response.body());
                        String status = jsonObj.getString("status");
                        if (status.equalsIgnoreCase("ok")) {
                            commentsAdapter.deleteComment(edit_position);
                            update_comments_count((int) userPosts.getComments_count()-1);
                        }else{
                            Toast.makeText(App.getContext(),jsonObj.getString("message"),Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("Error", e.toString());
                        Toast.makeText(App.getContext(),getString(R.string.report_comment_error),Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    Toast.makeText(App.getContext(),getString(R.string.report_comment_error),Toast.LENGTH_SHORT).show();
                    Log.e("error", String.valueOf(throwable.getMessage()));
                    utility.hide_loader();
                }
            });
        }catch (JSONException e) {
            Toast.makeText(App.getContext(),getString(R.string.report_comment_error),Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    private void fetch_comment_error_response(){
        if(commentsList.size()==0){
            commentsAdapter.removeLoader();
            commentsAdapter.setInfo(getString(R.string.no_comments));
        }else{
            commentsAdapter.setLoadMore(true);
            Toast.makeText(App.getContext(),getString(R.string.cant_load_more_comments),Toast.LENGTH_SHORT).show();
        }
    }

    private void show_make_comment_loader(){
        rotateLoading.setVisibility(View.VISIBLE);
        rotateLoading.start();
        send.setVisibility(View.GONE);
    }

    private void hide_make_comment_loader(){
        rotateLoading.setVisibility(View.GONE);
        send.setVisibility(View.VISIBLE);
    }

    private long get_top_comment_id(){
        if(commentsList.size()==0)return 0;
        Comments comments = commentsList.get(commentsList.size()-1);
        return comments.getId();
    }

    private void update_comments_count(int count){
        //we only update if the comments count changed
        if(userPosts.getComments_count() != count){
            userPosts.setComments_count(count);
            //notify activities of comment count change
            commentsAdapter.updateCommentsHeader(userPosts);
            LocalMessageManager.getInstance().send(R.id.update_post_comment_count, post_position, count);//TODO
        }
    }

    @Override
    public FragmentActivity getPostActivity() {
        return PostsCommentsActivity.this;
    }

    /**
     * load the comments activity
     * @param userPosts
     */
    @Override
    public void startCommentsActivity(UserPosts userPosts, int position) {
        Gson gson = new Gson();
        String myJson = gson.toJson(userPosts);
        Intent intent = new Intent(this, PostsCommentsActivity.class);
        intent.putExtra("post", myJson);
        startActivity(intent);
    }

    /**
     * method to handle userPosts likes
     * and update userPosts likes count
     */
    @Override
    public void likePost(UserPosts userPosts, String action, int position){
        Utility.likeunlikepost(userPosts, action, position,true);
    }

    @Override
    public void pinPost(UserPosts userPosts, String action){
        Utility.pinunpinpost(userPosts, action);
    }

    @Override
    public void editPost(UserPosts userPosts, int position) {

    }

    @Override
    public void deletePost(UserPosts userPosts, int position) {

    }

    @Override
    public void view_profile(UserData userData) {
        Intent intent;
        Gson gson = new Gson();
        String myJson = gson.toJson(userData);
        intent = new Intent(this, UsersProfileActivity.class);
        intent.putExtra("userdata", myJson);
        startActivity(intent);
    }

    @Override
    public void view_likes(UserPosts userPosts) {
        Intent intent;
        Gson gson = new Gson();
        String userpost = gson.toJson(userPosts);
        String option = gson.toJson(UsersDataActivity.OPTIONS.LIKES);
        intent = new Intent(PostsCommentsActivity.this, UsersDataActivity.class);
        intent.putExtra("userpost", userpost);
        intent.putExtra("option", option);
        startActivity(intent);
    }
}
