package apps.envision.mychurch.ui.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.DataViewModel;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.interfaces.HymnsClickListener;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.Hymns;
import apps.envision.mychurch.pojo.SBible;
import apps.envision.mychurch.ui.adapters.BookmarkedHymnsAdapter;
import apps.envision.mychurch.ui.adapters.HighlightedVersesAdapter;
import apps.envision.mychurch.utils.Utility;

public class BookmarkedHymnsActivity extends AppCompatActivity implements HymnsClickListener {

    private BookmarkedHymnsAdapter bookmarkedHymnsAdapter;
    private List<Hymns> hymnsList = new ArrayList<>();
    private DataViewModel dataViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bible_highlights);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.bookmarked_hymns);


        RecyclerView recyclerview = findViewById(R.id.recycler_view);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        recyclerview.setHasFixedSize(true);
        //recyclerView.setFocusable(false);
        recyclerview.setItemAnimator(new DefaultItemAnimator());


        // Get a new or existing ViewModel from the ViewModelProvider.
        dataViewModel = ViewModelProviders.of(this).get(DataViewModel.class);
        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        dataViewModel.fetchHymns().observe(this, bookmarksList -> {
            // Update the cached copy of the bookmarks in the adapter.
            this.hymnsList = bookmarksList;

            bookmarkedHymnsAdapter = new BookmarkedHymnsAdapter(this, hymnsList,this);
            recyclerview.setAdapter(bookmarkedHymnsAdapter);
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.highlights, menu);
        MenuItem mSearch = menu.findItem(R.id.search);
        SearchView mSearchView = (SearchView) mSearch.getActionView();
        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mSearchView.setOnCloseListener(() -> {

            //show_next_previous_nav();
            return false;
        });
        mSearchView.setQueryHint(getString(R.string.search_highlighted_verses));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                bookmarkedHymnsAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                bookmarkedHymnsAdapter.getFilter().filter(query);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void OnItemClick(Hymns hymns) {
        Gson gson = new Gson();
        String myJson = gson.toJson(hymns);
        Intent intent = new Intent(BookmarkedHymnsActivity.this, HymnsViewerActivity.class);
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
        Intent intent = new Intent(BookmarkedHymnsActivity.this, HymnsViewerActivity.class);
        intent.putExtra("hymns", myJson);
        startActivity(intent);
    }

    @Override
    public boolean isBookmarked(Hymns hymns) {
        return true;
    }

    @Override
    public void bookmark(Hymns hymns) {
        dataViewModel.deleteBookmarkedHymn(hymns.getId());
        Toast.makeText(BookmarkedHymnsActivity.this, R.string.removed_from_bookmarks,Toast.LENGTH_SHORT).show();
        LocalMessageManager.getInstance().send(R.id.reload_hymns);
    }


}
