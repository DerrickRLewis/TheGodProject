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

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.DataViewModel;
import apps.envision.mychurch.interfaces.MediaClickListener;
import apps.envision.mychurch.interfaces.MediaOptionsListener;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.ui.activities.AddPlaylistActivity;
import apps.envision.mychurch.ui.adapters.ListMediaAdapter;
import apps.envision.mychurch.utils.MediaOptions;
import apps.envision.mychurch.utils.ObjectMapper;

/**
 * fragment to load users favorite medias from the apps database
 */
public class BookmarksFragment extends Fragment implements MediaClickListener, MediaOptionsListener {

    List<Media> mediaList = new ArrayList<>();
    private ListMediaAdapter adapter;
    RecyclerView recyclerView;
    View view;
    TextView message,item_number;

    private MediaOptions mediaOptions;

    private DataViewModel dataViewModel;



    public static BookmarksFragment newInstance() {
        return new BookmarksFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.bookmarks_fragment, container, false);

        mediaOptions = new MediaOptions(getActivity(),this);

        recyclerView = view.findViewById(R.id.list);
        message = view.findViewById(R.id.message);
        item_number = view.findViewById(R.id.item_number);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new ListMediaAdapter(this);
        recyclerView.setAdapter(adapter);

        // Get a new or existing ViewModel from the ViewModelProvider.
        dataViewModel = ViewModelProviders.of(this).get(DataViewModel.class);
        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.

        dataViewModel.getBookmarks().observe(this, bookmarksList -> {
            // Update the cached copy of the interests in the adapter.
            //we only set the items from db, the first time fragment is launched
            if(mediaList!=null) {
                mediaList = ObjectMapper.mapBoomarkedMedia(bookmarksList);
            }

            if(mediaList==null || mediaList.size() == 0){
                message.setVisibility(View.VISIBLE);
                item_number.setText(getResources().getString(R.string.bookmarked_medias));
            }else{
                String size = mediaList.size()==1?getResources().getString(R.string.saved_video):getResources().getString(R.string.bookmarked_medias);
                item_number.setText(String.valueOf(mediaList.size()) +" "+ size);
                message.setVisibility(View.GONE);
            }
            adapter.setAdapter(mediaList);
        });


        return view;
    }
    @Override
    public void OnItemClick(Media media, String type) {
        //clear previous playing songs
        mediaOptions.play_media(dataViewModel,mediaList,media);
    }

    @Override
    public void OnOptionClick(Media media, View view) {
        mediaOptions.display(view,media);
    }

    @Override
    public boolean isBookmarked(Media media) {
        return true;
    }

    @Override
    public void bookmark(Media media) {
        dataViewModel.removeMediaFromBookmarks(media.getId());
        Toast.makeText(getActivity(), R.string.media_unbookmarked,Toast.LENGTH_SHORT).show();
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
        return false;
    }
}

