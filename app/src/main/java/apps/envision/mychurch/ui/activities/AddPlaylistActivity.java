package apps.envision.mychurch.ui.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.pojo.Playlist;
import apps.envision.mychurch.pojo.PlaylistMedias;
import apps.envision.mychurch.ui.adapters.PlaylistsAdapter;
import apps.envision.mychurch.utils.ImageLoader;
import apps.envision.mychurch.utils.ObjectMapper;

public class AddPlaylistActivity extends AppCompatActivity implements PlaylistListener {

    private PlaylistsAdapter playlistsAdapter;
    private Media media = null;

    private List<PlaylistMedias> playlistMedias = new ArrayList<>();
    private List<Playlist> playlistList = new ArrayList<>();
    private DataViewModel dataViewModel;

    private DataInterfaceDao dataInterfaceDao;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_playlists);
        //get bundle parameter
        Gson gson = new Gson();
        media = gson.fromJson(getIntent().getStringExtra("media"), Media.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle( media.getMedia_type().substring(0, 1).toUpperCase() + media.getMedia_type().substring(1)+" "+getString(R.string.playlists));

        init_views();

        dataInterfaceDao = AppDb.getDatabase(App.getContext()).dataDbInterface();
        executorService = Executors.newCachedThreadPool();
        // Get a new or existing ViewModel from the ViewModelProvider.
        dataViewModel = ViewModelProviders.of(this).get(DataViewModel.class);
        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.

        //set playlist type filter parameter
        dataViewModel.setMediaFilterType(media.getMedia_type());

        dataViewModel.getPlaylists().observe(this, playList -> {
            // Update the cached copy of the interests in the adapter.
            //we only set the items from db, the first time fragment is launched
            playlistList = playList;
            if(playlistList==null) playlistList = new ArrayList<>();
            playlistList.add(0,null);
            playlistsAdapter.setAdapter(playlistList);
        });

        dataViewModel.getAllPlaylistsMedia().observe(this, playlistMedias -> {
            this.playlistMedias = playlistMedias;
        });
    }

    private void init_views(){
        RecyclerView playlist = findViewById(R.id.playlist);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        playlist.setLayoutManager(new GridLayoutManager(this, 1));
        playlist.setHasFixedSize(true);
        playlist.setItemAnimator(new DefaultItemAnimator());
        playlistsAdapter = new PlaylistsAdapter(this,media.getMedia_type());
        playlist.setAdapter(playlistsAdapter);
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
    public void new_playlist() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.new_playlist));
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
                String title = input.getText().toString().trim();
                if(title.length() != 0) {
                    if (isPlaylistNameExists(title)) {
                        Toast.makeText(AddPlaylistActivity.this , "Playlist "+title+" already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        if(title.length()<3){
                            Toast.makeText(AddPlaylistActivity.this , getString(R.string.playlist_name_short_hint), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        createPlaylist(title);
                    }
                }else{
                    Toast.makeText(AddPlaylistActivity.this , getString(R.string.playlist_name_empty_hint), Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public boolean isMediaAddedTOPlaylist(Playlist playlist) {
        if(playlistMedias ==null)return false;
        for (PlaylistMedias _medias: playlistMedias) {
            if(media !=null && (_medias.getPlaylist_id() == playlist.getId() && _medias.getMedia_id() == media.getId())){
                return true;
            }
        }
        return false;
    }

    private void createPlaylist(String title){
        executorService.execute(() -> {
            Playlist playlist = new Playlist();
            playlist.setTitle(title);
            playlist.setMedia_type(media.getMedia_type());
            long id = dataInterfaceDao.createPlaylist(playlist);
            playlist.setId(id);

            runOnUiThread(() -> playListOnClick(playlist));
        });
    }

    @Override
    public void playListOnClick(Playlist playlist) {
        if(media ==null || playlist==null){
            Toast.makeText(this,getString(R.string.cant_add_to_playlist),Toast.LENGTH_SHORT).show();
            return;
        }

        if(!isMediaAddedTOPlaylist(playlist)) {
            dataViewModel.addMediaToPlaylist(ObjectMapper.mapMediaToPlaylist(playlist,media));
            dataViewModel.createPlaylist(playlist);
            Toast.makeText(this, getString(R.string.media_added_to_playlist), Toast.LENGTH_SHORT).show();
            finish();
        }else{
            Toast.makeText(this, "Media already added to this Playlist", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setThumbnail(ImageView imageView, Playlist playlist) {
        executorService.execute(() -> {
           PlaylistMedias playlistSongs = dataInterfaceDao.getPlaylistMedia(playlist.getId());
           if(playlistSongs!=null){
               setPlaylistImage(imageView,playlistSongs.getCover_photo());
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
        return false;
    }

    @Override
    public void onOptionsClick(Playlist playlist,View view) {
        //do nothing
    }

    private void setMediaCount(TextView textView,String count){
        runOnUiThread(() -> textView.setText(count));
    }

    private void setPlaylistImage(ImageView imageView,String url){
        runOnUiThread(() -> ImageLoader.loadImage(imageView,url));
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
