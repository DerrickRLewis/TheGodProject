package apps.envision.mychurch.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import apps.envision.mychurch.R;
import apps.envision.mychurch.libs.RotateLoading;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.ui.adapters.AutoCompleteAdapter;
import apps.envision.mychurch.ui.fragments.SearchFragment;


public class SearchActivity extends AppCompatActivity implements View.OnClickListener, LocalMessageCallback {
    static final String TAG = "gospeltunes";
    private Toolbar toolbar;
    Fragment frag;
    OnSearchListener searchListener;

    ImageView send;
    RotateLoading mProgress;
    AutoCompleteTextView mSearchField;
    AutoCompleteAdapter adapter;
    String search_text = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);//fade_out keyboard i
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setup_searchbar();
        load_fragment(); //load search fragment

        findViewById(R.id.finish).setOnClickListener(this);
    }

    /**
     * method to load fragment
     */
    private void load_fragment(){
        SearchFragment frag = SearchFragment.newInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, frag);
        transaction.commit();
    }

    private void setup_searchbar(){
        send = findViewById(R.id.send);
        send.setOnClickListener(this);
        mSearchField =  findViewById (R.id.search_field);
        mSearchField.setText(search_text);
        /*int layout = android.R.layout.simple_list_item_1;
        adapter = new AutoCompleteAdapter (this, layout);
        mSearchField.setAdapter (adapter);
        mSearchField.setOnItemClickListener((adapterView, view, i, l) -> {
            search_text = adapter.getItem(i);
            begin_search();
        });*/

        mProgress = findViewById(R.id.progress_bar);
        mSearchField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mSearchField.dismissDropDown();
                search_text = v.getText().toString().trim();
                begin_search();
            }
            return false;
        });
        //
        search_text = PreferenceSettings.get_search_query();
        if(!search_text.equalsIgnoreCase(""))mSearchField.setText(search_text,false);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id){
            case R.id.send:
                search_text = mSearchField.getText().toString().trim();
                if(!search_text.equalsIgnoreCase("")){
                    mSearchField.dismissDropDown();
                    begin_search();
                }
                break;
            case R.id.finish:
                finish();
                break;
        }
    }

    private void begin_search(){
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);//fade_out keyboard i
        if(frag!=null) {
            if(frag instanceof SearchFragment) {
                if (search_text != null && !search_text.equalsIgnoreCase("")) {
                    PreferenceSettings.set_search_query(search_text);
                    show_loader();
                    searchListener.perform_search();
                }
            }
        }
    }

    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
        switch (localMessage.getId()){
            case R.id.remove_loader:
                hide_loader();
                break;
        }
    }

    //interface listener to pass search results to fragment to display
    //interface is implemented in searchfragment
    public interface OnSearchListener {
        void perform_search();
    }

    /**
     * if searchfragment is loaded,
     * initialise our searchlistener interface
     * @param fragment
     */
    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        frag = fragment;
        if(fragment instanceof SearchFragment) {
            searchListener = (OnSearchListener) fragment;
        }
    }

    //method to show loader while user is searching soundcloud
    private void show_loader(){
        send.setVisibility(View.GONE);
        mProgress.setVisibility(View.VISIBLE);
        mProgress.start();
    }

    /**
     * method to hide loader when search is done
     */
    private void hide_loader(){
        send.setVisibility(View.VISIBLE);
        mProgress.stop();
        mProgress.setVisibility(View.GONE);
    }


    @Override
    protected void onStart() {
        LocalMessageManager.getInstance().addListener(this);
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalMessageManager.getInstance().removeListener(this);
    }
}
