package apps.envision.mychurch.socials.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import apps.envision.mychurch.R;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.interfaces.UserProfileListener;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.libs.pullrefresh.PullRefreshLayout;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.socials.UsersProfileActivity;
import apps.envision.mychurch.ui.adapters.FollowUsersAdapter;
import apps.envision.mychurch.utils.JsonParser;
import apps.envision.mychurch.utils.PaginationScrollListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserFollowersFollowingFragment extends Fragment implements LocalMessageCallback, UserProfileListener {

    private PullRefreshLayout pullRefreshLayout;
    private FollowUsersAdapter followUsersAdapter;
    private List<UserData> userDataList = new ArrayList<>();
    private UserData userData;
    //for loading more items
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = 0;
    private String search_query = "";
    private View view;
    private String email = "";
    private String options = "followers";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public UserFollowersFollowingFragment() {
        // Required empty public constructor
    }


    public static UserFollowersFollowingFragment newInstance(String email, String options) {
        UserFollowersFollowingFragment fragment = new UserFollowersFollowingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, email);
        args.putString(ARG_PARAM2, options);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            email = getArguments().getString(ARG_PARAM1);
            options = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_people, container, false);

        userData = PreferenceSettings.getUserData();
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        followUsersAdapter = new FollowUsersAdapter(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(followUsersAdapter);
        recyclerView.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                if(userDataList.size()>=20) {
                    isLoading = true;
                    currentPage += 1;
                    followUsersAdapter.setLoader();
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
        if(followUsersAdapter !=null)
            followUsersAdapter.setInfo(msg);
    }

    private void fetch_users(){
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("user",email);
            jsonData.put("email",userData.getEmail());
            jsonData.put("option",options);
            jsonData.put("page",currentPage);

            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = service.users_follow_people(requestBody);

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
                            List<UserData> usersList = JsonParser.getUsers(jsonObj.getJSONArray("users"));
                            parseJsonResponse(usersList);
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

    private void parseJsonResponse(List<UserData> itms){
        if(currentPage==0 && itms.size() == 0){
            display_message(getResources().getString(R.string.no_data));
        }else {
            if(currentPage==0) {
                userDataList = new ArrayList<>();
                followUsersAdapter.setAdapter(itms);
            }
            else followUsersAdapter.setMoreData(itms);
            userDataList.addAll(itms);
        }
    }

    private void remove_loader(){
        if(currentPage==0){
            pullRefreshLayout.setRefreshing(false);
        }else{
            isLoading = false;
            followUsersAdapter.setLoaded();
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
        if(localMessage.getId() == R.id.search_people){
            search_query = (String) localMessage.getObject();
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
    public void follow_unfollow(UserData user, int position) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("user",user.getEmail());
            jsonData.put("follower",userData.getEmail());
            if(user.isFollowing()){
                jsonData.put("action","unfollow");
            }else{
                jsonData.put("action","follow");
            }
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);
            Call<String> callAsync = service.follow_unfollow_user(requestBody);
            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response", String.valueOf(response.body()));
                    if (response.body() == null) {
                        followUsersAdapter.updateUser(user,position);
                        return;
                    }
                    try {
                        JSONObject jsonObj = new JSONObject(response.body());
                        String status = jsonObj.getString("status");
                        String action = jsonObj.getString("action");
                        if (status.equalsIgnoreCase("ok")) {
                            if(action.equalsIgnoreCase("follow")){
                                user.setFollowing(true);
                            }else{
                                user.setFollowing(false);
                            }
                            followUsersAdapter.updateUser(user,position);
                        }else{
                            followUsersAdapter.updateUser(user,position);
                        }
                        if(email.equalsIgnoreCase(userData.getEmail())){
                            LocalMessageManager.getInstance().send(R.id.update_userfollow_data);
                        }
                    } catch (JSONException e) {
                        Log.e("Error", e.toString());
                        followUsersAdapter.updateUser(user,position);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    //System.out.println(throwable);
                    Log.e("error", String.valueOf(throwable.getMessage()));
                    followUsersAdapter.updateUser(user,position);
                }
            });
        }catch (JSONException e) {
            Log.e("parse error",e.getMessage());
            followUsersAdapter.updateUser(user,position);
            e.printStackTrace();
        }
    }
}
