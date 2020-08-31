package apps.envision.mychurch.socials.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.pojo.Uploads;
import apps.envision.mychurch.utils.Constants;
import apps.envision.mychurch.utils.FileManager;
import apps.envision.mychurch.utils.ImageLoader;

public class UploadsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public List<Uploads> items = new ArrayList<>();
    private final int VIEW_TYPE_EMPTY = 0;
    private final int VIEW_TYPE_LIST = 1;
    private UploadsListener uploadsListener;

    public UploadsAdapter(UploadsListener uploadsListener) {
        this.uploadsListener = uploadsListener;
    }


    public void setAdapter(List<Uploads> data) {
        items.clear();
        items.addAll(data);
        this.notifyDataSetChanged();
    }

    public void setItems(List<Uploads> data) {
        items.clear();
        items.addAll(data);
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
        Uploads fd = items.get(position);
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
                v = inflater.inflate(R.layout.upload_item, parent, false);
                viewHolder = new UploadsHolder(v);
                viewHolder.itemView.setClickable(true);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int i) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_LIST:
                final UploadsHolder uploadsHolder = (UploadsHolder) holder;
                final Uploads ci = items.get(i);
                ImageLoader.loadUploadImage(uploadsHolder.thumbnail,ci.getFile_path());
                double filesize = ci.getLength()/(1024 * 1024);
                if(filesize> Constants.Uploads.maximum_single_file_size){
                    uploadsHolder.iv_error.setVisibility(View.VISIBLE);
                }else{
                    uploadsHolder.iv_error.setVisibility(View.GONE);
                }
                uploadsHolder.file_size.setText(FileManager.getFileSize(ci.getLength()));
                uploadsHolder.iv_clear.setOnClickListener(v -> uploadsListener.removeItem(i));
                uploadsHolder.iv_crop.setOnClickListener(v -> uploadsListener.cropItem(i));
                if(ci.getFile_type().equalsIgnoreCase("image")){
                    uploadsHolder.iv_crop.setVisibility(View.VISIBLE);
                }else{
                    uploadsHolder.iv_crop.setVisibility(View.GONE);
                }
                break;
        }
    }

    public class UploadsHolder extends RecyclerView.ViewHolder {
        private ImageView thumbnail,iv_clear,iv_crop,iv_error;
        private TextView file_size;

        private UploadsHolder(View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            iv_clear = itemView.findViewById(R.id.iv_clear);
            iv_crop = itemView.findViewById(R.id.iv_crop);
            iv_error = itemView.findViewById(R.id.iv_error);
            file_size = itemView.findViewById(R.id.file_size);
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

    public interface UploadsListener {
        void removeItem(int position);
        void cropItem(int position);
    }
}





