package apps.envision.mychurch.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import apps.envision.mychurch.R;
import apps.envision.mychurch.interfaces.BibleClickListener;
import apps.envision.mychurch.interfaces.VersionsClickListener;
import apps.envision.mychurch.pojo.Bible;
import apps.envision.mychurch.pojo.BibleVersions;
import apps.envision.mychurch.pojo.Info;
import apps.envision.mychurch.ui.viewholders.BibleSearchViewHolder;
import apps.envision.mychurch.ui.viewholders.BibleViewHolder;
import apps.envision.mychurch.ui.viewholders.VersionsViewHolder;


public class BibleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


    private ArrayList<Object> bibleArrayList = new ArrayList<>();
    private final int VIEW_TYPE_BIBLE = 0;
    private final int VIEW_TYPE_SEARCH = 1;
    private final int VIEW_TYPE_INFO = 2;
    private BibleClickListener bibleClickListener;
    private boolean isSearch = false;
    private String query = "";

    public BibleAdapter(BibleClickListener bibleClickListener) {
        this.bibleClickListener = bibleClickListener;
    }

    public void setAdapter(List<Bible> biblists) {
        this.bibleArrayList = new ArrayList<>();
        this.bibleArrayList.addAll(biblists);
        this.notifyDataSetChanged();
    }

    public void setIsSearch(boolean isSearch){
        this.isSearch = isSearch;
    }

    public void setSearchQuery(String query){
        this.query = query;
    }

    /**
     * add info item
     */
    public void setInfo(String message) {
        bibleArrayList = new ArrayList<>();
        bibleArrayList.add(new Info(message));
        this.notifyDataSetChanged();
    }


    @Override
    public int getItemViewType(int position) {
        Object object = bibleArrayList.get(position);
        if(object instanceof Info){
            return VIEW_TYPE_INFO;
        }if(isSearch){
            return VIEW_TYPE_SEARCH;
        }else {
            return VIEW_TYPE_BIBLE;
        }
    }


    @Override
    public int getItemCount() {
        return bibleArrayList != null ? bibleArrayList.size() : 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int i) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_BIBLE:
                final BibleViewHolder viewHolder = (BibleViewHolder) holder;
                final Bible ci = (Bible) bibleArrayList.get(i);
                viewHolder.bind(ci);
                break;
            case VIEW_TYPE_SEARCH:
                final BibleSearchViewHolder bibleSearchViewHolder = (BibleSearchViewHolder) holder;
                final Bible bible = (Bible) bibleArrayList.get(i);
                bibleSearchViewHolder.bind(bible,query);
                break;
            case VIEW_TYPE_INFO:
                final InfoViewHolder infoViewHolder = (InfoViewHolder) holder;
                final Info info = (Info) bibleArrayList.get(i);
                infoViewHolder.bind(info);
                break;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        switch (i) {
            case VIEW_TYPE_BIBLE:
                View v = inflater.inflate(R.layout.bible_layout, viewGroup, false);
                viewHolder = new BibleViewHolder(v,bibleClickListener);
                viewHolder.itemView.setClickable(true);
                break;
            case VIEW_TYPE_SEARCH:
                View s = inflater.inflate(R.layout.bible_search_layout, viewGroup, false);
                viewHolder = new BibleSearchViewHolder(s,bibleClickListener);
                viewHolder.itemView.setClickable(true);
                break;
            case VIEW_TYPE_INFO:
                View vI = inflater.inflate(R.layout.bible_info, viewGroup, false);
                viewHolder = new InfoViewHolder(vI);
                break;
        }

        return viewHolder;
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






