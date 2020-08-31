package apps.envision.mychurch.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import apps.envision.mychurch.R;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.interfaces.BranchClickListener;
import apps.envision.mychurch.interfaces.UserNotificationListener;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.libs.pullrefresh.PullRefreshLayout;
import apps.envision.mychurch.pojo.Branches;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.pojo.UserNotifications;
import apps.envision.mychurch.pojo.UserPosts;
import apps.envision.mychurch.socials.UsersDataActivity;
import apps.envision.mychurch.socials.UsersProfileActivity;
import apps.envision.mychurch.socials.adapters.UserNotificationsAdapter;
import apps.envision.mychurch.ui.activities.PostsCommentsActivity;
import apps.envision.mychurch.ui.adapters.BranchesAdapter;
import apps.envision.mychurch.utils.JsonParser;
import apps.envision.mychurch.utils.PaginationScrollListener;
import apps.envision.mychurch.utils.SwipeToDeleteCallback;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BranchesFragment extends Fragment implements LocalMessageCallback, BranchClickListener {

    private PullRefreshLayout pullRefreshLayout;
    private BranchesAdapter branchesAdapter;
    private RecyclerView recyclerView;
    private List<Branches> branchesList = new ArrayList<>();
    //for loading more items
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = 0;
    private View view;
    private boolean isVisible;

    public BranchesFragment() {
        // Required empty public constructor
    }


    public static BranchesFragment newInstance() {
        return new BranchesFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_people, container, false);

        recyclerView = view.findViewById(R.id.recyclerview);
        branchesAdapter = new BranchesAdapter(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(branchesAdapter);
        recyclerView.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                if(branchesList.size()>=20) {
                    isLoading = true;
                    currentPage += 1;
                    branchesAdapter.setLoader();
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
        if(branchesAdapter !=null)
            branchesAdapter.setInfo(msg);
    }

    private void fetch_users(){
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("page",currentPage);

            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = service.church_branches(requestBody);

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response", String.valueOf(response.body()));
                    if(!isVisible)return;
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
                            List<Branches> branchesList = JsonParser.getBranches(jsonObj);
                            parseJsonResponse(branchesList);
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
                    if(!isVisible)return;
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

    private void parseJsonResponse(List<Branches> itms){
        if(currentPage==0 && itms.size() == 0){
            display_message(getResources().getString(R.string.no_data));
        }else {
            if(currentPage==0) {
                branchesList = new ArrayList<>();
                branchesAdapter.setAdapter(itms);
            }
            else branchesAdapter.setMoreData(itms);
            branchesList.addAll(itms);
        }
    }

    private void remove_loader(){
        if(currentPage==0){
            pullRefreshLayout.setRefreshing(false);
        }else{
            isLoading = false;
            branchesAdapter.setLoaded();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        isVisible = true;
        LocalMessageManager.getInstance().addListener(this);
    }

    @Override
    public void onDestroy() {
        isVisible = false;
        LocalMessageManager.getInstance().removeListener(this);
        super.onDestroy();
    }


    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {

    }


    @Override
    public void OnItemClick(Branches branches) {

    }
}
