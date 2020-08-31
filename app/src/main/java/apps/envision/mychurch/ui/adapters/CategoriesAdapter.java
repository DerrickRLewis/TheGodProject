package apps.envision.mychurch.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.interfaces.CategoriesClickListener;
import apps.envision.mychurch.pojo.Categories;
import apps.envision.mychurch.ui.viewholders.CategoriesViewHolder;

public class CategoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


    private ArrayList<Categories> items = new ArrayList<>();
    private final int VIEW_TYPE_EMPTY = 0;
    private final int VIEW_TYPE_LIST = 1;
    private CategoriesClickListener categoriesClickListener;

    public CategoriesAdapter(CategoriesClickListener categoriesClickListener) {
        this.categoriesClickListener = categoriesClickListener;
    }

    public void setAdapter(List<Categories> data) {
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
        Categories fd = items.get(position);
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
                final Categories ci = items.get(i);
                CategoriesViewHolder viewHolder = (CategoriesViewHolder) holder;
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
                viewHolder = new NoDataHolder(v1);
                break;
            case VIEW_TYPE_LIST:
                View v;
                v = inflater.inflate(R.layout.categories_grid, viewGroup, false);
                viewHolder = new CategoriesViewHolder(v,categoriesClickListener);
                viewHolder.itemView.setClickable(true);
                break;
        }

        return viewHolder;
    }

    public class NoDataHolder extends RecyclerView.ViewHolder {
        public TextView message;

        private NoDataHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
            bind();
        }

        public void bind(){
            message.setText(App.getContext().getResources().getString(R.string.no_data
            ));
        }
    }
}






