package apps.envision.mychurch.ui.fragments;


import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.libs.bible_download.BibleDownloadWorker;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.interfaces.VersionsClickListener;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.BibleDownload;
import apps.envision.mychurch.pojo.BibleVersions;
import apps.envision.mychurch.ui.adapters.BibleVersionsAdapter;
import apps.envision.mychurch.utils.Constants;
import apps.envision.mychurch.utils.FileManager;
import apps.envision.mychurch.utils.Utility;

import static apps.envision.mychurch.libs.bible_download.BibleDownloadWorker.DOWNLOAD_ARG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BibleDownloadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BibleDownloadFragment extends Fragment implements VersionsClickListener, LocalMessageCallback {

    private BibleVersionsAdapter bibleVersionsAdapter;

    public BibleDownloadFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BibleDownloadFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BibleDownloadFragment newInstance() {
        return new BibleDownloadFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bible_download, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(
                new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        bibleVersionsAdapter = new BibleVersionsAdapter(this);
        recyclerView.setAdapter(bibleVersionsAdapter);
        bibleVersionsAdapter.setAdapter(getVersions());

        return view;
    }

    private List<BibleVersions> getVersions(){
        List<BibleVersions> bibleVersions = new ArrayList<>();
        String[] titles = getResources().getStringArray(R.array.bible_version_titles);
        String[] descriptions = getResources().getStringArray(R.array.bible_version_descriptions);
        String[] books = getResources().getStringArray(R.array.bible_version_names);

        for (int i=0; i<titles.length; i++) {
            BibleVersions versions = new BibleVersions();
            versions.setTitle(titles[i]);
            versions.setDescription(descriptions[i]);
            versions.setBook(books[i]);
            bibleVersions.add(versions);
        }
        return bibleVersions;
    }

    @Override
    public void downloadVersion(BibleVersions bibleVersions) {
        if(PreferenceSettings.isBibleDownloadInProgress()){
            new Utility(getActivity()).show_download_progress_alert(getString(R.string.download_in_progress),getString(R.string.bible_download_hint_error));
            return;
        }
        androidx.appcompat.app.AlertDialog.Builder builder;
        if(PreferenceSettings.getBibleThemeMode()==1){
            builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity(),R.style.AlertDialogCustomLight);
        }else {
            builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity(),R.style.AlertDialogCustomDark);
        }
        builder.setTitle("Download "+bibleVersions.getBook()+" Bible Version");
        builder.setMessage(getString(R.string.bible_download_hint));

        builder.setPositiveButton("Ok",
                (dialog, which) -> {
                    // positive button logic
                    dialog.dismiss();
                    startDownload(bibleVersions.getBook());
                });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }

    @Override
    public void cancelDownload(UUID uuid,String book) {
        androidx.appcompat.app.AlertDialog.Builder builder;
        if(PreferenceSettings.getBibleThemeMode()==1){
            builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity(),R.style.AlertDialogCustomLight);
        }else {
            builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity(),R.style.AlertDialogCustomDark);
        }
        builder
                .setTitle(book + getString(R.string.version_download))
                .setMessage(R.string.version_download_hint)
                .setPositiveButton("Ok", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    WorkManager.getInstance().cancelWorkById(uuid);
                    Log.e("SimpleWork uuid: " ,String.valueOf(uuid));
                    Toast.makeText(App.getContext(), book + " bible version download cancelled", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(false)
                .create()
                .show();
    }


    public void startDownload(String name) {
        BibleDownload bibleDownload = PreferenceSettings.getCurrentBibleDownload(name);
        if(bibleDownload==null) {
            bibleDownload = new BibleDownload();
            bibleDownload.setTitle(name + " Version");
            bibleDownload.setBook(name);
            bibleDownload.setFile_path(FileManager.getBibleDestinationFolder());
            bibleDownload.setNotify_id(new Random().nextInt(10000));
            switch (bibleDownload.getBook()) {
                case Constants.BIBLE_VERSIONS.AMP:
                    bibleDownload.setDownload_path(Constants.BIBLE_DOWNLOAD_URL+getString(R.string.amp));
                    bibleDownload.setDownload_task_identifier(Constants.BIBLE_DOWNLOAD_IDENTFIER.AMP_IDENTIFIER);
                    break;
                case Constants.BIBLE_VERSIONS.KJV:
                    bibleDownload.setDownload_path(Constants.BIBLE_DOWNLOAD_URL+getString(R.string.kjv));
                    bibleDownload.setDownload_task_identifier(Constants.BIBLE_DOWNLOAD_IDENTFIER.KJV_IDENTIFIER);
                    break;
                case Constants.BIBLE_VERSIONS.MSG:
                    bibleDownload.setDownload_path(Constants.BIBLE_DOWNLOAD_URL+getString(R.string.msg));
                    bibleDownload.setDownload_task_identifier(Constants.BIBLE_DOWNLOAD_IDENTFIER.MSG_IDENTIFIER);
                    break;
                case Constants.BIBLE_VERSIONS.NIV:
                    bibleDownload.setDownload_path(Constants.BIBLE_DOWNLOAD_URL+getString(R.string.niv));
                    bibleDownload.setDownload_task_identifier(Constants.BIBLE_DOWNLOAD_IDENTFIER.NIV_IDENTIFIER);
                    break;
                case Constants.BIBLE_VERSIONS.NKJV:
                    bibleDownload.setDownload_path(Constants.BIBLE_DOWNLOAD_URL+getString(R.string.nkjv));
                    bibleDownload.setDownload_task_identifier(Constants.BIBLE_DOWNLOAD_IDENTFIER.NKJV_IDENTIFIER);
                    break;
                case Constants.BIBLE_VERSIONS.NLT:
                    bibleDownload.setDownload_path(Constants.BIBLE_DOWNLOAD_URL+getString(R.string.nlt));
                    bibleDownload.setDownload_task_identifier(Constants.BIBLE_DOWNLOAD_IDENTFIER.NLT_IDENTIFIER);
                    break;
                case Constants.BIBLE_VERSIONS.NRSV:
                    bibleDownload.setDownload_path(Constants.BIBLE_DOWNLOAD_URL+getString(R.string.nrsv));
                    bibleDownload.setDownload_task_identifier(Constants.BIBLE_DOWNLOAD_IDENTFIER.NRSV_IDENTIFIER);
                    break;
            }
            PreferenceSettings.setCurrentBibleDownload(name,bibleDownload);
        }

        // Create the Data object:
        Data myData = new Data.Builder()
                .putString(DOWNLOAD_ARG, name)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiresStorageNotLow(true)
                .setRequiresBatteryNotLow(true)
                .build();

        // ...then create and enqueue a OneTimeWorkRequest that uses those arguments
        OneTimeWorkRequest downloadWork = new OneTimeWorkRequest.Builder(BibleDownloadWorker.class)
                .setInputData(myData)
                .addTag(bibleDownload.getDownload_task_identifier())
                .setConstraints(constraints)
                .build();
        bibleDownload.setDownload_id(downloadWork.getId());
        PreferenceSettings.setCurrentBibleDownload(bibleDownload.getBook(),bibleDownload);
        WorkManager.getInstance()
                .beginUniqueWork(bibleDownload.getDownload_task_identifier(), ExistingWorkPolicy.KEEP,downloadWork).enqueue();

        WorkManager.getInstance().getWorkInfoByIdLiveData(downloadWork.getId())
                .observe(this, workInfo -> {
                    // Do something with the status
                    Log.e("SimpleWorkRequest: " ,String.valueOf(workInfo));
                    if (workInfo != null) {

                        Log.e("SimpleWorkRequest: " ,workInfo.getState().name() + "\n");
                    }
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        Log.e("SimpleWorkRequest: " ,"finished executing");
                    }
                });
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
        if(localMessage.getId() == R.id.bible_download_progress){
            String book = (String) localMessage.getObject();
            bibleVersionsAdapter.updateAdapter(book);
        }

        if(localMessage.getId() == R.id.bible_download_complete){
            String book = (String) localMessage.getObject();
            bibleVersionsAdapter.updateAdapter(book);
        }
    }
}