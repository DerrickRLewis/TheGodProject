package apps.envision.mychurch.ui.adapters;

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
import apps.envision.mychurch.interfaces.HymnsClickListener;
import apps.envision.mychurch.interfaces.MediaClickListener;
import apps.envision.mychurch.libs.RotateLoading;
import apps.envision.mychurch.pojo.Header;
import apps.envision.mychurch.pojo.Hymns;
import apps.envision.mychurch.pojo.Info;
import apps.envision.mychurch.pojo.Loader;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.ui.viewholders.HymnsViewHolder;
import apps.envision.mychurch.ui.viewholders.MediaViewHolder;

/*
* adapter class to display list of our searched songs from soundcloud
 */
public class HymnsAdapter extends RecyclerView.Adapter< RecyclerView.ViewHolder>{


    private List<Object> items = new ArrayList<>();
    private final int VIEW_TYPE_HYMNS = 0;
    private final int VIEW_TYPE_HEADER = 1;
    private final int VIEW_TYPE_INFO = 2;
    private final int VIEW_TYPE_LOADER = 3;

    private HymnsClickListener hymnsClickListener;

    public HymnsAdapter(HymnsClickListener hymnsClickListener) {
        this.hymnsClickListener = hymnsClickListener;
    }

    /**
     * setadapter with media arraylist
     * @param data
     */
    public void setAdapter(List<Hymns> data) {
        items = new ArrayList<>();
        items.addAll(data);
        this.notifyDataSetChanged();
    }

    public void setMoreData(List<Hymns> data) {
        items.addAll(data);
        this.notifyItemRangeInserted(items.size(), data.size());
        //this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        Object object = items.get(position);
        if(object instanceof Header){
            return VIEW_TYPE_HEADER;
        }else if(object instanceof Info){
            return VIEW_TYPE_INFO;
        }else if(object instanceof Loader){
            return VIEW_TYPE_LOADER;
        }else {
            return VIEW_TYPE_HYMNS;
        }
    }

    /**
     * add info item
     */
    public void setInfo(String message) {
        items = new ArrayList<>();
        items.add(new Info(message));
        this.notifyDataSetChanged();
        this.notifyItemInserted(items.size() - 1);
    }

    /**
     * create view holders
     * @param viewGroup
     * @param i
     * @return
     */
    @Override
    public  RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        switch (i) {
            case VIEW_TYPE_HYMNS:
                View v = inflater.inflate(R.layout.hymns_item, viewGroup, false);
                viewHolder = new HymnsViewHolder(v,hymnsClickListener);
                viewHolder.itemView.setClickable(true);
                break;
            case VIEW_TYPE_HEADER:
                View vH = inflater.inflate(R.layout.media_header, viewGroup, false);
                viewHolder = new HeaderViewHolder(vH);
                break;
            case VIEW_TYPE_INFO:
                View vI = inflater.inflate(R.layout.info, viewGroup, false);
                viewHolder = new InfoViewHolder(vI);
                break;
            case VIEW_TYPE_LOADER:
                View ld = inflater.inflate(R.layout.loader, viewGroup, false);
                viewHolder = new ViewLoader(ld);
                break;
        }

        return viewHolder;
    }

    /**
     * bind viewholder with data
     * @param holder
     * @param i
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_HYMNS:
                final HymnsViewHolder viewHolder = (HymnsViewHolder) holder;
                final Hymns ci = (Hymns)items.get(i);
                viewHolder.bind(ci);
                break;
            case VIEW_TYPE_HEADER:
                final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                final Header header = (Header)items.get(i);
                headerViewHolder.bind(header.media_type);
                break;
            case VIEW_TYPE_INFO:
                final InfoViewHolder infoViewHolder = (InfoViewHolder) holder;
                final Info info = (Info) items.get(i);
                infoViewHolder.bind(info);
                break;
            case VIEW_TYPE_LOADER:
                final ViewLoader viewLoader = (ViewLoader) holder;
                viewLoader.rotateLoading.start();
                break;
        }
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textView;
        private HeaderViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.icon);
            textView = itemView.findViewById(R.id.text);
        }

        private void bind(String media_type){

        }

    }

    private static class InfoViewHolder extends RecyclerView.ViewHolder {
        private TextView message;
        private InfoViewHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
        }

        private void bind(Info info){
            message.setText(info.message);
        }

    }

    public class ViewLoader extends RecyclerView.ViewHolder {
        private RotateLoading rotateLoading;

        ViewLoader(View view) {
            super(view);
            rotateLoading = (RotateLoading) view.findViewById(R.id.progressBar);
        }
    }

    public void setLoaded(){
        int position = items.size() - 1;
        if (items.get(position) instanceof Loader) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void setLoader(){
        items.add(new Loader());
        this.notifyItemInserted(items.size()-1);
    }
}






