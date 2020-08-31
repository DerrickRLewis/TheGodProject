package apps.envision.mychurch.socials.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.interfaces.UserPostsListener;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.libs.pullrefresh.PullRefreshLayout;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.pojo.UserPosts;
import apps.envision.mychurch.socials.EditPostActivity;
import apps.envision.mychurch.socials.UsersDataActivity;
import apps.envision.mychurch.socials.UsersProfileActivity;
import apps.envision.mychurch.socials.adapters.UserPostAdapter;
import apps.envision.mychurch.ui.activities.PostsCommentsActivity;
import apps.envision.mychurch.utils.JsonParser;
import apps.envision.mychurch.utils.PostPaginationScrollListener;
import apps.envision.mychurch.utils.Utility;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserDetailedPostsFragment extends Fragment implements UserPostsListener, LocalMessageCallback {

    //for loading more items
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = 0;
    private static final String ARG_PARAM1 = "param1";
    private PullRefreshLayout pullRefreshLayout;
    private List<UserPosts> userPostsList = new ArrayList<>();
    private UserPostAdapter userPostAdapter;
    private boolean fragmentVisible = false;
    private View view;
    private int current_video_player_position = 0;
    private UserData userData, thisUser;
    private Utility utility;

    public UserDetailedPostsFragment() {
    }

    public static UserDetailedPostsFragment newInstance(String user) {
        UserDetailedPostsFragment fragment = new UserDetailedPostsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Gson gson = new Gson();
            userData = gson.fromJson(getArguments().getString(ARG_PARAM1), UserData.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_posts_list, container, false);
        thisUser = PreferenceSettings.getUserData();
        utility = new Utility(getActivity());

        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        userPostAdapter = new UserPostAdapter(this);
        recyclerView.setAdapter(userPostAdapter);
        recyclerView.addOnScrollListener(new PostPaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                if(userPostsList.size()>=20) {
                    isLoading = true;
                    currentPage += 1;
                    userPostAdapter.setLoader();
                    fetch_posts();
                }
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }

            @Override
            public int getPlayerPosition() {
                return current_video_player_position;
            }
        });

        //check for new hymns
        init_pullrefresh();
        return view;
    }

    //init pullrefresh
    private void init_pullrefresh(){
        pullRefreshLayout = view.findViewById(R.id.pullRefreshLayout);
        int[] colorScheme = getResources().getIntArray(R.array.refresh_color_scheme);
        pullRefreshLayout.setRefreshStyle();
        pullRefreshLayout.setColorSchemeColors(colorScheme);
        // listen refresh event
        pullRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 0;
            fetch_posts();
        });
        pullRefreshLayout.setRefreshing(true);
        fetch_posts();
    }

    /**
     * display empty message if our data list is empty
     * @param msg
     */
    private void display_message(String msg){
        if(userPostAdapter !=null)
            userPostAdapter.setInfo(msg);
    }

    private void fetch_posts(){
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("page",currentPage);
            jsonData.put("email", userData.getEmail());
            jsonData.put("viewer", thisUser.getEmail());

            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = service.fetchUserPosts(requestBody);

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.d("response", String.valueOf(response.body()));
                    remove_loader();
                    if (response.body() == null) {
                        if(currentPage == 0) {
                            display_message(getString(R.string.no_data));
                        }
                        return;
                    }
                    try {
                        JSONObject jsonObj = new JSONObject(response.body());
                        String status = jsonObj.getString("status");
                        if (status.equalsIgnoreCase("ok")) {
                            List<UserPosts> mediaList = JsonParser.getUserPosts(jsonObj.getJSONArray("posts"));
                            parseJsonResponse(mediaList);
                            isLastPage = jsonObj.getBoolean("isLastPage");
                        }
                    } catch (JSONException e) {
                        Log.e("Error", e.toString());
                        if(currentPage == 0) {
                            display_message(getString(R.string.no_data));
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    //System.out.println(throwable);
                    Log.e("error", String.valueOf(throwable.getMessage()));
                    if(!fragmentVisible)return;
                    remove_loader();
                    if(currentPage == 0) {
                        display_message(getString(R.string.no_data));
                    }

                    //setNetworkError();
                }
            });
        }catch (JSONException e) {
            Log.e("parse error",e.getMessage());
            e.printStackTrace();
        }
    }

    private void parseJsonResponse(List<UserPosts> itms){
        if(currentPage==0 && itms.size() == 0){
            display_message(getResources().getString(R.string.no_data));
        }else {
            if(currentPage==0) {
                userPostsList = new ArrayList<>();
                userPostAdapter.setAdapter(itms);
            }
            else userPostAdapter.setMoreData(itms);
            userPostsList.addAll(itms);
        }
    }

    private void remove_loader(){
        if(currentPage==0){
            pullRefreshLayout.setRefreshing(false);
        }else{
            isLoading = false;
            userPostAdapter.setLoaded();
        }
    }

    @Override
    public void onStart() {
        fragmentVisible = true;
        LocalMessageManager.getInstance().addListener(this);
        super.onStart();
    }

    @Override
    public void onDestroy() {
        fragmentVisible = false;
        LocalMessageManager.getInstance().removeListener(this);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        fragmentVisible = false;
        super.onDetach();
    }


    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
        switch (localMessage.getId()){
            case R.id.reload_posts_view:
                pullRefreshLayout.setRefreshing(true);
                currentPage = 0;
                fetch_posts();
                break;
            case R.id.current_video_player_position:
                current_video_player_position = localMessage.getArg1();
                break;
            case R.id.update_post_comment_count:
                int post_position =  localMessage.getArg1();
                int count =  localMessage.getArg2();
                userPostAdapter.updatePostCommentCount(post_position, count);
                break;
            case R.id.update_edited_post:
                UserPosts userPosts = (UserPosts) localMessage.getObject();
                int _adapterPos =  userPosts.getAdapterPosition();
                userPostAdapter.setEditedPost(userPosts,_adapterPos);
                break;
        }
    }

    @Override
    public FragmentActivity getPostActivity() {
        return getActivity();
    }

    /**
     * load the comments activity
     * @param userPosts
     */
    @Override
    public void startCommentsActivity(UserPosts userPosts, int position) {
        Gson gson = new Gson();
        String myJson = gson.toJson(userPosts);
         Intent intent = new Intent(getActivity(), PostsCommentsActivity.class);
         intent.putExtra("post", myJson);
         intent.putExtra("post_position", position);
         startActivity(intent);
    }

    @Override
    public void editPost(UserPosts userPosts, int position) {
        Gson gson = new Gson();
        String myJson = gson.toJson(userPosts);
        Intent intent = new Intent(getActivity(), EditPostActivity.class);
        intent.putExtra("post", myJson);
        intent.putExtra("post_position", position);
        startActivity(intent);
    }

    @Override
    public void deletePost(UserPosts userPosts, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.delete_post_hint);
        builder.setMessage(getString(R.string.delete_post_long_hint));

        builder.setPositiveButton("Delete",
                (dialog, which) -> {
                    // positive button logic
                    dialog.dismiss();
                    deleteUserPost(userPosts.getId(),position);
                });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        // display dialog
        dialog.show();
    }

    @Override
    public void view_profile(UserData userData) {
        Intent intent;
        Gson gson = new Gson();
        String myJson = gson.toJson(userData);
        intent = new Intent(getActivity(), UsersProfileActivity.class);
        intent.putExtra("userdata", myJson);
        startActivity(intent);
    }

    @Override
    public void view_likes(UserPosts userPosts) {
        Intent intent;
        Gson gson = new Gson();
        String userpost = gson.toJson(userPosts);
        String option = gson.toJson(UsersDataActivity.OPTIONS.LIKES);
        intent = new Intent(getActivity(), UsersDataActivity.class);
        intent.putExtra("userpost", userpost);
        intent.putExtra("option", option);
        startActivity(intent);
    }

    /**
     * method to handle userPosts likes
     * and update userPosts likes count
     */
    @Override
    public void likePost(UserPosts userPosts, String action, int position){
        Utility.likeunlikepost(userPosts, action, position,false);
    }

    @Override
    public void pinPost(UserPosts userPosts, String action){
        Utility.pinunpinpost(userPosts, action);
    }

    /**
     * send a request to delete a comment on our remote server
     * @param id
     */
    private void deleteUserPost(long id,int position){
        try {
            utility.show_loader();
            JSONObject jsonData = new JSONObject();
            jsonData.put("id", id);
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = service.deletepost(requestBody);

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response", String.valueOf(response.body()));
                    utility.hide_loader();
                    if(response.body()==null){
                        Toast.makeText(App.getContext(), getString(R.string.delete_post_error_hint),Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        JSONObject jsonObj = new JSONObject(response.body());
                        String status = jsonObj.getString("status");
                        if (status.equalsIgnoreCase("ok")) {
                            userPostAdapter.deletePost(position);
                        }else{
                            Toast.makeText(App.getContext(), getString(R.string.delete_post_error_hint),Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("Error", e.toString());
                        Toast.makeText(App.getContext(), getString(R.string.delete_post_error_hint),Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    Toast.makeText(App.getContext(), getString(R.string.delete_post_error_hint),Toast.LENGTH_SHORT).show();
                    Log.e("error", String.valueOf(throwable.getMessage()));
                    utility.hide_loader();
                }
            });
        }catch (JSONException e) {
            Toast.makeText(App.getContext(), getString(R.string.delete_post_error_hint),Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }
}
