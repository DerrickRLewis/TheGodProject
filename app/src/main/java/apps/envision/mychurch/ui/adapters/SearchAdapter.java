package apps.envision.mychurch.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.libs.RotateLoading;
import apps.envision.mychurch.interfaces.MediaClickListener;
import apps.envision.mychurch.interfaces.OnLoadMoreListener;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.ui.viewholders.MediaViewHolder;
import apps.envision.mychurch.utils.NetworkUtil;

/*
* adapter class to display list of our searched media
 */
public class SearchAdapter extends RecyclerView.Adapter< RecyclerView.ViewHolder>{


    private List<Media> items = new ArrayList<>();
    private final int VIEW_TYPE_MEDIA = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading;
    private int visibleThreshold = 2;
    public int lastVisibleItem, totalItemCount;

    private MediaClickListener mediaClickListener;

    public SearchAdapter(RecyclerView mRecyclerView, MediaClickListener mediaClickListener) {
        this.mediaClickListener = mediaClickListener;
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && NetworkUtil.hasConnection(App.getContext())) {
                    if (totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        if (items.size() > 1 && mOnLoadMoreListener != null) {
                            mOnLoadMoreListener.onLoadMore();
                        }
                        isLoading = true;
                    }
                }
            }
        });
    }

    /**
     * setadapter with media arraylist
     * @param data
     */
    public void setMoreAdapter(List<Media> data) {
        items.addAll(data);
        //items.addAll((items.size()-1),data);
        this.notifyDataSetChanged();
    }

    /**
     * setadapter with media arraylist
     * @param data
     */
    public void setAdapter(List<Media> data) {
        items = new ArrayList<>();
        items.addAll(data);
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        Media fd = items.get(position);
       if(fd==null && position == (items.size()-1)){
            return VIEW_TYPE_LOADING;
        }else{
            return VIEW_TYPE_MEDIA;
        }
    }

    /**
     * add loader item
     */
    public void setLoader() {
        items.add(null);
        this.notifyItemInserted(items.size() - 1);
    }

    /**
     * remove loader
     */
    public void removeLoader() {
        items.remove(items.size() - 1);
        this.notifyItemRemoved(items.size());
    }

    /**
     * set onloadmore listener
     * @param mOnLoadMoreListener
     */
    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
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
            case VIEW_TYPE_MEDIA:
                View v = inflater.inflate(R.layout.media_item_list, viewGroup, false);
                viewHolder = new MediaViewHolder(v,mediaClickListener);
                viewHolder.itemView.setClickable(true);
                break;
            case VIEW_TYPE_LOADING:
                View vL = inflater.inflate(R.layout.loader, viewGroup, false);
                viewHolder = new LoadingViewHolder(vL);
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
            case VIEW_TYPE_LOADING:
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
                loadingViewHolder.progressBar.start();
                break;
            case VIEW_TYPE_MEDIA:
                final MediaViewHolder viewHolder = (MediaViewHolder) holder;
                final Media ci = items.get(i);
                viewHolder.bind(ci);
                break;
        }
    }


    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        private RotateLoading progressBar;
        private LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }

    }

    public void setLoaded() {
        isLoading = false;
    }
}






