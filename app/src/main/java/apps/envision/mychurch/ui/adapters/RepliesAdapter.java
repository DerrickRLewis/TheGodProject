package apps.envision.mychurch.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.interfaces.RepliesListener;
import apps.envision.mychurch.libs.RotateLoading;
import apps.envision.mychurch.pojo.Comments;
import apps.envision.mychurch.pojo.Info;
import apps.envision.mychurch.pojo.LoadMore;
import apps.envision.mychurch.pojo.Loader;
import apps.envision.mychurch.pojo.Replies;
import apps.envision.mychurch.ui.viewholders.RepliesViewHolder;
import apps.envision.mychurch.utils.ImageLoader;
import apps.envision.mychurch.utils.LetterTileProvider;


public class RepliesAdapter extends RecyclerView.Adapter< RecyclerView.ViewHolder>{

    private List<Object> items = new ArrayList<>();
    private final int VIEW_TYPE_COMMENTS = 0;
    private final int VIEW_TYPE_HEADER = 1;
    private final int VIEW_TYPE_INFO = 2;
    private final int VIEW_TYPE_LOAD_MORE = 3;
    private final int VIEW_TYPE_LOADER = 4;

    private RepliesListener repliesListener;

    public RepliesAdapter(RepliesListener repliesListener) {
        this.repliesListener = repliesListener;
    }

    /**
     * setadapter with comments arraylist
     * @param data
     */
   /* public void setAdapter(List<Comments> data) {
        removeLoader();
        Collections.reverse(data);
        items.addAll(data);
        this.notifyDataSetChanged();
    }*/

    /**
     * setadapter with more comments arraylist
     * @param data
     */
    public void setMoreAdapter(List<Replies> data) {
        Collections.reverse(data);
        items.addAll(1,data);
        this.notifyItemRangeInserted(1,data.size());
    }

    public void setNewComment(Replies comment) {
        if(items.get(1) instanceof Info){
            items.set(1, comment);
            this.notifyItemChanged(1);
        }else {
            items.add(comment);
            this.notifyItemInserted(items.size() - 1);
        }
    }

    public void setEditedComment(Replies comment, int position) {
        items.set(position, comment);
        this.notifyItemChanged(position);
    }

    public void deleteComment(int position) {
        items.remove(position);
        this.notifyItemRemoved(position);
    }

    public void setInfo(String msg){
        items.add(new Info(msg));
        this.notifyItemInserted(1);
    }

    public void setLoadMore(boolean replace){
        if(replace){
            items.set(1, new LoadMore());
            this.notifyItemChanged(1);
        }else {
            items.add(1, new LoadMore());
            this.notifyItemInserted(1);
        }
    }

    public void fetchLoading(){
        items.add(new Loader());
        this.notifyItemInserted(1);
    }

    public void setLoading(){
        items.set(1,new Loader());
        this.notifyItemChanged(1);
    }

    public void removeLoader(){
        items.remove(1);
        this.notifyItemRemoved(1);
    }

    /*
     * add info item
     */
    public void setHeader(Comments comments) {
        items = new ArrayList<>();
        items.add(comments);
        this.notifyItemInserted(items.size() - 1);
    }


    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        Object object = items.get(position);
        if(object instanceof Comments){
            return VIEW_TYPE_HEADER;
        }else if(object instanceof Info){
            return VIEW_TYPE_INFO;
        }else if(object instanceof Loader){
            return VIEW_TYPE_LOADER;
        }else if(object instanceof LoadMore){
            return VIEW_TYPE_LOAD_MORE;
        }else {
            return VIEW_TYPE_COMMENTS;
        }
    }

    /**
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
            case VIEW_TYPE_COMMENTS:
                View v = inflater.inflate(R.layout.replies_item, viewGroup, false);
                viewHolder = new RepliesViewHolder(v,repliesListener);
                viewHolder.itemView.setClickable(true);
                break;
            case VIEW_TYPE_HEADER:
                View vH = inflater.inflate(R.layout.comments_media_header, viewGroup, false);
                viewHolder = new HeaderViewHolder(vH);
                break;
            case VIEW_TYPE_INFO:
                View vI = inflater.inflate(R.layout.comment_info, viewGroup, false);
                viewHolder = new InfoViewHolder(vI);
                break;
            case VIEW_TYPE_LOADER:
                View vIoader = inflater.inflate(R.layout.loader, viewGroup, false);
                viewHolder = new LoadingViewHolder(vIoader);
                break;
            case VIEW_TYPE_LOAD_MORE:
                View vLoadMore = inflater.inflate(R.layout.load_more, viewGroup, false);
                viewHolder = new LoadMoreViewHolder(vLoadMore,repliesListener);
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
            case VIEW_TYPE_COMMENTS:
                final RepliesViewHolder viewHolder = (RepliesViewHolder) holder;
                final Replies ci = (Replies) items.get(i);
                viewHolder.bind(ci);
                break;
            case VIEW_TYPE_HEADER:
                final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                final Comments comments = (Comments) items.get(i);
                headerViewHolder.bind(comments);
                break;
            case VIEW_TYPE_INFO:
                final InfoViewHolder infoViewHolder = (InfoViewHolder) holder;
                final Info info = (Info) items.get(i);
                infoViewHolder.bind(info);
                break;
            case VIEW_TYPE_LOADER:
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
                loadingViewHolder.progressBar.start();
            case VIEW_TYPE_LOAD_MORE:
                //final LoadMoreViewHolder loadMoreViewHolder = (LoadMoreViewHolder) holder;
                break;
        }
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private ImageView thumbnail;
        private TextView title,description;
        private LetterTileProvider letterTileProvider;
        private HeaderViewHolder(View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            letterTileProvider = new LetterTileProvider(App.getContext());
        }

        private void bind(Comments comments){
            title.setText(comments.getName());
            description.setText(comments.getContent());
            if(comments.getAvatar().equalsIgnoreCase("")){
                thumbnail.setImageBitmap(letterTileProvider.getLetterTile(comments.getName()));
            }else{
                ImageLoader.loadUserAvatar(thumbnail, comments.getAvatar());
            }
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

    private static class LoadMoreViewHolder extends RecyclerView.ViewHolder {
        private ImageView load_more;
        private LoadMoreViewHolder(View itemView, RepliesListener repliesListener) {
            super(itemView);
            load_more = itemView.findViewById(R.id.load_more);
            load_more.setOnClickListener(v -> repliesListener.loadMore());
        }
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        private RotateLoading progressBar;
        private LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }

    }
}






