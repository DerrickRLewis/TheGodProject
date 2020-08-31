package apps.envision.mychurch.socials.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
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
import apps.envision.mychurch.interfaces.UserNotificationListener;
import apps.envision.mychurch.interfaces.UserProfileListener;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.libs.pullrefresh.PullRefreshLayout;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.pojo.UserNotifications;
import apps.envision.mychurch.pojo.UserPosts;
import apps.envision.mychurch.socials.UsersDataActivity;
import apps.envision.mychurch.socials.UsersProfileActivity;
import apps.envision.mychurch.socials.adapters.UserNotificationsAdapter;
import apps.envision.mychurch.ui.activities.PostsCommentsActivity;
import apps.envision.mychurch.ui.adapters.FollowUsersAdapter;
import apps.envision.mychurch.utils.JsonParser;
import apps.envision.mychurch.utils.PaginationScrollListener;
import apps.envision.mychurch.utils.SwipeToDeleteCallback;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserNotificationsFragment extends Fragment implements LocalMessageCallback, UserNotificationListener {

    private PullRefreshLayout pullRefreshLayout;
    private UserNotificationsAdapter userNotificationsAdapter;
    private RecyclerView recyclerView;
    private List<UserNotifications> userDataList = new ArrayList<>();
    private UserData userData;
    //for loading more items
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = 0;
    private String search_query = "";
    private View view;
    private static final String ARG_PARAM1 = "param1";

    public UserNotificationsFragment() {
        // Required empty public constructor
    }


    public static UserNotificationsFragment newInstance() {
        return new UserNotificationsFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_people, container, false);

        userData = PreferenceSettings.getUserData();
        recyclerView = view.findViewById(R.id.recyclerview);
        userNotificationsAdapter = new UserNotificationsAdapter(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(userNotificationsAdapter);
        recyclerView.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                if(userDataList.size()>=20) {
                    isLoading = true;
                    currentPage += 1;
                    userNotificationsAdapter.setLoader();
                    fetch_users();
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
            fetch_users();
        });
        pullRefreshLayout.setRefreshing(true);
        fetch_users();
    }

    /**
     * display empty message if our data list is empty
     * @param msg
     */
    private void display_message(String msg){
        if(userNotificationsAdapter !=null)
            userNotificationsAdapter.setInfo(msg);
    }

    private void fetch_users(){
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("email",userData.getEmail());
            jsonData.put("page",currentPage);

            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = service.userNotifications(requestBody);

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response", String.valueOf(response.body()));
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
                            List<UserNotifications> userNotificationsList = JsonParser.getUserNotifications(jsonObj);
                            parseJsonResponse(userNotificationsList);
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

    private void parseJsonResponse(List<UserNotifications> itms){
        if(currentPage==0 && itms.size() == 0){
            display_message(getResources().getString(R.string.no_data));
        }else {
            if(currentPage==0) {
                userDataList = new ArrayList<>();
                userNotificationsAdapter.setAdapter(itms);
            }
            else userNotificationsAdapter.setMoreData(itms);
            userDataList.addAll(itms);
            enableSwipeToDeleteAndUndo();
        }
    }

    private void remove_loader(){
        if(currentPage==0){
            pullRefreshLayout.setRefreshing(false);
        }else{
            isLoading = false;
            userNotificationsAdapter.setLoaded();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalMessageManager.getInstance().addListener(this);
    }

    @Override
    public void onDestroy() {
        LocalMessageManager.getInstance().removeListener(this);
        super.onDestroy();
    }


    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
        if(localMessage.getId() == R.id.reload_notifications){
            currentPage = 0;
            pullRefreshLayout.setRefreshing(true);
            fetch_users();
        }

        if(localMessage.getId() == R.id.search_people_cancel){
            search_query = "";
            currentPage = 0;
            pullRefreshLayout.setRefreshing(true);
            fetch_users();
        }
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
    public void viewItem(UserNotifications userNotifications) {
        switch (userNotifications.getType()){
            case "follow":
                Intent intent2;
                Gson gson2 = new Gson();
                String userdata2 = gson2.toJson(userData);
                String option2 = gson2.toJson(UsersDataActivity.OPTIONS.FOLLOWERS);
                intent2 = new Intent(getActivity(), UsersDataActivity.class);
                intent2.putExtra("userdata", userdata2);
                intent2.putExtra("option", option2);
                startActivity(intent2);
                break;
            case "comment":  case "like":
                if(!userNotifications.getPost_data().equalsIgnoreCase("")){
                    UserPosts userPosts = new Gson().fromJson(userNotifications.getPost_data(), UserPosts.class);
                    if(userPosts!=null){
                        Gson gson = new Gson();
                        String myJson = gson.toJson(userPosts);
                        Intent intent = new Intent(getActivity(), PostsCommentsActivity.class);
                        intent.putExtra("post", myJson);
                        intent.putExtra("post_position", 0);
                        startActivity(intent);
                    }
                }
                break;
           /* case "like":
                if(!userNotifications.getPost_data().equalsIgnoreCase("")){
                    UserPosts userPosts = new Gson().fromJson(userNotifications.getPost_data(), UserPosts.class);
                    if(userPosts!=null){
                        Intent intent;
                        Gson gson = new Gson();
                        String userpost = gson.toJson(userPosts);
                        String option = gson.toJson(UsersDataActivity.OPTIONS.LIKES);
                        intent = new Intent(getActivity(), UsersDataActivity.class);
                        intent.putExtra("userpost", userpost);
                        intent.putExtra("option", option);
                        startActivity(intent);
                    }
                }
                break;*/
        }
    }

    @Override
    public void deleteItem(UserNotifications userNotifications) {

    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(getActivity()) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int position = viewHolder.getAdapterPosition();
                final UserNotifications item = (UserNotifications)userNotificationsAdapter.getData().get(position);
                userNotificationsAdapter.removeItem(position);
                deleteNotification(item.getId());
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }

    private void deleteNotification(long id){
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("id", id);
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = service.deleteNotification(requestBody);

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response", String.valueOf(response.body()));
                    if(response.body()==null){
                        return;
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
}
