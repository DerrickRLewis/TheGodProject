package apps.envision.mychurch.ui.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.AppDb;
import apps.envision.mychurch.db.DataInterfaceDao;
import apps.envision.mychurch.db.DataViewModel;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.interfaces.BibleClickListener;
import apps.envision.mychurch.libs.colorlib.colorpicker.ColorPickerDialog;
import apps.envision.mychurch.libs.colorlib.colorpicker.ColorPickerSwatch;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.Bible;
import apps.envision.mychurch.pojo.BibleDownload;
import apps.envision.mychurch.pojo.Info;
import apps.envision.mychurch.pojo.SBible;
import apps.envision.mychurch.ui.adapters.BibleAdapter;
import apps.envision.mychurch.ui.fragments.ReadBibleFragment;
import apps.envision.mychurch.utils.Constants;
import apps.envision.mychurch.utils.Utility;

public class BibleActivity extends AppCompatActivity implements View.OnClickListener, LocalMessageCallback, BibleClickListener {

    private LinearLayout parent_layout;
    private ColorPickerDialog colorPickerDialog;
    private ViewGroup.MarginLayoutParams container_params;
    private int bible_count =0;
    private DataInterfaceDao dataInterfaceDao;
    private ExecutorService executorService;
    List<String> books = new ArrayList<>();
    List<String> chapters = new ArrayList<>();
    List<String> versions =  new ArrayList<>();
    List<String> fonts =  new ArrayList<>();
    private List<Bible> bibleList = new ArrayList<>();
    private List<Bible> searchBibleList = new ArrayList<>();
    private boolean isSearch = false;
    private MenuItem theme,speak;
    private List read_list = null;
    private int read_position = 0;


    private Chip version_chip, book_chip, chapter_chip, font_chip, search_version_chip;
    private Dialog dialog;
    private AlertDialog alertDialog;
    private ChipGroup chip_group;
    private HorizontalScrollView search_filter_layout,bible_read_filter;

    private AppCompatCheckBox old_testament, new_testament, current_chapter;

    private BibleAdapter bibleAdapter;
    private DataViewModel dataViewModel;
    private boolean isBibleLayoutLoaded = false;

    private String default_selected_version = "";
    private String default_selected_book = "";
    private int default_selected_chapter = 0;

    private FloatingActionButton previous, next;
    private boolean shouldHidePrevious=false, shouldHideNext=false, reloadBible = false;

