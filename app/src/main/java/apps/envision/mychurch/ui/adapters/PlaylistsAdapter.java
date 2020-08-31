package apps.envision.mychurch.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.interfaces.PlaylistListener;
import apps.envision.mychurch.pojo.Playlist;

public class PlaylistsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public List<Playlist> items = new ArrayList<>();
    private final int VIEW_TYPE_NEW = 0;
    private final int VIEW_TYPE_LIST = 1;
    private PlaylistListener playlistListener;
    private String media_type = "";

    public PlaylistsAdapter(PlaylistListener playlistListener, String media_type) {
        this.playlistListener = playlistListener;
        this.media_type = media_type;
    }


    public void setAdapter(List<Playlist> data) {
        items.clear();
        items.addAll(data);
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        Playlist fd = items.get(position);
        if(fd == null){
            return VIEW_TYPE_NEW;
        }else{
            return VIEW_TYPE_LIST;
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (i) {
            case VIEW_TYPE_NEW:
                View v1 = inflater.inflate(R.layout.add_playlist, parent, false);
                viewHolder = new CreatePlaylistHolder(v1);
                break;
            case VIEW_TYPE_LIST:
                View v;
                v = inflater.inflate(R.layout.playlist_list, parent, false);
                viewHolder = new PlaylistHolder(v);
                viewHolder.itemView.setClickable(true);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int i) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_NEW:
                CreatePlaylistHolder createPlaylistHolder = (CreatePlaylistHolder) holder;
                createPlaylistHolder.bind();
                createPlaylistHolder.holder.setOnClickListener(view -> playlistListener.new_playlist());
                break;
            case VIEW_TYPE_LIST:
                final PlaylistHolder videoHolder = (PlaylistHolder) holder;
                final Playlist ci = items.get(i);
                videoHolder.title.setText(ci.getTitle().substring(0, 1).toUpperCase() + ci.getTitle().substring(1));
                videoHolder.holder.setOnClickListener(v -> playlistListener.playListOnClick(ci));
                if(playlistListener.isMediaAddedTOPlaylist(ci)){
                    videoHolder.checkBox.setVisibility(View.VISIBLE);
                    videoHolder.checkBox.setChecked(true);
                }else{
                    videoHolder.checkBox.setChecked(false);
                    videoHolder.checkBox.setVisibility(View.GONE);
                }

                if(playlistListener.showOptions()){
                    videoHolder.options.setVisibility(View.VISIBLE);
                    videoHolder.options.setOnClickListener(v -> playlistListener.onOptionsClick(ci,videoHolder.options));
                }else{
                    videoHolder.options.setVisibility(View.GONE);
                }
                playlistListener.setPlaylistSize(videoHolder.media,ci);
                playlistListener.setThumbnail(videoHolder.thumbnail,ci);

                break;
        }
    }

    public class PlaylistHolder extends RecyclerView.ViewHolder {
        private TextView title,media;
        private LinearLayout holder;
        private ImageView thumbnail,options;
        private CheckBox checkBox;

        private PlaylistHolder(View itemView) {
            super(itemView);
            holder = itemView.findViewById(R.id.itm_holder);
            title = itemView.findViewById(R.id.title);
            media = itemView.findViewById(R.id.media);
            checkBox = itemView.findViewById(R.id.myCheckbox);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            options = itemView.findViewById(R.id.options);
        }
    }

    public class CreatePlaylistHolder extends RecyclerView.ViewHolder {
        public LinearLayout holder;
        public TextView title;

        private CreatePlaylistHolder(View itemView) {
            super(itemView);
            holder = itemView.findViewById(R.id.itm_holder);
            title = itemView.findViewById(R.id.title);
        }

        private void bind(){
            if(media_type.equalsIgnoreCase(App.getContext().getString(R.string.audio))){
                title.setText(App.getContext().getString(R.string.new_audio_playlist));
            }else{
                title.setText(App.getContext().getString(R.string.new_video_playlist));
            }
        }
    }
}





