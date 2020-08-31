package apps.envision.mychurch.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.interfaces.MediaClickListener;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.ui.viewholders.MediaViewHolder;

public class GridMediaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


    ArrayList<Media> items = new ArrayList<>();
    private final int VIEW_TYPE_EMPTY = 0;
    private final int VIEW_TYPE_LIST = 1;
    private MediaClickListener mediaClickListener;
    private String type = "";

    public GridMediaAdapter(MediaClickListener mediaClickListener, String type) {
        this.mediaClickListener = mediaClickListener;
        this.type = type;
    }

    public void setAdapter(List<Media> data) {
        items = new ArrayList<>();
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
        Media fd = items.get(position);
        if(fd == null){
            return VIEW_TYPE_EMPTY;
        }else{
            return VIEW_TYPE_LIST;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int i) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_LIST:
                final Media ci = items.get(i);
                MediaViewHolder viewHolder = (MediaViewHolder) holder;
                viewHolder.setType(type);
                viewHolder.bind(ci);
                break;
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        switch (i) {
            case VIEW_TYPE_EMPTY:
                View v1 = inflater.inflate(R.layout.empty_data, viewGroup, false);
                viewHolder = new NoDatatHolder(v1);
                break;
            case VIEW_TYPE_LIST:
                View v;
                v = inflater.inflate(R.layout.media_item_grid, viewGroup, false);
                viewHolder = new MediaViewHolder(v,mediaClickListener);
                viewHolder.itemView.setClickable(true);
                break;
        }

        return viewHolder;
    }

    public class NoDatatHolder extends RecyclerView.ViewHolder {
        public TextView message;

        private NoDatatHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
            bind();
        }

        public void bind(){
           switch (type){
               case "video":
                   message.setText(App.getContext().getResources().getString(R.string.no_videos_found));
                   break;
               case "audio":
                   message.setText(App.getContext().getResources().getString(R.string.no_audios_found));
                   break;
               case "trending videos":
                   message.setText(App.getContext().getResources().getString(R.string.no_trending_videos));
                   break;
               case "trending audios":
                   message.setText(App.getContext().getResources().getString(R.string.no_trending_audios));
                   break;
               case "bookmarks":
                   message.setText(App.getContext().getResources().getString(R.string.no_bookmarked_media));
                   break;
           }
        }
    }
}