    private BottomSheetBehavior sheetBehavior;
    TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(PreferenceSettings.getBibleThemeMode()==0) {
            setTheme(R.style.AppThemeDark);
        }
        setContentView(R.layout.activity_bible);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.bible));

        AppDb db = AppDb.getDatabase(App.getContext());
        dataInterfaceDao = db.dataDbInterface();
        executorService = Executors.newSingleThreadExecutor();

        //get bible books and versions
        getAvailableVersions();
        books = Arrays.asList(getResources().getStringArray(R.array.bible_books));
        chapters = Arrays.asList(getResources().getStringArray(R.array.bible_books_chapters));
        fonts = Arrays.asList(getResources().getStringArray(R.array.bible_font_size));

        parent_layout = findViewById(R.id.parent_view);
        CardView layoutBottomSheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        container_params = (ViewGroup.MarginLayoutParams) parent_layout.getLayoutParams();

        previous= findViewById(R.id.previous);
        previous.setOnClickListener(this);
        next= findViewById(R.id.next);
        next.setOnClickListener(this);
        check_and_load_view();


        // Get a new or existing ViewModel from the ViewModelProvider.
        dataViewModel = ViewModelProviders.of(this).get(DataViewModel.class);
        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.

        dataViewModel.getBible().observe(this, bibleList -> {
            //if(PreferenceSettings.isBibleDownloadInProgress() && isBibleLayoutLoaded)return;//TODO
            if(reloadBible) {
                Log.e("bible list",String.valueOf(bibleList));
                this.bibleList = bibleList;
                if (isBibleLayoutLoaded) {
                    bibleAdapter.setAdapter(bibleList);
                    set_next_previous_visibility();
                }
                reloadBible = false;
            }
        });

        dataViewModel.searchBible().observe(this, bibleList -> {
            if(isBibleLayoutLoaded && bibleList!=null) {
                Log.e("bible list",String.valueOf(bibleList));
                searchBibleList = bibleList;
                if(bibleList.size()==0){
                    bibleAdapter.setInfo("No search result");
                }else {
                    bibleAdapter.setAdapter(bibleList);
                }
            }
        });

        setSelectedDefaultValues();
        setSelectedBookChapter();
        initTextToSpeech();
        init_color_dialog();
    }

    private void init_color_dialog(){
        int[] mColors = getResources().getIntArray(R.array.default_rainbow);
        colorPickerDialog = ColorPickerDialog.newInstance(R.string.color_picker_default_title,
                mColors,
                PreferenceSettings.getSelectedHighlightColor(),
                5, // Number of columns
                ColorPickerDialog.SIZE_SMALL,
                false // True or False to enable or disable the serpentine effect
                //0, // stroke width
                //Color.BLACK // stroke color
        );

        colorPickerDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {

            @Override
            public void onColorSelected(int color) {
                PreferenceSettings.setSelectedHighlightColor(color);

                List<SBible> bibleList = new ArrayList<>();
                if(PreferenceSettings.getHighlightedBibleVerses()!=null){
                    bibleList = PreferenceSettings.getHighlightedBibleVerses();
                }

                List<SBible> selectedBibleList = new ArrayList<>();
                if(PreferenceSettings.getSelectedBibleVerses()!=null){
                    selectedBibleList = PreferenceSettings.getSelectedBibleVerses();
                    for (SBible sbible: selectedBibleList) {
                        sbible.setColorCode(color);
                    }
                }
                bibleList.addAll(selectedBibleList);
                PreferenceSettings.setHighlightedBibleVerses(bibleList);
                setSelectedBookChapter();
            }

        });
    }

    private void initTextToSpeech(){
        // Init TextToSpeech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.UK);
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(App.getContext(), "This language is not supported!",
                            Toast.LENGTH_SHORT).show();
                }

                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {

                    }

                    @Override
                    public void onDone(String utteranceId) {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                read_position +=1;
                                if(read_list==null || read_list.size()==read_position) {
                                    speak.setTitle(getString(R.string.read_chapter));
                                }else{
                                    String text_to_read = Utility.get_bibleverses_as_string_for_speech((List<Bible>) read_list.get(read_position));
                                    textToSpeech.speak(text_to_read, TextToSpeech.QUEUE_FLUSH, null,TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
                                    speak.setTitle(getString(R.string.stop_reading));
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String utteranceId) {

                    }
                });
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bible, menu);
        speak = menu.findItem(R.id.read);
        theme = menu.findItem(R.id.theme);
        if(PreferenceSettings.getBibleThemeMode()==0){
            theme.setTitle(getString(R.string.light_mode));
        }else{
            theme.setTitle(getString(R.string.night_mode));
        }
        MenuItem mSearch = menu.findItem(R.id.search);
        SearchView mSearchView = (SearchView) mSearch.getActionView();
        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bible_count==0)return;
                search_filter_layout.setVisibility(View.VISIBLE);
                bible_read_filter.setVisibility(View.GONE);
                if(bibleAdapter!=null){
                    bibleAdapter.setIsSearch(true);
                    bibleAdapter.setInfo("Search text must be\n minimum of 3 words");
                }
                hide_next_previous_nav();
                sheetBehavior.setPeekHeight(0);
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                container_params.bottomMargin = 0;
               if(current_chapter!=null){
                   current_chapter.setText(PreferenceSettings.getDefaultSelectedBook()+" only");
               }
               isSearch = true;
            }
        });
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                isSearch = false;
                search_filter_layout.setVisibility(View.GONE);
                bible_read_filter.setVisibility(View.VISIBLE);
                if(bibleAdapter!=null){
                    bibleAdapter.setIsSearch(false);
                }
                setSelectedBookChapter();
                //show_next_previous_nav();
                return false;
            }
        });
        mSearchView.setQueryHint("Search Bible");
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.length()<3){
                    Toast.makeText(App.getContext(),"Search text must be minimum of 3 words",Toast.LENGTH_SHORT).show();
                    return false;
                }
                if(bibleAdapter!=null){
                    bibleAdapter.setSearchQuery(query);
                }
                String search_query = "%"+query+"%";
                if(current_chapter.isChecked()){
                    String[] books = {PreferenceSettings.getDefaultSelectedBook()};
                    dataViewModel.setBibleSearchFilter(books,search_query,PreferenceSettings.getDefaultSelectedVersion());
                }else if(old_testament.isChecked() && !new_testament.isChecked()){
                    String[] books = getResources().getStringArray(R.array.old_testament);
                    dataViewModel.setBibleSearchFilter(books,search_query,PreferenceSettings.getDefaultSelectedVersion());
                }else if(!old_testament.isChecked() && new_testament.isChecked()){
                    String[] books = getResources().getStringArray(R.array.new_testament);
                    dataViewModel.setBibleSearchFilter(books,search_query,PreferenceSettings.getDefaultSelectedVersion());
                }else{
                    String[] books = getResources().getStringArray(R.array.bible_books);
                    dataViewModel.setBibleSearchFilter(books,search_query,PreferenceSettings.getDefaultSelectedVersion());
                }
                return false;
            }
        });
        return true;
    }

    private void hide_next_previous_nav(){
        previous.setVisibility(View.GONE);
        next.setVisibility(View.GONE);
        shouldHidePrevious = true;
        shouldHideNext = true;
    }

    private void show_next_previous_nav(){
       /* previous.setVisibility(View.VISIBLE);
        next.setVisibility(View.VISIBLE);
        shouldHidePrevious = false;
        shouldHideNext = false;*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            case R.id.bookmark:
                startActivity(new Intent(this, BibleHighlightActivity.class));
                return true;
            case R.id.theme:
                if(PreferenceSettings.getBibleThemeMode()==0){
                    theme.setTitle(getString(R.string.light_mode));
                    PreferenceSettings.setBibleThemeMode(1);
                }else{
                    theme.setTitle(getString(R.string.night_mode));
                    PreferenceSettings.setBibleThemeMode(0);
                }
                recreate();
                return true;
            case R.id.read:
                 read_selected_bible_verses();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void check_and_load_view(){
        //check if the bible versions have been downloaded
        executorService.execute(() -> {
            bible_count +=  dataInterfaceDao.getBibleAMP();
            bible_count +=  dataInterfaceDao.getBibleKJV();
            bible_count +=  dataInterfaceDao.getBibleMSG();
            bible_count +=  dataInterfaceDao.getBibleNIV();
            bible_count +=  dataInterfaceDao.getBibleNKJV();
            bible_count +=  dataInterfaceDao.getBibleNLT();
            bible_count +=  dataInterfaceDao.getBibleNRSV();
            Log.e("bible_count",String.valueOf(bible_count));
            runOnUiThread(() -> {
                if(bible_count<31102){
                    init_empty_layout();
                    if(theme!=null){
                        theme.setVisible(false);
                    }
                    if(speak!=null){
                        speak.setVisible(false);
                    }
                }else{
                    previous.setVisibility(View.VISIBLE);
                    next.setVisibility(View.VISIBLE);
                    init_bible_layout();
                    if(theme!=null){
                        theme.setVisible(true);
                    }
                    if(speak!=null){
                        speak.setVisible(true);
                    }
                }
            });
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.download_bible:
                startActivity(new Intent(this, DownloadBibleActivity.class));
                break;
            case R.id.version: case R.id.search_version:
                loadVersions();
                break;
            case R.id.book:
                loadBooks();
                break;
            case R.id.chapter:
                loadChapters();
                break;
            case R.id.font_chip:
                loadFonts();
                break;
            case R.id.next:
                set_next_chapter();
                break;
            case R.id.previous:
                set_previous_chapter();
                break;
            case R.id.new_testament: case R.id.old_testament:
                current_chapter.setChecked(false);
                break;
                case R.id.current_chapter:
                new_testament.setChecked(false);
                old_testament.setChecked(false);
                break;
        }
    }

    private void init_bible_layout(){
        clearLayout();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.fragment_read_bible, null);
        // Add the new row before the add field button.

        search_filter_layout = rowView.findViewById(R.id.search_filter_layout);
        bible_read_filter = rowView.findViewById(R.id.bible_read_filter);

        version_chip = rowView.findViewById(R.id.version);
        version_chip.setText(default_selected_version);
        search_version_chip = rowView.findViewById(R.id.search_version);
        search_version_chip.setText(default_selected_version);
        book_chip = rowView.findViewById(R.id.book);
        font_chip = rowView.findViewById(R.id.font_chip);
        font_chip.setText("font - "+PreferenceSettings.getDefaultSelectedFont());
        book_chip.setText(default_selected_book);
        chapter_chip = rowView.findViewById(R.id.chapter);
        chapter_chip.setText("Chapter "+default_selected_chapter);

        version_chip.setOnClickListener(this);
        search_version_chip.setOnClickListener(this);
        book_chip.setOnClickListener(this);
        chapter_chip.setOnClickListener(this);
        font_chip.setOnClickListener(this);

        old_testament = rowView.findViewById(R.id.old_testament);
        old_testament.setOnClickListener(this);
        new_testament = rowView.findViewById(R.id.new_testament);
        new_testament.setOnClickListener(this);
        current_chapter = rowView.findViewById(R.id.current_chapter);
        current_chapter.setOnClickListener(this);

        RecyclerView recyclerView = rowView.findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        bibleAdapter = new BibleAdapter(this);
        recyclerView.setAdapter(bibleAdapter);
        bibleAdapter.setAdapter(bibleList);
        isBibleLayoutLoaded = true;
        parent_layout.addView(rowView, 0);
        set_next_previous_visibility();



        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (dy > 0) {
                    next.hide();
                    previous.hide();
                } else if (dy < 0) {
                    if(!shouldHideNext)next.show();
                    if(!shouldHidePrevious)previous.show();
                }
            }
        });
    }

    private void init_empty_layout(){
        clearLayout();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.no_bible_layout, null);
        view.findViewById(R.id.download_bible).setOnClickListener(this);
        // Add the new row before the add field button.
        parent_layout.addView(view, 0);
    }


    public void clearLayout() {
        parent_layout.removeAllViews();
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalMessageManager.getInstance().addListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalMessageManager.getInstance().removeListener(this);
        PreferenceSettings.setSelectedBibleVerses(new ArrayList<>());
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
        if(localMessage.getId() == R.id.bible_download_complete){
            /*if(bible_count==0){
                check_and_load_view();
            }else{
                bibleAdapter.notifyDataSetChanged();
            }*/
            //recreate();
            String book = (String)localMessage.getObject();
            AlertDialog.Builder builder;
            if(PreferenceSettings.getBibleThemeMode()==1){
                builder = new AlertDialog.Builder(this,R.style.AlertDialogCustomLight);
            }else {
                builder = new AlertDialog.Builder(this,R.style.AlertDialogCustomDark);
            }
            builder.setTitle(getString(R.string.reload_bible));
            builder.setMessage(book+" "+getString(R.string.reload_bible_hint));
            builder.setPositiveButton("Yes, Reload",
                    (dialog, which) -> {
                        // positive button logic
                        dialog.dismiss();
                        finish();
                        startActivity(getIntent());

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
        if(localMessage.getId() == R.id.reload_bible_view){
            /*if(bible_count==0){
                check_and_load_view();
            }else{
                bibleAdapter.notifyDataSetChanged();
            }*/
            setSelectedBookChapter();
        }
    }


    private void getAvailableVersions(){
        BibleDownload bibleDownload1 = PreferenceSettings.getCurrentBibleDownload(Constants.BIBLE_VERSIONS.AMP);
        if(bibleDownload1!=null && bibleDownload1.isCompleted()){
            versions.add(Constants.BIBLE_VERSIONS.AMP);
        }

        BibleDownload bibleDownload2 = PreferenceSettings.getCurrentBibleDownload(Constants.BIBLE_VERSIONS.KJV);
        if(bibleDownload2!=null && bibleDownload2.isCompleted()){
            versions.add(Constants.BIBLE_VERSIONS.KJV);
        }

        BibleDownload bibleDownload3 = PreferenceSettings.getCurrentBibleDownload(Constants.BIBLE_VERSIONS.MSG);
        if(bibleDownload3!=null && bibleDownload3.isCompleted()){
            versions.add(Constants.BIBLE_VERSIONS.MSG);
        }

        BibleDownload bibleDownload4 = PreferenceSettings.getCurrentBibleDownload(Constants.BIBLE_VERSIONS.NIV);
        if(bibleDownload4!=null && bibleDownload4.isCompleted()){
            versions.add(Constants.BIBLE_VERSIONS.NIV);
        }

        BibleDownload bibleDownload5 = PreferenceSettings.getCurrentBibleDownload(Constants.BIBLE_VERSIONS.NKJV);
        if(bibleDownload5!=null && bibleDownload5.isCompleted()){
            versions.add(Constants.BIBLE_VERSIONS.NKJV);
        }

        BibleDownload bibleDownload6 = PreferenceSettings.getCurrentBibleDownload(Constants.BIBLE_VERSIONS.NLT);
        if(bibleDownload6 !=null && bibleDownload6.isCompleted()){
            versions.add(Constants.BIBLE_VERSIONS.NLT);
        }

        BibleDownload bibleDownload7 = PreferenceSettings.getCurrentBibleDownload(Constants.BIBLE_VERSIONS.NRSV);
        if(bibleDownload7!=null && bibleDownload7.isCompleted()){
            versions.add(Constants.BIBLE_VERSIONS.NRSV);
        }

        //return spinnerArray;
    }

    /**
     * here we load the current playlist in a dialog
     */
    public void loadBooks() {
        View view = LayoutInflater.from(this).inflate(R.layout.bible_dialog, null);
        chip_group = view.findViewById(R.id.chip_group);
        LinearLayout parent_toolbar_search = view.findViewById(R.id.parent_toolbar_search);
        if(PreferenceSettings.getBibleThemeMode()==0){
            parent_toolbar_search.setBackgroundColor(getResources().getColor(R.color.material_grey_800));
        }else{
            parent_toolbar_search.setBackgroundColor(getResources().getColor(R.color.white));
        }
        AppCompatEditText searchEditText = view.findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()<2){
                    setBooksChipsView(books);
                }else{
                    String query = s.toString();
                    List<String> booksList = new ArrayList<>();
                    for (String book: books) {
                        if(book.toLowerCase().contains(query.toLowerCase())){
                            booksList.add(book);
                        }
                    }
                    Log.e("books",String.valueOf(booksList));
                    setBooksChipsView(booksList);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        setBooksChipsView(books);
        view.findViewById(R.id.finish).setOnClickListener(v1 -> dialog.dismiss());


        dialog = new Dialog(this, R.style.MaterialList);
        dialog.setContentView(view);
        dialog.setCancelable(true);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setGravity(Gravity.TOP);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        dialog.getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        dialog.show();
    }

    /**
     * here we load the current playlist in a dialog
     */
    private void loadVersions() {
        if(alertDialog!=null && alertDialog.isShowing())return;
        AlertDialog.Builder dialogBuilder;
        if(PreferenceSettings.getBibleThemeMode()==1){
            dialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogCustomLight);
        }else {
            dialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogCustomDark);
        }
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.bible_versions_chip_layout, null);
        dialogBuilder.setView(view);

        AppCompatButton download_bible = view.findViewById(R.id.download_bible);
        if(versions.size()<7){
            download_bible.setVisibility(View.VISIBLE);
        }else{
            download_bible.setVisibility(View.GONE);
        }

        download_bible.setOnClickListener(v -> {
            startActivity(new Intent(BibleActivity.this,DownloadBibleActivity.class));
            alertDialog.dismiss();
        });

        ChipGroup chip_group = view.findViewById(R.id.chip_group);
        chip_group.removeAllViews();
        for (String version: versions) {
            Chip chip = new Chip(this);
            chip.setText(version);
            if(version_chip.getText().toString().equalsIgnoreCase(version)){
                chip.setChipIcon(App.getContext().getResources().getDrawable(R.drawable.ic_check_white_24dp));
                chip.setChipIconSize(40);
                chip.setChipStartPadding(15);
                chip.setChipIconTintResource(R.color.white);
            }
            if(PreferenceSettings.getBibleThemeMode()==1){
                chip.setChipBackgroundColorResource(R.color.material_blue_grey_700);
                chip.setChipIconTintResource(R.color.white);
                chip.setTextColor(getResources().getColor(R.color.white));
            }else{
                chip.setChipBackgroundColorResource(R.color.white);
                chip.setChipIconTintResource(R.color.material_blue_grey_700);
                chip.setTextColor(getResources().getColor(R.color.material_blue_grey_700));
            }
            chip.setOnClickListener(v -> {
                version_chip.setText(version);
                search_version_chip.setText(version);
                PreferenceSettings.setDefaultSelectedVersion(version);
                setSelectedDefaultValues();
                bibleAdapter.notifyDataSetChanged();
                alertDialog.dismiss();
            });
            chip_group.addView(chip);
        }

        //view.findViewById(R.id.cancel).setOnClickListener(v1 -> alertDialog.dismiss());
        alertDialog = dialogBuilder.create();
        alertDialog.show();
    }


    private void setBooksChipsView(List<String> booksList){
        chip_group.removeAllViews();
        chip_group.setPadding(0,0,0,0);
        chip_group.setChipSpacingHorizontal(-10);
        chip_group.setChipSpacingVertical(-30);
        for (String book: booksList) {
            Chip chip = new Chip(this);
            chip.setText(book);
            if(book_chip.getText().toString().equalsIgnoreCase(book)){
                chip.setChipIconSize(40);
                chip.setChipStartPadding(15);
                chip.setChipIcon(App.getContext().getResources().getDrawable(R.drawable.ic_check_white_24dp));
            }
            if(PreferenceSettings.getBibleThemeMode()==1){
                chip.setChipBackgroundColorResource(R.color.material_blue_grey_700);
                chip.setChipIconTintResource(R.color.white);
                chip.setTextColor(getResources().getColor(R.color.white));
            }else{
                chip.setChipBackgroundColorResource(R.color.white);
                chip.setChipIconTintResource(R.color.material_blue_grey_700);
                chip.setTextColor(getResources().getColor(R.color.material_blue_grey_700));
            }
            chip.setOnClickListener(v -> {
                book_chip.setText(book);
                chapter_chip.setText("Chapter 1");
                Log.e("book index",String.valueOf(books.indexOf(book)));
                PreferenceSettings.setDefaultSelectedBook(book);
                PreferenceSettings.setDefaultSelectedChapter(1);
                setSelectedDefaultValues();
                setSelectedBookChapter();
                dialog.dismiss();
            });
            chip_group.addView(chip);
        }
    }



    private void loadChapters() {
        if(alertDialog!=null && alertDialog.isShowing())return;
        int chapter_ = Integer.parseInt(chapters.get(books.indexOf(book_chip.getText().toString())));
        Log.e("chapter",String.valueOf(chapter_));
        AlertDialog.Builder dialogBuilder;
        if(PreferenceSettings.getBibleThemeMode()==1){
            dialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogCustomLight);
        }else {
            dialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogCustomDark);
        }
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.bible_chip_layout, null);
        dialogBuilder.setView(view);

        ChipGroup chip_group = view.findViewById(R.id.chip_group);
        chip_group.removeAllViews();
        chip_group.setPadding(0,0,0,0);
        chip_group.setChipSpacingHorizontal(-10);
        chip_group.setChipSpacingVertical(-30);
        for (int i=1; i<=chapter_; i++) {
            Chip chip = new Chip(this);
            String txt = String.valueOf(i);
            chip.setText(txt);
            if(chapter_chip.getText().toString().equalsIgnoreCase("Chapter "+txt)){
                chip.setChipIcon(App.getContext().getResources().getDrawable(R.drawable.ic_check_white_24dp));
                chip.setChipIconSize(40);
                chip.setChipStartPadding(15);
                chip.setChipIconTintResource(R.color.white);
            }
            if(PreferenceSettings.getBibleThemeMode()==1){
                chip.setChipBackgroundColorResource(R.color.material_blue_grey_700);
                chip.setChipIconTintResource(R.color.white);
                chip.setTextColor(getResources().getColor(R.color.white));
            }else{
                chip.setChipBackgroundColorResource(R.color.white);
                chip.setChipIconTintResource(R.color.material_blue_grey_700);
                chip.setTextColor(getResources().getColor(R.color.material_blue_grey_700));
            }
            chip.setOnClickListener(v -> {
                chapter_chip.setText("Chapter "+txt);
                PreferenceSettings.setDefaultSelectedChapter(Integer.parseInt(txt));
                setSelectedDefaultValues();
                setSelectedBookChapter();
                alertDialog.dismiss();
            });
            chip_group.addView(chip);
        }

        //view.findViewById(R.id.cancel).setOnClickListener(v1 -> alertDialog.dismiss());
        alertDialog = dialogBuilder.create();
        alertDialog.show();
    }


    private void loadFonts() {
        if(alertDialog!=null && alertDialog.isShowing())return;
        AlertDialog.Builder dialogBuilder;
        if(PreferenceSettings.getBibleThemeMode()==1){
            dialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogCustomLight);
        }else {
            dialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogCustomDark);
        }
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.bible_chip_layout, null);
        dialogBuilder.setView(view);

        ChipGroup chip_group = view.findViewById(R.id.chip_group);
        chip_group.removeAllViews();
        chip_group.setPadding(0,0,0,0);
        chip_group.setChipSpacingHorizontal(-10);
        chip_group.setChipSpacingVertical(-30);
        for (String font: fonts) {
            Chip chip = new Chip(this);
            chip.setText(font);
            if(font_chip.getText().toString().equalsIgnoreCase("font - "+font)){
                chip.setChipIcon(App.getContext().getResources().getDrawable(R.drawable.ic_check_white_24dp));
                chip.setChipIconSize(40);
                chip.setChipStartPadding(15);
                chip.setChipIconTintResource(R.color.white);
            }
            if(PreferenceSettings.getBibleThemeMode()==1){
                chip.setChipBackgroundColorResource(R.color.material_blue_grey_700);
                chip.setChipIconTintResource(R.color.white);
                chip.setTextColor(getResources().getColor(R.color.white));
            }else{
                chip.setChipBackgroundColorResource(R.color.white);
                chip.setChipIconTintResource(R.color.material_blue_grey_700);
                chip.setTextColor(getResources().getColor(R.color.material_blue_grey_700));
            }
            chip.setOnClickListener(v -> {
                font_chip.setText("font - "+font);
                PreferenceSettings.setDefaultSelectedFont(Integer.parseInt(font));
                setSelectedDefaultValues();
                bibleAdapter.notifyDataSetChanged();
                alertDialog.dismiss();
            });
            chip_group.addView(chip);
        }

        //view.findViewById(R.id.cancel).setOnClickListener(v1 -> alertDialog.dismiss());
        alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void setSelectedBookChapter(){
        PreferenceSettings.setSelectedBibleVerses(new ArrayList<>());
        if(isSearch){
            bibleAdapter.setAdapter(searchBibleList);
        }else{
            reloadBible = true;
            dataViewModel.setBibleFilter(default_selected_book,default_selected_chapter);
        }
        if(isBibleLayoutLoaded){
            hide_bottom_layout();
        }
    }


    @Override
    public List<String> getDownloadedVersion() {
        return versions;
    }

    @Override
    public void show_bottom_layout() {
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        sheetBehavior.setPeekHeight((int) getResources().getDimension(R.dimen.bottom_media_peek_height));
        container_params.bottomMargin = (int) getResources().getDimension(R.dimen.bottom_bible_peek_height);
        hide_next_previous_nav();
    }

    @Override
    public void hide_bottom_layout() {
        sheetBehavior.setPeekHeight(0);
        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        container_params.bottomMargin = 0;
        if(!isSearch)set_next_previous_visibility();
    }

    @Override
    public void set_selected_verses(Bible bible, String selected_version) {
        List<SBible> bibleList = new ArrayList<>();
        if(PreferenceSettings.getSelectedBibleVerses()!=null){
            bibleList = PreferenceSettings.getSelectedBibleVerses();
        }
        SBible sBible = new SBible();
        sBible.setId(bible.getId());
        sBible.setBook(bible.getBook());
        sBible.setChapter(bible.getChapter());
        sBible.setVerse(bible.getVerse());
        switch (selected_version){
            case Constants.BIBLE_VERSIONS.AMP:
                sBible.setContent(bible.getAMP());
                sBible.setVersion("Amplified Bible");
                break;
            case Constants.BIBLE_VERSIONS.KJV:
                sBible.setContent(bible.getKJV());
                sBible.setVersion("King James Version");
                break;
            case Constants.BIBLE_VERSIONS.NKJV:
                sBible.setContent(bible.getNKJV());
                sBible.setVersion("New King James Version");
                break;
            case Constants.BIBLE_VERSIONS.NIV:
                sBible.setContent(bible.getNIV());
                sBible.setVersion("New International Version");
                break;
            case Constants.BIBLE_VERSIONS.NLT:
                sBible.setContent(bible.getNLT());
                sBible.setVersion("New Living Translation");
                break;
            case Constants.BIBLE_VERSIONS.MSG:
                sBible.setContent(bible.getMSG());
                sBible.setVersion("The Message Bible");
                break;
            case Constants.BIBLE_VERSIONS.NRSV:
                sBible.setContent(bible.getNRSV());
                sBible.setVersion("New Revised Standard Version");
                break;
        }

        bibleList.add(sBible);
        PreferenceSettings.setSelectedBibleVerses(bibleList);
    }

    @Override
    public void remove_selected_verses(Bible bible) {
        List<SBible> bibleList = new ArrayList<>();
        if(PreferenceSettings.getSelectedBibleVerses()!=null){
            bibleList = PreferenceSettings.getSelectedBibleVerses();
        }
        for (Iterator<SBible> iterator = bibleList.iterator(); iterator.hasNext(); ) {
            SBible value = iterator.next();
            if(value.getId() == bible.getId()){
                iterator.remove();
            }
        }
        PreferenceSettings.setSelectedBibleVerses(bibleList);
    }

    @Override
    public boolean is_bible_verse_selected(Bible bible) {
        List<SBible> bibleList = new ArrayList<>();
        if(PreferenceSettings.getSelectedBibleVerses()!=null){
            bibleList = PreferenceSettings.getSelectedBibleVerses();
        }
        for (SBible bibl: bibleList) {
            if(bibl.getId() == bible.getId()){
                return true;
            }
        }
        return false;
    }

    @Override
    public SBible is_bible_verse_highlighted(Bible bible) {
        List<SBible> bibleList = new ArrayList<>();
        if(PreferenceSettings.getHighlightedBibleVerses()!=null){
            bibleList = PreferenceSettings.getHighlightedBibleVerses();
        }
        for (SBible bibl: bibleList) {
            if(bibl.getId() == bible.getId()){
                return bibl;
            }
        }
        return null;
    }

    @Override
    public int get_total_selected_verses() {
        List<SBible> bibleList = new ArrayList<>();
        if(PreferenceSettings.getSelectedBibleVerses()!=null){
            bibleList = PreferenceSettings.getSelectedBibleVerses();
        }
        return bibleList.size();
    }

    public void remove_highlight_verse(Bible bible) {
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
        PreferenceSettings.setHighlightedBibleVerses(bibleList);
    }

    private void setSelectedDefaultValues(){
        default_selected_version = PreferenceSettings.getDefaultSelectedVersion();
        if(default_selected_version.equalsIgnoreCase("") && versions.size()>0){
            default_selected_version = versions.get(0);
            PreferenceSettings.setDefaultSelectedVersion(default_selected_version);
        }
        default_selected_book = PreferenceSettings.getDefaultSelectedBook();
        if(default_selected_book.equalsIgnoreCase("")){
            default_selected_book = "Genesis";
            PreferenceSettings.setDefaultSelectedBook(default_selected_book);
        }
        default_selected_chapter = PreferenceSettings.getDefaultSelectedChapter();
        if(default_selected_chapter == 0){
            default_selected_chapter = 1;
            PreferenceSettings.setDefaultSelectedChapter(default_selected_chapter);
        }
    }

    private void set_next_previous_visibility(){
        int chapter_ = Integer.parseInt(chapters.get(books.indexOf(book_chip.getText().toString())));
        int current_chapter = PreferenceSettings.getDefaultSelectedChapter();
        if(current_chapter<=1){
            previous.setVisibility(View.GONE);
            shouldHidePrevious = true;
        }else{
            previous.setVisibility(View.VISIBLE);
            shouldHidePrevious = false;
        }

        if(current_chapter+1 > chapter_){
            next.setVisibility(View.GONE);
            shouldHideNext = true;
        }else{
            next.setVisibility(View.VISIBLE);
            shouldHideNext = false;
        }
    }

    private void set_next_chapter(){
        int current_chapter = PreferenceSettings.getDefaultSelectedChapter();
        int next = current_chapter+1;
        chapter_chip.setText("Chapter "+next);
        PreferenceSettings.setDefaultSelectedChapter(next);
        setSelectedDefaultValues();
        setSelectedBookChapter();
    }

    private void set_previous_chapter(){
        int current_chapter = PreferenceSettings.getDefaultSelectedChapter();
        int next = current_chapter-1;
        chapter_chip.setText("Chapter "+next);
        PreferenceSettings.setDefaultSelectedChapter(next);
        setSelectedDefaultValues();
        setSelectedBookChapter();
    }

    //bottom layout click methods
    public void highlight_verses(View view) {
        colorPickerDialog.show(getFragmentManager(), "color_dialog_test");
    }

    public void share_selected_bible_verses(View view){
        List<SBible> selectedBibleList = new ArrayList<>();
        if(PreferenceSettings.getSelectedBibleVerses()!=null){
            selectedBibleList = PreferenceSettings.getSelectedBibleVerses();
        }
        String text_to_share = Utility.get_bibleverses_as_string(Utility.arrange_selected_bible_verses(selectedBibleList));
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text_to_share);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
        setSelectedBookChapter();
    }

    public void copy_selected_bible_verses(View view){
        List<SBible> selectedBibleList = new ArrayList<>();
        if(PreferenceSettings.getSelectedBibleVerses()!=null){
            selectedBibleList = PreferenceSettings.getSelectedBibleVerses();
        }
        String text_to_copy = Utility.get_bibleverses_as_string(Utility.arrange_selected_bible_verses(selectedBibleList));
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text_to_copy);
        clipboard.setPrimaryClip(clip);
        setSelectedBookChapter();
        Toast.makeText(App.getContext(),"Copied to clipboard",Toast.LENGTH_SHORT).show();
    }

    public void bookmark_selected_bible_verses(View view){
        List<SBible> selectedBibleList = new ArrayList<>();
        if(PreferenceSettings.getSelectedBibleVerses()!=null){
            selectedBibleList = PreferenceSettings.getSelectedBibleVerses();
        }
        String text_to_copy = Utility.get_bibleverses_as_string_for_bookmarks(Utility.arrange_selected_bible_verses(selectedBibleList));
        Intent intent = new Intent(BibleActivity.this, NewNotesActivity.class);
        intent.putExtra("content", text_to_copy);
        startActivity(intent);
    }

    private void read_selected_bible_verses(){
      /*  if(textToSpeech.isSpeaking()){
            textToSpeech.stop();
        }else {
            List<SBible> selectedBibleList = new ArrayList<>();
            if(PreferenceSettings.getSelectedBibleVerses()!=null){
                selectedBibleList = PreferenceSettings.getSelectedBibleVerses();
            }
            String text_to_read = Utility.get_bibleverses_as_string_for_speech(Utility.arrange_selected_bible_verses(selectedBibleList));
            textToSpeech.speak(text_to_read, TextToSpeech.QUEUE_FLUSH, null,TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
        }*/
        if(textToSpeech.isSpeaking()){
            textToSpeech.stop();
            speak.setTitle(getString(R.string.read_chapter));
        }else {
            if(bibleList.size()>20) {
                read_list = Utility.split(bibleList,5);
                read_position = 0;

                String text_to_read = Utility.get_bibleverses_as_string_for_speech((List<Bible>) read_list.get(0));
                textToSpeech.speak(text_to_read, TextToSpeech.QUEUE_FLUSH, null,TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
                speak.setTitle(getString(R.string.stop_reading));
            }else{
                read_list = null;
                read_position = 0;
                String text_to_read = Utility.get_bibleverses_as_string_for_speech(bibleList);
                textToSpeech.speak(text_to_read, TextToSpeech.QUEUE_FLUSH, null,TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
                speak.setTitle(getString(R.string.stop_reading));
            }

        }
    }

    public void cancel_bottom_layout(View view){
        setSelectedBookChapter();
    }
}
