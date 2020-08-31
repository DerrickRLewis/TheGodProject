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
import java.util.List;

import apps.envision.mychurch.R;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.db.DataViewModel;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.interfaces.MediaClickListener;
import apps.envision.mychurch.interfaces.MediaOptionsListener;
import apps.envision.mychurch.libs.pullrefresh.PullRefreshLayout;
import apps.envision.mychurch.pojo.Bookmarks;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.ui.activities.AddPlaylistActivity;
import apps.envision.mychurch.ui.adapters.MediaFragmentAdapter;
import apps.envision.mychurch.utils.JsonParser;
import apps.envision.mychurch.utils.MediaOptions;
import apps.envision.mychurch.utils.ObjectMapper;
import apps.envision.mychurch.utils.PaginationScrollListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class TrendingMediaFragment extends Fragment implements View.OnClickListener, MediaOptionsListener, MediaClickListener {

    private PullRefreshLayout pullRefreshLayout;
    private MediaFragmentAdapter mediaFragmentAdapter;
    private View view;

    private MediaOptions mediaOptions;

    private List<Bookmarks> bookmarksList = new ArrayList<>();
    private List<Media> mediaList = new ArrayList<>();
    private boolean fragmentVisible = false;
    private DataViewModel dataViewModel;

    //for loading more media items
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = 0;

    /**
     * @return
     */
    public static TrendingMediaFragment newInstance() {
        return new TrendingMediaFragment();
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
            fetch_media();
        });
        pullRefreshLayout.setRefreshing(true);
        fetch_media();
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

        mediaOptions = new MediaOptions(getActivity(),this);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        mediaFragmentAdapter = new MediaFragmentAdapter(this,"trending");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mediaFragmentAdapter);
        recyclerView.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;

                mediaFragmentAdapter.setLoader();
                fetch_media();
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
        dataViewModel.getBookmarks().observe(this, bookmarksList -> {
            // Update the cached copy of the bookmarks in the adapter.
            this.bookmarksList = bookmarksList;
        });
        return view;
    }

    /**
     * display empty message if our data list is empty
     * @param msg
     */
    private void display_message(String msg){
        if(mediaFragmentAdapter !=null)
        mediaFragmentAdapter.setInfo(msg, "");
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
    }

    /**
     * fetch trending media from our remote server
     */
    private void fetch_media(){
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("page",currentPage);
            if(PreferenceSettings.getUserData()!=null) {
                jsonData.put("email", PreferenceSettings.getUserData().getEmail());
            }
            jsonData.put("version", PreferenceSettings.getAppVersion());
            String requestBody = jsonData.toString();
            Log.e("trending requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = service.getTrendingMedia(requestBody);

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.d("trending response", String.valueOf(response.body()));
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
                            List<Media> mediaList = JsonParser.getMedia(jsonObj.getJSONArray("media"));
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

    private void parseJsonResponse(List<Media> itms){
        if(currentPage==0 && itms.size() == 0){
            display_message(getResources().getString(R.string.no_data));
        }else {
            if(currentPage==0) {
                mediaList = new ArrayList<>();
                mediaFragmentAdapter.setAdapter(itms);
            }
            else mediaFragmentAdapter.setMoreData(itms);
            mediaList.addAll(itms);
        }
    }

    private void remove_loader(){
        if(currentPage==0){
            pullRefreshLayout.setRefreshing(false);
        }else{
            isLoading = false;
            mediaFragmentAdapter.setLoaded();
        }
    }

    @Override
    public boolean isBookmarked(Media media) {
        for (Bookmarks bookmarks: bookmarksList) {
            if(bookmarks.getId() == media.getId())
                return true;
        }
        return false;
    }

    @Override
    public void bookmark(Media media) {
        if(!isBookmarked(media)) {
            dataViewModel.bookmarkMedia(ObjectMapper.mapBoomarkFromMedia(media));
            Toast.makeText(getActivity(), R.string.media_bookmarked,Toast.LENGTH_SHORT).show();
        } else {
            dataViewModel.removeMediaFromBookmarks(media.getId());
            Toast.makeText(getActivity(), R.string.media_unbookmarked,Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void addToPlaylist(Media media) {
        Gson gson = new Gson();
        String myJson = gson.toJson(media);
        Intent intent = new Intent(getActivity(), AddPlaylistActivity.class);
        intent.putExtra("media", myJson);
        startActivity(intent);
    }

    @Override
    public boolean isPlaylistActivity() {
        return false;
    }

    @Override
    public void OnItemClick(Media media, String type) {
        //clear previous playing songs
        mediaOptions.play_media(dataViewModel,mediaList,media);
    }

    @Override
    public boolean isDownloads() {
        return false;
    }

    @Override
    public void OnOptionClick(Media media, View view) {
        mediaOptions.display(view,media);
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
}

