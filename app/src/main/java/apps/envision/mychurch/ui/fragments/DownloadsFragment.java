package apps.envision.mychurch.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.db.DataViewModel;
import apps.envision.mychurch.interfaces.MediaClickListener;
import apps.envision.mychurch.interfaces.MediaOptionsListener;
import apps.envision.mychurch.pojo.Bookmarks;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.ui.activities.AddPlaylistActivity;
import apps.envision.mychurch.ui.activities.DownloadsActivity;
import apps.envision.mychurch.ui.adapters.ListMediaAdapter;
import apps.envision.mychurch.utils.FileManager;
import apps.envision.mychurch.utils.MediaOptions;
import apps.envision.mychurch.utils.ObjectMapper;


/**
 * A simple {@link Fragment} subclass.
 */
public class DownloadsFragment extends Fragment implements MediaOptionsListener, MediaClickListener, LocalMessageCallback {


    private View view;
    //utility class to help in loading files from folder
    private FileManager fileManager;
    private ArrayList<Media> downloads_list = new ArrayList<>();
    private ListMediaAdapter adapter;
    private RecyclerView list;
    private TextView no_downloads;

    private DataViewModel dataViewModel;

    private MediaOptions mediaOptions;

    private List<Bookmarks> bookmarksList = new ArrayList<>();

    private String media_type = "";

    /**
     * @return
     */
    public static DownloadsFragment newInstance(String media_type) {
        DownloadsFragment downloadsFragment = new DownloadsFragment();
        Bundle args = new Bundle();
        args.putString("media_type", media_type);
        downloadsFragment.setArguments(args);
        return downloadsFragment;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // your stuff or nothing
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
        view = inflater.inflate(R.layout.fragment_downloads, container, false);
        fileManager = new FileManager();
        mediaOptions = new MediaOptions(getActivity(),this);

        Bundle args = getArguments();
        assert args != null;
        media_type = args.getString("media_type", getString(R.string.audio));

        init_views();

        // Get a new or existing ViewModel from the ViewModelProvider.
        dataViewModel = ViewModelProviders.of(this).get(DataViewModel.class);
        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.

        dataViewModel.getBookmarks().observe(this, bookmarksList -> {
            // Update the cached copy of the interests in the adapter.
            //we only set the items from db, the first time fragment is launched
            this.bookmarksList = bookmarksList;
        });
        if(DownloadsActivity.isStoragePermissionGranted){
            load_downloaded_files();
        }
        return view;
    }

    private void init_views(){
        no_downloads = view.findViewById(R.id.no_downloads);
        list = view.findViewById(R.id.list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        list.setLayoutManager(new GridLayoutManager(getActivity(), 1));

        list.setHasFixedSize(true);
        list.setItemAnimator(new DefaultItemAnimator());
        adapter = new ListMediaAdapter(this);
        list.setAdapter(adapter);
    }

    /**
     * fetch all downloaded file
     * from our download folder
     * display empty records if downloads list is empty
     */
    public void load_downloaded_files(){
        downloads_list = fileManager.getDownloads(media_type);
        adapter.setAdapter(downloads_list);
        if(downloads_list.size()==0){
            if(media_type.equalsIgnoreCase(App.getContext().getString(R.string.audio))){
                no_downloads.setText(getString(R.string.no_downloaded_audio));
            }else{
                no_downloads.setText(getString(R.string.no_downloaded_videos));
            }
            no_downloads.setVisibility(View.VISIBLE);
            list.setVisibility(View.GONE);
        }else{
            no_downloads.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public boolean isBookmarked(Media media) {
        for (Bookmarks bookmarks: bookmarksList) {
            if(bookmarks.getId() == bookmarks.getId())
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
    public boolean isDownloads() {
        return true;
    }

    @Override
    public void OnItemClick(Media media, String type) {
        mediaOptions.play_media(dataViewModel,downloads_list,media);
    }

    @Override
    public void OnOptionClick(Media media, View view) {
        mediaOptions.display(view,media);
    }

    @Override
    public void onStart() {
        LocalMessageManager.getInstance().addListener(this);
        super.onStart();
    }

    @Override
    public void onDestroy() {
        LocalMessageManager.getInstance().removeListener(this);
        super.onDestroy();
    }

    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
        switch (localMessage.getId()){
            case R.id.reload_downloads:
                String type = (String) localMessage.getObject();
                if(type!= null && type.equalsIgnoreCase(media_type))
                    load_downloaded_files();
                break;
            case R.id.request_storage_permission_granted:
                load_downloaded_files();
                break;
        }
    }
}

