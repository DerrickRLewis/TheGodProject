package apps.envision.mychurch.socials.adapters;

import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import apps.envision.mychurch.R;
import apps.envision.mychurch.interfaces.UserPostsListener;
import apps.envision.mychurch.libs.RotateLoading;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.Header;
import apps.envision.mychurch.pojo.Info;
import apps.envision.mychurch.pojo.LikeUpdate;
import apps.envision.mychurch.pojo.Loader;
import apps.envision.mychurch.pojo.RecyclerviewState;
import apps.envision.mychurch.pojo.UserPosts;
import apps.envision.mychurch.ui.viewholders.UserPostsViewHolder;

/*
* adapter class to display list of our searched songs from soundcloud
 */
public class UserPostAdapter extends RecyclerView.Adapter< RecyclerView.ViewHolder>{


    private List<Object> items = new ArrayList<>();
    private final int VIEW_TYPE_HYMNS = 0;
    private final int VIEW_TYPE_HEADER = 1;
    private final int VIEW_TYPE_INFO = 2;
    private final int VIEW_TYPE_LOADER = 3;

    private UserPostsListener userPostsListener;

    public UserPostAdapter(UserPostsListener userPostsListener) {
        this.userPostsListener = userPostsListener;
    }

    /**
     * setadapter with media arraylist
     * @param data
     */
    public void setAdapter(List<UserPosts> data) {
        items = new ArrayList<>();
        items.addAll(data);
        this.notifyDataSetChanged();
    }

    public void setMoreData(List<UserPosts> data) {
        items.addAll(data);
        this.notifyItemRangeInserted(items.size(), data.size());
        //this.notifyDataSetChanged();
    }

    public void updatePostCommentCount(int position, int count) {
        UserPosts userPosts = (UserPosts) items.get(position);
        if(userPosts!=null) {
            userPosts.setComments_count(count);
            this.notifyItemChanged(position);
        }
    }

    public void updatePostLikesCount(LikeUpdate likeUpdate) {
        UserPosts userPosts = (UserPosts) items.get(likeUpdate.position);
        if(userPosts!=null) {
            if(likeUpdate.option.equalsIgnoreCase("like")){
                userPosts.setLiked(true);
            }else{
                userPosts.setLiked(false);
            }
            userPosts.setLikes_count(likeUpdate.count);
            this.notifyItemChanged(likeUpdate.position);
        }
    }

    public void updatePostLikesCount(int position, int count) {
        UserPosts userPosts = (UserPosts) items.get(position);
        if(userPosts!=null) {
            userPosts.setLikes_count(count);
            this.notifyItemChanged(position);
        }
    }

    public void setEditedPost(UserPosts userPosts, int position) {
        items.set(position, userPosts);
        this.notifyItemChanged(position);
    }

    public void deletePost(int position) {
        items.remove(position);
        this.notifyItemRemoved(position);
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
                View v = inflater.inflate(R.layout.user_posts_list, viewGroup, false);
                viewHolder = new UserPostsViewHolder(v,userPostsListener);
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
                final UserPostsViewHolder viewHolder = (UserPostsViewHolder) holder;
                final UserPosts ci = (UserPosts) items.get(i);
                viewHolder.bind(ci,i);
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

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
      /*  int position = holder.getLayoutPosition();
        Log.e("recycledview", "position "+position);*/
        super.onViewRecycled(holder);
    }
}






