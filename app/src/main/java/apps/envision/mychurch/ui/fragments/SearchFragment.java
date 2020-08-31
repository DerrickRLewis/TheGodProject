package apps.envision.mychurch.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.R;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.db.DataViewModel;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.interfaces.MediaClickListener;
import apps.envision.mychurch.interfaces.MediaOptionsListener;
import apps.envision.mychurch.pojo.Bookmarks;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.ui.activities.AddPlaylistActivity;
import apps.envision.mychurch.ui.activities.SearchActivity;
import apps.envision.mychurch.ui.adapters.SearchAdapter;
import apps.envision.mychurch.utils.JsonParser;
import apps.envision.mychurch.utils.MediaOptions;
import apps.envision.mychurch.utils.ObjectMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment implements View.OnClickListener,SearchActivity.OnSearchListener, MediaOptionsListener, MediaClickListener {

    private RecyclerView recyclerView;
    private LinearLayout info_layout;
    private TextView message;
    private SearchAdapter searchAdapter;
    private int page = 0; //0 for initial search, 1 after load more

    private DataViewModel dataViewModel;
    private boolean init = false;

    private MediaOptions mediaOptions;

    private List<Bookmarks> bookmarksList = new ArrayList<>();
    private List<Media> mediaList = new ArrayList<>();

    /**
     * @return
     */
    public static SearchFragment newInstance() {
        return new SearchFragment();
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
        View view = inflater.inflate(R.layout.search_fragment, container, false);
        mediaOptions = new MediaOptions(getActivity(),this);
        info_layout = view.findViewById(R.id.info_layout);
        info_layout.setOnClickListener(this);
        message = view.findViewById(R.id.message);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));

        recyclerView.setHasFixedSize(true);
        //recyclerView.setFocusable(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        searchAdapter = new SearchAdapter(recyclerView,this);
        searchAdapter.setOnLoadMoreListener(() -> {
            if(mediaList.size()>=20) { //we only load more if the previous search had upto 20 results
                searchAdapter.setLoader();
                page = 1;
                search_web();
            }
        });
        recyclerView.setAdapter(searchAdapter);

        // Get a new or existing ViewModel from the ViewModelProvider.
        dataViewModel = ViewModelProviders.of(this).get(DataViewModel.class);
        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.


        dataViewModel.getSearch().observe(this, searchList -> {
            // Update the cached copy of the interests in the adapter.
            //we only set the items from db, the first time fragment is launched
            if(searchList!=null) {
                mediaList = ObjectMapper.mapSearchMedia(searchList);
                Log.e("response size", String.valueOf(mediaList.size()));
                PreferenceSettings.set_search_offset(mediaList.size());
            }
            if(!init) {
                parseJsonResponse(mediaList);
                init = true;
            }
        });
        dataViewModel.getBookmarks().observe(this, bookmarksList -> {
            // Update the cached copy of the interests in the adapter.
            //we only set the items from db, the first time fragment is launched
            this.bookmarksList = bookmarksList;
        });
        return view;
    }

    /**
     * display empty message if our data list is empty
     * @param msg
     */
    private void display_message(String msg){
        if(mediaList !=null && mediaList.size()>0) {
            recyclerView.setVisibility(View.GONE);
            info_layout.setVisibility(View.VISIBLE);
            message.setText(msg);
        }
    }

    /**
     * hide display message
     */
    private void hide_message(){
        recyclerView.setVisibility(View.VISIBLE);
        info_layout.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
    }


    @Override
    public void perform_search() {
        page = 0;
        search_web();
    }

    private void search_web(){
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("query", PreferenceSettings.get_search_query());
            if(page==0){
                jsonData.put("offset", "0");
            }else{
                jsonData.put("offset", String.valueOf(PreferenceSettings.get_search_offset()));
            }
            jsonData.put("version", PreferenceSettings.getAppVersion());
            if(PreferenceSettings.getUserData()!=null) {
                jsonData.put("email", PreferenceSettings.getUserData().getEmail());
            }
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = service.search(requestBody);

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response", String.valueOf(response.body()));
                    LocalMessageManager.getInstance().send(R.id.remove_loader);
                    if (response.body() == null) {
                        if(mediaList.size() == 0 && page == 0) {
                            display_message(getString(R.string.data_error));
                        }
                        return;
                    }
                    try {
                        JSONObject jsonObj = new JSONObject(response.body());
                        String status = jsonObj.getString("status");
                        if (status.equalsIgnoreCase("ok")) {
                            List<Media> mediaList = JsonParser.getMedia(jsonObj.getJSONArray("search"));
                            parseJsonResponse(mediaList);
                            if(mediaList.size()>0 && page == 0){
                                //if we have results from our search, clear the previous saved search data
                                dataViewModel.deleteAllSearch();
                            }
                            dataViewModel.insertAllSearch(ObjectMapper.mapSearchList(mediaList));
                        }
                    } catch (JSONException e) {
                        Log.e("Error", e.toString());
                        if(mediaList.size() == 0 && page == 0) {
                            display_message(getString(R.string.data_error));
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    //System.out.println(throwable);
                    LocalMessageManager.getInstance().send(R.id.remove_loader);
                    if(mediaList.size() == 0 && page == 0) {
                        display_message(getString(R.string.data_error));
                    }
                    Log.e("error", String.valueOf(throwable.getMessage()));
                    //setNetworkError();
                }
            });
        }catch (JSONException e) {
            Log.e("parse error",e.getMessage());
            e.printStackTrace();
        }
    }

    private void parseJsonResponse(List<Media> itms){
        if(page==1)remove_loader();
        if(itms.size() == 0 && page == 0){
            display_message(getResources().getString(R.string.no_data));
        } else if (itms.size() > 0) {
            hide_message();
            if (page == 0) searchAdapter.setAdapter(itms);
            else {
                searchAdapter.setMoreAdapter(itms);
            }
        }
    }

    private void remove_loader(){
        searchAdapter.removeLoader();
        searchAdapter.notifyDataSetChanged();
        searchAdapter.setLoaded();
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
}

