package apps.envision.mychurch.ui.fragments;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.AppDb;
import apps.envision.mychurch.db.DataInterfaceDao;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.libs.materialspinner.MaterialSpinner;
import apps.envision.mychurch.pojo.BibleDownload;
import apps.envision.mychurch.ui.activities.DownloadBibleActivity;
import apps.envision.mychurch.utils.Constants;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReadBibleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReadBibleFragment extends Fragment implements View.OnClickListener, LocalMessageCallback {

    private FrameLayout parent_layout;
    private int bible_count =0;
    private DataInterfaceDao dataInterfaceDao;
    private ExecutorService executorService;
    List<String> books = new ArrayList<>();
    List<String> chapters = new ArrayList<>();
    List<String> versions =  new ArrayList<String>();


    private Chip version_chip, book_chip, chapter_chip;
    private Dialog dialog;
    private AlertDialog alertDialog;
    private AppCompatEditText searchEditText;
    private ChipGroup chip_group;

    public ReadBibleFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BibleDownloadFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReadBibleFragment newInstance() {
        return new ReadBibleFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        AppDb db = AppDb.getDatabase(App.getContext());
        dataInterfaceDao = db.dataDbInterface();
        executorService = Executors.newSingleThreadExecutor();

        //get bible books and versions
        getAvailableVersions();
        books = Arrays.asList(getResources().getStringArray(R.array.bible_books));
        chapters = Arrays.asList(getResources().getStringArray(R.array.bible_books_chapters));

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.read_bible_layout, container, false);
        parent_layout = view.findViewById(R.id.parent_view);
        check_and_load_view();
        return view;
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
            if(getActivity()==null)return;
            getActivity().runOnUiThread(() -> {
                if(bible_count==0){
                    init_empty_layout();
                }else{
                    init_bible_layout();
                }
            });
        });
    }

    @Override
    public void onClick(View v) {
       switch (v.getId()){
           case R.id.download_bible:
               startActivity(new Intent(getActivity(), DownloadBibleActivity.class));
               break;
           case R.id.version:
               loadVersions();
               break;
           case R.id.book:
               loadBooks();
               break;
           case R.id.chapter:
               loadChapters();
               break;

       }
    }

    private void init_bible_layout(){
        if(getActivity()==null)return;
        clearLayout();
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.fragment_read_bible, null);
        // Add the new row before the add field button.

        version_chip = rowView.findViewById(R.id.version);
        if(versions.size()>0){
            version_chip.setText(versions.get(0));
        }
        book_chip = rowView.findViewById(R.id.book);
        if(books.size()>0){
            book_chip.setText(books.get(0));
        }
        chapter_chip = rowView.findViewById(R.id.chapter);
        chapter_chip.setText("Chapter 1");

        version_chip.setOnClickListener(this);
        book_chip.setOnClickListener(this);
        chapter_chip.setOnClickListener(this);


        parent_layout.addView(rowView, 0);
    }

    private void init_empty_layout(){
        if(getActivity()==null)return;
        clearLayout();
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
    }

    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
        if(localMessage.getId() == R.id.bible_download_complete){
            if(bible_count==0){
                check_and_load_view();
            }
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

      /*  AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.bible_chip_layout, null);
        dialogBuilder.setView(view);
        //view.findViewById(R.id.cancel).setOnClickListener(v1 -> alertDialog.dismiss());
        alertDialog = dialogBuilder.create();
        alertDialog.show();*/


        View view = LayoutInflater.from(getActivity()).inflate(R.layout.bible_dialog, null);
        chip_group = view.findViewById(R.id.chip_group);
        searchEditText = view.findViewById(R.id.searchEditText);
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


        dialog = new Dialog(getActivity(), R.style.MaterialList);
        dialog.setContentView(view);
        dialog.setCancelable(true);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setGravity(Gravity.TOP);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        dialog.getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        dialog.show();
    }

    private void setBooksChipsView(List<String> booksList){
        chip_group.removeAllViews();
        chip_group.setPadding(0,0,0,0);
        chip_group.setChipSpacingHorizontal(10);
        chip_group.setChipSpacingVertical(10);
        for (String book: booksList) {
            Chip chip = new Chip(getActivity());
            chip.setText(book);
            if(book_chip.getText().toString().equalsIgnoreCase(book)){
                chip.setChipBackgroundColorResource(R.color.material_blue_grey_700);
                chip.setChipIcon(App.getContext().getResources().getDrawable(R.drawable.ic_check_white_24dp));
                chip.setChipIconSize(40);
                chip.setChipStartPadding(15);
                chip.setChipIconTintResource(R.color.white);
            }else {
                chip.setChipBackgroundColorResource(R.color.material_blue_grey_700);
            }
            chip.setTextColor(getResources().getColor(R.color.white));
            chip.setOnClickListener(v -> {
                book_chip.setText(book);
                Log.e("book index",String.valueOf(books.indexOf(book)));
                dialog.dismiss();
            });
            chip_group.addView(chip);
        }
    }

    /**
     * here we load the current playlist in a dialog
     */
    private void loadVersions() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.bible_chip_layout, null);
        dialogBuilder.setView(view);

        ChipGroup chip_group = view.findViewById(R.id.chip_group);
        chip_group.removeAllViews();
        for (String version: versions) {
            Chip chip = new Chip(getActivity());
            chip.setText(version);
            if(version_chip.getText().toString().equalsIgnoreCase(version)){
                chip.setChipBackgroundColorResource(R.color.material_blue_grey_700);
                chip.setChipIcon(App.getContext().getResources().getDrawable(R.drawable.ic_check_white_24dp));
                chip.setChipIconSize(40);
                chip.setChipStartPadding(15);
                chip.setChipIconTintResource(R.color.white);
            }else {
                chip.setChipBackgroundColorResource(R.color.material_blue_grey_700);
            }
            chip.setTextColor(getResources().getColor(R.color.white));
            chip.setOnClickListener(v -> {
                version_chip.setText(version);
                alertDialog.dismiss();
            });
            chip_group.addView(chip);
        }

        //view.findViewById(R.id.cancel).setOnClickListener(v1 -> alertDialog.dismiss());
        alertDialog = dialogBuilder.create();
        alertDialog.show();
    }



    private void loadChapters() {
        int chapter_ = Integer.parseInt(chapters.get(books.indexOf(book_chip.getText().toString())));
        Log.e("chapter",String.valueOf(chapter_));
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.bible_chip_layout, null);
        dialogBuilder.setView(view);

        ChipGroup chip_group = view.findViewById(R.id.chip_group);
        chip_group.removeAllViews();
        chip_group.setPadding(0,0,0,0);
        chip_group.setChipSpacingHorizontal(10);
        chip_group.setChipSpacingVertical(10);
        for (int i=1; i<=chapter_; i++) {
            Chip chip = new Chip(getActivity());
            String txt = String.valueOf(i);
            chip.setText(txt);
            if(chapter_chip.getText().toString().equalsIgnoreCase("Chapter "+txt)){
                chip.setChipBackgroundColorResource(R.color.material_blue_grey_700);
                chip.setChipIcon(App.getContext().getResources().getDrawable(R.drawable.ic_check_white_24dp));
                chip.setChipIconSize(40);
                chip.setChipStartPadding(15);
                chip.setChipIconTintResource(R.color.white);
            }else {
                chip.setChipBackgroundColorResource(R.color.material_blue_grey_700);
            }
            chip.setTextColor(getResources().getColor(R.color.white));
            chip.setOnClickListener(v -> {
                chapter_chip.setText("Chapter "+txt);
                alertDialog.dismiss();
            });
            chip_group.addView(chip);
        }

        //view.findViewById(R.id.cancel).setOnClickListener(v1 -> alertDialog.dismiss());
        alertDialog = dialogBuilder.create();
        alertDialog.show();
    }


}
