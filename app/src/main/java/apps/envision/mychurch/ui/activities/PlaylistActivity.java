package apps.envision.mychurch.ui.activities;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.AppDb;
import apps.envision.mychurch.db.DataInterfaceDao;
import apps.envision.mychurch.db.DataViewModel;
import apps.envision.mychurch.interfaces.MediaClickListener;
import apps.envision.mychurch.interfaces.MediaOptionsListener;
import apps.envision.mychurch.pojo.Bookmarks;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.pojo.Playlist;
import apps.envision.mychurch.pojo.PlaylistMedias;
import apps.envision.mychurch.ui.adapters.ListMediaAdapter;
import apps.envision.mychurch.utils.MediaOptions;
import apps.envision.mychurch.utils.ObjectMapper;

import static apps.envision.mychurch.utils.Utility.darkenColor;

public class PlaylistActivity extends AppCompatActivity implements View.OnClickListener, MediaOptionsListener, MediaClickListener {

    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView header_img;
    private TextView media_count;
    ListMediaAdapter songsAdapter;
    Playlist playlist = null;
    TextView message;

    private DataInterfaceDao dataInterfaceDao;
    private ExecutorService executorService;
    private DataViewModel dataViewModel;

    private MediaOptions mediaOptions;
    private List<Bookmarks> bookmarksList = new ArrayList<>();
    private List<Media> mediaList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Gson gson = new Gson();
        playlist = gson.fromJson(getIntent().getStringExtra("playlist"), Playlist.class);
        mediaOptions = new MediaOptions(this,this);
        setContentView(R.layout.activity_playlists);

        Toolbar toolbar = (Toolbar) findViewById(R.id.anim_toolbar);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbar.setTitle(getString(R.string.playlist));

        init_views();

        dataInterfaceDao = AppDb.getDatabase(App.getContext()).dataDbInterface();
        executorService = Executors.newCachedThreadPool();
        setPlaylistDetails();
        // Get a new or existing ViewModel from the ViewModelProvider.
        dataViewModel = ViewModelProviders.of(this).get(DataViewModel.class);

        dataViewModel.setPlaylistMediaFilterId(playlist.getId());
        dataViewModel.getPlaylistsMedia().observe(this, playList -> {
            if(playList!=null) {
                this.mediaList = ObjectMapper.mapMediaFromPlaylists(playList);
                setPlaylistMediaCount(mediaList.size());
            }
            if(mediaList.size() == 0){
                message.setVisibility(View.VISIBLE);
                songsAdapter.setAdapter(new ArrayList<>());
            }else{
                message.setVisibility(View.GONE);
                songsAdapter.setAdapter(mediaList);
            }
        });

        dataViewModel.getBookmarks().observe(this, bookmarksList -> {
            // Update the cached copy of the interests in the adapter.
            this.bookmarksList = bookmarksList;
        });
    }

    /**
     * set the selected playlist details
     */
    private void setPlaylistDetails() {
        TextView title = findViewById(R.id.title);
        title.setText(playlist.getTitle().substring(0, 1).toUpperCase() + playlist.getTitle().substring(1));
        header_img = findViewById(R.id.header_img);
        executorService.execute(() -> {
            PlaylistMedias playlistMedias = dataInterfaceDao.getPlaylistMedia(playlist.getId());
            if(playlistMedias!=null){
                setPlaylistImage(playlistMedias.getCover_photo());
            }
        });

    }

    /**
     * set the playlist media count
     * @param count
     */
    private void setPlaylistMediaCount(int count){
        if(count>0) {
            media_count.setVisibility(View.VISIBLE);
            String _mdia_count = count > 100 ? "100+ " : count+" ";
            media_count.setText(_mdia_count + getString(R.string.media_tracks));
        }else{
            media_count.setVisibility(View.GONE);
        }
    }

    /**
     * extract and set the coverphoto for the playlist
     * @param url
     */
    private void setPlaylistImage(String url){
        runOnUiThread(() -> Glide.with(App.getContext())
                .asBitmap().load(url)
                .listener(new RequestListener<Bitmap>() {
                              @Override
                              public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Bitmap> target, boolean b) {
                                  return false;
                              }

                              @Override
                              public boolean onResourceReady(Bitmap bitmap, Object o, Target<Bitmap> target, DataSource dataSource, boolean b) {
                                  header_img.setImageBitmap(bitmap);
                                  Palette.from(bitmap).generate(palette -> {
                                      if(palette!=null) {
                                          int vibrantColor = palette.getVibrantColor(getResources().getColor(R.color.primary));
                                          collapsingToolbar.setContentScrimColor(vibrantColor);
                                          collapsingToolbar.setStatusBarScrimColor(getResources().getColor(R.color.black_trans80));
                                          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                              getWindow().setStatusBarColor(
                                                      darkenColor(vibrantColor));
                                          }
                                      }
                                  });
                                  return false;
                              }
                          }
                ).submit());
    }

    /**
     * init the current views
     */
    public void init_views() {
        media_count = findViewById(R.id.media_count);
        message = findViewById(R.id.message);
        RecyclerView recyclerView = findViewById(R.id.playlist);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        songsAdapter = new ListMediaAdapter(this);
        recyclerView.setAdapter(songsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.playlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            case R.id.clear_songs:
                if(mediaList.size()==0)return true;
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(PlaylistActivity.this);
                builder.setTitle(R.string.clear_playlist_info);
                builder.setMessage(R.string.clear_playlist_message);
                String positiveText = getString(android.R.string.ok);
                builder.setPositiveButton(positiveText,
                        (dialog, which) -> {
                            // positive button logic
                            dataViewModel.deleteAllPlaylistMedia(playlist.getId());
                            dataViewModel.createPlaylist(playlist);
                        });

                String negativeText = getString(android.R.string.cancel);
                builder.setNegativeButton(negativeText,
                        (dialog, which) -> {
                            // negative button logic
                        });

                AlertDialog dialog = builder.create();
                // display dialog
                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.finish:
                finish();
                break;
        }
    }

    /**
     * check if media is already added to bookmarks
     * @param media
     * @return
     */
    @Override
    public boolean isBookmarked(Media media) {
        for (Bookmarks bookmarks: bookmarksList) {
            if(bookmarks.getId() == media.getId())
                return true;
        }
        return false;
    }

    /**
     * bookmark a media
     * @param media
     */
    @Override
    public void bookmark(Media media) {
        if(!isBookmarked(media)) {
            dataViewModel.bookmarkMedia(ObjectMapper.mapBoomarkFromMedia(media));
            Toast.makeText(this, R.string.media_bookmarked,Toast.LENGTH_SHORT).show();
        } else {
            dataViewModel.removeMediaFromBookmarks(media.getId());
            Toast.makeText(this, R.string.media_unbookmarked,Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * add or remove media from playlist
     * @param media
     */
    @Override
    public void addToPlaylist(Media media) { //remove from playlist, since it already exists
        //remove song from playlist
        dataViewModel.deletePlaylistMedia(media.getId(),playlist.getId());
        dataViewModel.createPlaylist(playlist);
    }

    @Override
    public boolean isPlaylistActivity() {
        return true;
    }

    @Override
    public boolean isDownloads() {
        return false;
    }

    /**
     * play selected media file
     * @param media
     * @param type
     */
    @Override
    public void OnItemClick(Media media, String type) {
        mediaOptions.play_media(dataViewModel,mediaList,media);
    }

    @Override
    public void OnOptionClick(Media media, View view) {
        mediaOptions.display(view,media);
    }
}
