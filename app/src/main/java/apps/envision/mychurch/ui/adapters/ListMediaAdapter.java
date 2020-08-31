package apps.envision.mychurch.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import apps.envision.mychurch.R;
import apps.envision.mychurch.interfaces.MediaClickListener;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.ui.viewholders.MediaViewHolder;


public class ListMediaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


    private ArrayList<Media> mediaList = new ArrayList<>();
    private MediaClickListener mediaClickListener;

    public ListMediaAdapter(MediaClickListener mediaClickListener) {
        this.mediaClickListener = mediaClickListener;
    }

    public void setAdapter(List<Media> mediaList) {
        this.mediaList = new ArrayList<>();
        this.mediaList.addAll(mediaList);
        this.notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return mediaList != null ? mediaList.size() : 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int i) {
        final Media media = mediaList.get(i);
        MediaViewHolder viewHolder = (MediaViewHolder) holder;
        viewHolder.bind(media);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.media_item_list, viewGroup, false);
        final MediaViewHolder viewHolder = new MediaViewHolder(v,mediaClickListener);
        return viewHolder;
    }
}






