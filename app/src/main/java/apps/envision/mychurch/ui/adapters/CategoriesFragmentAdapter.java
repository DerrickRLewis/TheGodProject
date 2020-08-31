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
import apps.envision.mychurch.interfaces.CategoriesClickListener;
import apps.envision.mychurch.pojo.Categories;
import apps.envision.mychurch.pojo.Header;
import apps.envision.mychurch.pojo.Info;
import apps.envision.mychurch.pojo.Loader;
import apps.envision.mychurch.ui.viewholders.CategoriesViewHolder;

public class CategoriesFragmentAdapter extends RecyclerView.Adapter< RecyclerView.ViewHolder>{


    private List<Object> items = new ArrayList<>();
    private final int VIEW_TYPE_CATEGORY = 0;
    private final int VIEW_TYPE_HEADER = 1;
    private final int VIEW_TYPE_INFO = 2;

    private CategoriesClickListener categoriesClickListener;

    public CategoriesFragmentAdapter(CategoriesClickListener categoriesClickListener) {
        this.categoriesClickListener = categoriesClickListener;
    }

    public int getSpanCount(int pos){
        if(pos == 1)return 1;
        if(items.get(pos) instanceof Header)return 2;
        if(items.get(pos) instanceof Loader) return 2;
        if(items.get(pos) instanceof Info) return 2;
        return 1;
    }

    /**
     * setadapter with media arraylist
     * @param data
     */
    public void setAdapter(List<Categories> data) {
        items = new ArrayList<>();
        Categories categories = new Categories();
        categories.setId(0);
        categories.setTitle(App.getContext().getString(R.string.all_messages));
        categories.setThumbnail("");
        categories.setMedia_count(0);
        items.add(categories);
        items.addAll(data);
        this.notifyDataSetChanged();
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
        }else {
            return VIEW_TYPE_CATEGORY;
        }
    }

    /**
     * add info item
     */
    public void setInfo(String message) {
        items = new ArrayList<>();
        Categories categories = new Categories();
        categories.setId(0);
        categories.setTitle(App.getContext().getString(R.string.all_messages));
        categories.setThumbnail("");
        categories.setMedia_count(0);
        items.add(categories);
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
            case VIEW_TYPE_CATEGORY:
                View v = inflater.inflate(R.layout.categories_list, viewGroup, false);
                viewHolder = new CategoriesViewHolder(v,categoriesClickListener);
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
            case VIEW_TYPE_CATEGORY:
                final CategoriesViewHolder viewHolder = (CategoriesViewHolder) holder;
                final Categories ci = (Categories) items.get(i);
                viewHolder.bind(ci);
                break;
            case VIEW_TYPE_HEADER:
                final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                final Header header = (Header)items.get(i);
                headerViewHolder.bind();
                break;
            case VIEW_TYPE_INFO:
                final InfoViewHolder infoViewHolder = (InfoViewHolder) holder;
                final Info info = (Info) items.get(i);
                infoViewHolder.bind(info);
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

        private void bind(){
            imageView.setImageDrawable(App.getContext().getDrawable(R.drawable.categories));
            textView.setText(App.getContext().getString(R.string.categories));
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
}






