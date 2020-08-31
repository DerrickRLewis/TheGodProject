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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.Bible;
import apps.envision.mychurch.pojo.SBible;
import apps.envision.mychurch.ui.adapters.HighlightedVersesAdapter;
import apps.envision.mychurch.utils.Utility;

public class BibleHighlightActivity extends AppCompatActivity implements HighlightedVersesAdapter.HighlightsListener {

    private HighlightedVersesAdapter highlightedVersesAdapter;
    private List<SBible> sBibleList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(PreferenceSettings.getBibleThemeMode()==0) {
            setTheme(R.style.AppThemeDark);
        }
        setContentView(R.layout.activity_bible_highlights);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.highlighted_verses));

        sBibleList = PreferenceSettings.getHighlightedBibleVerses();
        RecyclerView recyclerview = findViewById(R.id.recycler_view);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        recyclerview.setHasFixedSize(true);
        //recyclerView.setFocusable(false);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        highlightedVersesAdapter = new HighlightedVersesAdapter(this, sBibleList,this);
        recyclerview.setAdapter(highlightedVersesAdapter);
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
                highlightedVersesAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                highlightedVersesAdapter.getFilter().filter(query);
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
    public void share_highlight(SBible sBible) {
        List<SBible> selectedBibleList = new ArrayList<>();
        selectedBibleList.add(sBible);
        String text_to_share = Utility.get_bibleverses_as_string(Utility.arrange_selected_bible_verses(selectedBibleList));
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text_to_share);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    @Override
    public void copy_highlight(SBible sBible) {
        List<SBible> selectedBibleList = new ArrayList<>();
        selectedBibleList.add(sBible);
        String text_to_copy = Utility.get_bibleverses_as_string(Utility.arrange_selected_bible_verses(selectedBibleList));
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text_to_copy);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(App.getContext(),"Copied to clipboard",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void delete_highlight(SBible sBible, int position) {
        AlertDialog.Builder builder;
        if(PreferenceSettings.getBibleThemeMode()==1){
            builder = new AlertDialog.Builder(this,R.style.AlertDialogCustomLight);
        }else {
            builder = new AlertDialog.Builder(this,R.style.AlertDialogCustomDark);
        }
        builder.setTitle(getString(R.string.remove_highlight));
        builder.setMessage(getString(R.string.remove_highlight_hint));
        builder.setPositiveButton("Yes, Delete",
                (dialog, which) -> {
                    // positive button logic
                    dialog.dismiss();
                    remove_highlight_verse(sBible,position);

                });
        builder.setNegativeButton("Cancel",
                (dialog, which) -> {
                    // positive button logic
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        // display dialog
        dialog.show();
    }

    public void remove_highlight_verse(SBible bible, int position) {
        List<SBible> bibleList = new ArrayList<>();
        if(PreferenceSettings.getHighlightedBibleVerses()!=null){
            bibleList = PreferenceSettings.getHighlightedBibleVerses();
        }
        for (Iterator<SBible> iterator = bibleList.iterator(); iterator.hasNext(); ) {
            SBible value = iterator.next();
            if(value.getId() == bible.getId()){
                iterator.remove();
            }
        }
        sBibleList = new ArrayList<>();
        sBibleList = bibleList;
        PreferenceSettings.setHighlightedBibleVerses(bibleList);
        highlightedVersesAdapter.setAdapterItems(sBibleList);
       // highlightedVersesAdapter.notifyDataSetChanged();
        LocalMessageManager.getInstance().send(R.id.reload_bible_view);
    }
}
