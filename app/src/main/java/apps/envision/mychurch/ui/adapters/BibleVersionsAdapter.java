package apps.envision.mychurch.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import apps.envision.mychurch.R;
import apps.envision.mychurch.interfaces.VersionsClickListener;
import apps.envision.mychurch.pojo.BibleVersions;
import apps.envision.mychurch.ui.viewholders.VersionsViewHolder;


public class BibleVersionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


    private ArrayList<BibleVersions> bibleVersions = new ArrayList<>();
    private VersionsClickListener versionsClickListener;

    public BibleVersionsAdapter(VersionsClickListener versionsClickListener) {
        this.versionsClickListener = versionsClickListener;
    }

    public void setAdapter(List<BibleVersions> eventsLists) {
        this.bibleVersions = new ArrayList<>();
        this.bibleVersions.addAll(eventsLists);
        this.notifyDataSetChanged();
    }

    public void updateAdapter(String book){
        int pos = -1;
        for(int i=0; i<bibleVersions.size(); i++){
            if(bibleVersions.get(i).getBook().equalsIgnoreCase(book)){
                pos = i;
            }
        }
        if(pos!=-1){
            this.notifyItemChanged(pos);
        }
    }


    @Override
    public int getItemCount() {
        return bibleVersions != null ? bibleVersions.size() : 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int i) {
        final BibleVersions versions = bibleVersions.get(i);
        VersionsViewHolder viewHolder = (VersionsViewHolder) holder;
        viewHolder.bind(versions);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bible_versions, viewGroup, false);
        final VersionsViewHolder viewHolder = new VersionsViewHolder(v,versionsClickListener);
        return viewHolder;
    }
}






