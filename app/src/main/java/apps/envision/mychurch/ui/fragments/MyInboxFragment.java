package apps.envision.mychurch.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import apps.envision.mychurch.R;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.db.DataViewModel;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.interfaces.InboxListener;
import apps.envision.mychurch.interfaces.MediaClickListener;
import apps.envision.mychurch.interfaces.MediaOptionsListener;
import apps.envision.mychurch.libs.pullrefresh.PullRefreshLayout;
import apps.envision.mychurch.pojo.Bookmarks;
import apps.envision.mychurch.pojo.Inbox;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.ui.activities.AddPlaylistActivity;
import apps.envision.mychurch.ui.activities.InboxViewerActivity;
import apps.envision.mychurch.ui.adapters.InboxAdapter;
import apps.envision.mychurch.ui.adapters.MediaFragmentAdapter;
import apps.envision.mychurch.utils.JsonParser;
import apps.envision.mychurch.utils.MediaOptions;
import apps.envision.mychurch.utils.NetworkUtil;
import apps.envision.mychurch.utils.ObjectMapper;
import apps.envision.mychurch.utils.PaginationScrollListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyInboxFragment extends Fragment implements View.OnClickListener, InboxListener {

    private PullRefreshLayout pullRefreshLayout;
    private InboxAdapter inboxAdapter;
    private View view;

    private List<Inbox> inboxList = new ArrayList<>();
    private boolean fragmentVisible = false;
    private DataViewModel dataViewModel;

    //for loading more inbox items
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = 0;

    /**
     * @return
     */
    public static MyInboxFragment newInstance() {
        return new MyInboxFragment();
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
            fetch_inbox();
        });

        if(NetworkUtil.hasConnection(getActivity())){
            pullRefreshLayout.setRefreshing(true);
            fetch_inbox();
        }
    }

    /**
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.media_fragment, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);

        inboxAdapter = new InboxAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(inboxAdapter);
        recyclerView.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                if(inboxList.size()>19 && NetworkUtil.hasConnection(getActivity())){
                    isLoading = true;
                    currentPage += 1;
                    inboxAdapter.setLoader();
                    fetch_inbox();
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

        //check for new media
        init_pullrefresh();

        // Get a new or existing ViewModel from the ViewModelProvider.
        dataViewModel = ViewModelProviders.of(this).get(DataViewModel.class);
        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        dataViewModel.getAllInbox().observe(this, inboxList -> {
            // Update the cached copy of the bookmarks in the adapter.
            this.inboxList = inboxList;
            parseJsonResponse(this.inboxList);
        });
        return view;
    }

    /**
     * display empty message if our data list is empty
     * @param msg
     */
    private void display_message(String msg){
        if(inboxAdapter !=null)
            inboxAdapter.setInfo(msg);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
    }

    private void fetch_inbox(){
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("page",currentPage);
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = service.fetch_inbox(requestBody);

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
                            List<Inbox> inboxList = JsonParser.getInbox(jsonObj.getJSONArray("inbox"));
                            Collections.reverse(inboxList);
                            dataViewModel.deleteAllInbox();
                            dataViewModel.insertAllInbox(inboxList);
                            parseJsonResponse(inboxList);
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

    private void parseJsonResponse(List<Inbox> itms){
        if(currentPage==0 && itms.size() == 0){
            display_message(getResources().getString(R.string.no_data));
        }else {
            if(currentPage==0) {
                inboxList = new ArrayList<>();
                inboxAdapter.setAdapter(itms);
            }
            else inboxAdapter.setMoreData(itms);
            inboxList.addAll(itms);
        }
    }

    private void remove_loader(){
        if(currentPage==0){
            pullRefreshLayout.setRefreshing(false);
        }else{
            isLoading = false;
            inboxAdapter.setLoaded();
        }
    }


    @Override
    public void onStart() {
        fragmentVisible = true;
        super.onStart();
    }

    @Override
    public void onDestroy() {
        fragmentVisible = false;
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        fragmentVisible = false;
        super.onDetach();
    }

    @Override
    public void OnItemClick(Inbox inbox) {
        Gson gson = new Gson();
        String myJson = gson.toJson(inbox);
        Intent intent = new Intent(getActivity(), InboxViewerActivity.class);
        intent.putExtra("inbox", myJson);
        startActivity(intent);
    }
}

