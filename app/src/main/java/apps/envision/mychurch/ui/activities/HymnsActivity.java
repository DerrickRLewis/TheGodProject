package apps.envision.mychurch.ui.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.db.DataViewModel;
import apps.envision.mychurch.interfaces.HymnsClickListener;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.libs.pullrefresh.PullRefreshLayout;
import apps.envision.mychurch.pojo.Hymns;
import apps.envision.mychurch.ui.adapters.HymnsAdapter;
import apps.envision.mychurch.utils.JsonParser;
import apps.envision.mychurch.utils.PaginationScrollListener;
import apps.envision.mychurch.utils.Utility;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HymnsActivity extends AppCompatActivity implements HymnsClickListener, LocalMessageCallback {

    private PullRefreshLayout pullRefreshLayout;
    private HymnsAdapter hymnsAdapter;
    private List<Hymns> hymnsList = new ArrayList<>();
    private List<Hymns> bookmarksList = new ArrayList<>();
    private boolean fragmentVisible = false;
    //for loading more items
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = 0;
    private String search_query = "";
    private DataViewModel dataViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hymns);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.hymns));

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        hymnsAdapter = new HymnsAdapter(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(hymnsAdapter);
        recyclerView.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                if(hymnsList.size()>=20) {
                    isLoading = true;
                    currentPage += 1;
                    hymnsAdapter.setLoader();
                    fetch_hymns();
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

        // Get a new or existing ViewModel from the ViewModelProvider.
        dataViewModel = ViewModelProviders.of(this).get(DataViewModel.class);
        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        dataViewModel.fetchHymns().observe(this, bookmarksList -> {
            // Update the cached copy of the bookmarks in the adapter.
            this.bookmarksList = bookmarksList;
        });

        //check for new hymns
        init_pullrefresh();

    }

    //init pullrefresh
    private void init_pullrefresh(){
        pullRefreshLayout = findViewById(R.id.pullRefreshLayout);
        int[] colorScheme = getResources().getIntArray(R.array.refresh_color_scheme);
        pullRefreshLayout.setRefreshStyle();
        pullRefreshLayout.setColorSchemeColors(colorScheme);
        // listen refresh event
        pullRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 0;
            fetch_hymns();
        });
        pullRefreshLayout.setRefreshing(true);
        fetch_hymns();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hymns, menu);
        MenuItem mSearch = menu.findItem(R.id.search);
        SearchView mSearchView = (SearchView) mSearch.getActionView();
        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mSearchView.setOnCloseListener(() -> {
            search_query = "";
            currentPage = 0;
            pullRefreshLayout.setRefreshing(true);
            fetch_hymns();
            //show_next_previous_nav();
            return false;
        });
        mSearchView.setQueryHint(getString(R.string.search_highlighted_verses));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                search_query = query;
                currentPage = 0;
                pullRefreshLayout.setRefreshing(true);
                fetch_hymns();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed

                return false;
            }
        });
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            case R.id.bookmarks:
                // app icon in action bar clicked; goto parent activity.
                startActivity(new Intent(HymnsActivity.this, BookmarkedHymnsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * display empty message if our data list is empty
     * @param msg
     */
    private void display_message(String msg){
        if(hymnsAdapter !=null)
            hymnsAdapter.setInfo(msg);
    }

    private void fetch_hymns(){
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("page",currentPage);
            jsonData.put("query",search_query);

            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = service.fetch_hymns(requestBody);

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
                            List<Hymns> mediaList = JsonParser.getHymns(jsonObj.getJSONArray("hymns"));
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

    private void parseJsonResponse(List<Hymns> itms){
        if(currentPage==0 && itms.size() == 0){
            display_message(getResources().getString(R.string.no_data));
        }else {
            if(currentPage==0) {
                hymnsList = new ArrayList<>();
                hymnsAdapter.setAdapter(itms);
            }
            else hymnsAdapter.setMoreData(itms);
            hymnsList.addAll(itms);
        }
    }

    private void remove_loader(){
        if(currentPage==0){
            pullRefreshLayout.setRefreshing(false);
        }else{
            isLoading = false;
            hymnsAdapter.setLoaded();
        }
    }

    @Override
    public void OnItemClick(Hymns hymns) {
        Gson gson = new Gson();
        String myJson = gson.toJson(hymns);
        Intent intent = new Intent(HymnsActivity.this, HymnsViewerActivity.class);
        intent.putExtra("hymns", myJson);
        startActivity(intent);
    }

    @Override
    public void share_hymn(Hymns hymns) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, hymns.getTitle());
        sharingIntent.putExtra(Intent.EXTRA_TEXT, Utility.stripHtml(hymns.getContent()));
        startActivity(Intent.createChooser(sharingIntent, "Share Via"));
    }

    @Override
    public void copy_hymn(Hymns hymns) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", Utility.stripHtml(hymns.getContent()));
        clipboard.setPrimaryClip(clip);
        Toast.makeText(App.getContext(),"Copied to clipboard",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void open_hymn(Hymns hymns) {
        Gson gson = new Gson();
        String myJson = gson.toJson(hymns);
        Intent intent = new Intent(HymnsActivity.this, HymnsViewerActivity.class);
        intent.putExtra("hymns", myJson);
        startActivity(intent);
    }

    @Override
    public boolean isBookmarked(Hymns hymns) {
        for (Hymns bookmarks: bookmarksList) {
            if(bookmarks.getId() == hymns.getId())
                return true;
        }
        return false;
    }

    @Override
    public void bookmark(Hymns hymns) {
        if(!isBookmarked(hymns)) {
            dataViewModel.bookmarkHymns(hymns);
            Toast.makeText(HymnsActivity.this, R.string.added_to_bookmarks,Toast.LENGTH_SHORT).show();
        } else {
            dataViewModel.deleteBookmarkedHymn(hymns.getId());
            Toast.makeText(HymnsActivity.this, R.string.removed_from_bookmarks,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalMessageManager.getInstance().addListener(this);
    }

    @Override
    protected void onDestroy() {
        LocalMessageManager.getInstance().removeListener(this);
        super.onDestroy();
    }


    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
        if(localMessage.getId() == R.id.reload_hymns){
            hymnsAdapter.notifyDataSetChanged();
        }
    }
}
