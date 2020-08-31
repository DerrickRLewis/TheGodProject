package apps.envision.mychurch.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.pojo.Playlist;

public class HorizontalPlaylistsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public List<Playlist> items = new ArrayList<>();
    private final int VIEW_TYPE_EMPTY = 0;
    private final int VIEW_TYPE_LIST = 1;
    private PlaylistListener playlistListener;

    public HorizontalPlaylistsAdapter(PlaylistListener playlistListener) {
        this.playlistListener = playlistListener;
    }


    public void setAdapter(List<Playlist> data) {
        items.clear();
        items.addAll(data);
        this.notifyDataSetChanged();
    }

    public void setEmptyAdapter() {
        items.clear();
        items.add(null);
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
            return VIEW_TYPE_EMPTY;
        }else{
            return VIEW_TYPE_LIST;
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (i) {
            case VIEW_TYPE_EMPTY:
                View v1 = inflater.inflate(R.layout.empty_data, parent, false);
                viewHolder = new NoPlaylistHolder(v1);
                break;
            case VIEW_TYPE_LIST:
                View v;
                v = inflater.inflate(R.layout.horizontal_playlist, parent, false);
                viewHolder = new PlaylistHolder(v);
                viewHolder.itemView.setClickable(true);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int i) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_LIST:
                final PlaylistHolder videoHolder = (PlaylistHolder) holder;
                final Playlist ci = items.get(i);
                videoHolder.title.setText(ci.getTitle().substring(0, 1).toUpperCase() + ci.getTitle().substring(1));
                videoHolder.holder.setOnClickListener(v -> playlistListener.playListOnClick(ci));
                playlistListener.setPlaylistSize(videoHolder.media,ci);
                playlistListener.setThumbnail(videoHolder.thumbnail,ci);
                break;
        }
    }

    public class PlaylistHolder extends RecyclerView.ViewHolder {
        private TextView title,media;
        private LinearLayout holder;
        private ImageView thumbnail;

        private PlaylistHolder(View itemView) {
            super(itemView);
            holder = itemView.findViewById(R.id.itm_holder);
            title = itemView.findViewById(R.id.title);
            media = itemView.findViewById(R.id.media);
            thumbnail = itemView.findViewById(R.id.thumbnail);
        }
    }

    public class NoPlaylistHolder extends RecyclerView.ViewHolder {
        public TextView message;

        private NoPlaylistHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
            bind();
        }

        public void bind(){
            message.setText(App.getContext().getResources().getString(R.string.no_playlist_created));
        }
    }

    public interface PlaylistListener {
        void playListOnClick(Playlist playlist);
        void setThumbnail(ImageView imageView, Playlist playlist);
        void setPlaylistSize(TextView textView, Playlist playlist);
    }
}





