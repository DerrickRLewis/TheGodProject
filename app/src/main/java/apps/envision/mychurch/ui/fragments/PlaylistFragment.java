package apps.envision.mychurch.ui.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.AppDb;
import apps.envision.mychurch.db.DataInterfaceDao;
import apps.envision.mychurch.db.DataViewModel;
import apps.envision.mychurch.interfaces.PlaylistListener;
import apps.envision.mychurch.pojo.Playlist;
import apps.envision.mychurch.pojo.PlaylistMedias;
import apps.envision.mychurch.ui.activities.PlaylistActivity;
import apps.envision.mychurch.ui.adapters.PlaylistsAdapter;
import apps.envision.mychurch.utils.ImageLoader;

public class PlaylistFragment extends Fragment implements PlaylistListener {

    private View view;
    private PlaylistsAdapter playlistsAdapter;

    private List<Playlist> playlistList = new ArrayList<>();
    private DataViewModel dataViewModel;

    private DataInterfaceDao dataInterfaceDao;
    private ExecutorService executorService;

    private String media_type = "";

    public static PlaylistFragment newInstance(String media_type) {
        PlaylistFragment playlistFragment = new PlaylistFragment();
        Bundle args = new Bundle();
        args.putString("media_type", media_type);
        playlistFragment.setArguments(args);
        return playlistFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.content_add_playlist, container, false);

        Bundle args = getArguments();
        assert args != null;
        media_type = args.getString("media_type", getString(R.string.audio));
        Log.e("media_type",media_type);

        init_views();
        dataInterfaceDao = AppDb.getDatabase(App.getContext()).dataDbInterface();
        executorService = Executors.newCachedThreadPool();
        // Get a new or existing ViewModel from the ViewModelProvider.
        dataViewModel = ViewModelProviders.of(this).get(DataViewModel.class);
        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.

        //set playlist type filter parameter
        dataViewModel.setMediaFilterType(media_type);

        dataViewModel.getPlaylists().observe(this, playList -> {
            // Update the cached copy of the interests in the adapter.
            //we only set the items from db, the first time fragment is launched
            playlistList = playList;
            if(playlistList==null) playlistList = new ArrayList<>();
            playlistList.add(0,null);
            playlistsAdapter.setAdapter(playlistList);
        });
        return view;
    }

    private void init_views(){
        RecyclerView playlist = view.findViewById(R.id.playlist);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        playlist.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        playlist.setHasFixedSize(true);
        playlist.setItemAnimator(new DefaultItemAnimator());
        playlistsAdapter = new PlaylistsAdapter(this,media_type);
        playlist.setAdapter(playlistsAdapter);
    }

    @Override
    public void new_playlist() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.new_playlist));
        // I'm using fragment here so I'm using getView() to provide ViewGroup
        // but you can provide here any other instance of ViewGroup from your Fragment / Activity
        View view = getLayoutInflater().inflate(R.layout.new_playlist, null);
        // Set up the input
        final EditText input = view.findViewById(R.id.input);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        builder.setView(view);

        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String userInputValue = input.getText().toString().trim();
                if(userInputValue.length() != 0) {
                    if (isPlaylistNameExists(userInputValue)) {
                        Toast.makeText(getActivity() , "Playlist "+userInputValue+" already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        if(userInputValue.length()<3){
                            Toast.makeText(getActivity() , getString(R.string.playlist_name_short_hint), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Playlist playlist = new Playlist();
                        playlist.setTitle(userInputValue);
                        playlist.setMedia_type(media_type);
                        dataViewModel.createPlaylist(playlist);
                    }
                }else{
                    Toast.makeText(getActivity() , getString(R.string.playlist_name_empty_hint), Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public boolean isMediaAddedTOPlaylist(Playlist playlist) {
        return false;
    }

    @Override
    public void playListOnClick(Playlist playlist) {
        Gson gson = new Gson();
        String myJson = gson.toJson(playlist);
        Intent intent = new Intent(getActivity(), PlaylistActivity.class);
        intent.putExtra("playlist", myJson);
        startActivity(intent);
    }

    @Override
    public void setThumbnail(ImageView imageView, Playlist playlist) {
        executorService.execute(() -> {
            PlaylistMedias playlistMedias = dataInterfaceDao.getPlaylistMedia(playlist.getId());
            if(playlistMedias!=null){
                setPlaylistImage(imageView,playlistMedias.getCover_photo());
            }
        });
    }

    @Override
    public void setPlaylistSize(TextView textView, Playlist playlist) {
        executorService.execute(() -> {
            int count = dataInterfaceDao.countPlaylistMedia(playlist.getId());
            setMediaCount(textView,count +" " + playlist.getMedia_type() + "(s)");
        });
    }

    @Override
    public boolean showOptions() {
        return true;
    }

    @Override
    public void onOptionsClick(Playlist playlist, View view) {
        PopupMenu popup = new PopupMenu(getActivity(), view);
        popup.getMenuInflater().inflate(R.menu.playlist_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            switch (id) {
                case R.id.clear:
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Clear all media")
                            .setMessage("Go ahead and remove all media from this playlist")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dataViewModel.deleteAllPlaylistMedia(playlist.getId());
                                    dataViewModel.createPlaylist(playlist);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    break;
                case R.id.delete:
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Delete this playlist")
                            .setMessage("Go ahead to delete and remove all media from this playlist")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dataViewModel.deletePlaylist(playlist.getId());
                                    dataViewModel.deleteAllPlaylistMedia(playlist.getId());
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    break;
            }
            return true;
        });

        popup.show();
    }

    private void setMediaCount(TextView textView,String count){
        if(getActivity()!=null)
        getActivity().runOnUiThread(() -> textView.setText(count));
    }

    private void setPlaylistImage(ImageView imageView,String url){
        if(getActivity()!=null)
            getActivity().runOnUiThread(() -> ImageLoader.loadImage(imageView,url));
    }

    private boolean isPlaylistNameExists(String title){
        if(playlistList==null)return false;
        for (Playlist playlist:playlistList) {
            if(playlist!=null && playlist.getTitle().equalsIgnoreCase(title))
                return true;
        }
        return false;
    }
}

