package apps.envision.mychurch.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.libs.spans.Spanny;
import apps.envision.mychurch.pojo.SBible;

public class HighlightedVersesAdapter extends RecyclerView.Adapter<HighlightedVersesAdapter.MyViewHolder>
        implements Filterable {
    private Context context;
    private List<SBible> highlightedVerses;
    private List<SBible> highlightedFilteredVerses;
    private HighlightsListener listener;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView title,content,version;
        public MyViewHolder(View view) {
            super(view);
            title = itemView.findViewById(R.id.title);
            version = itemView.findViewById(R.id.version);
            content = itemView.findViewById(R.id.content);
            ImageView share = itemView.findViewById(R.id.share);
            ImageView copy = itemView.findViewById(R.id.copy);
            ImageView delete = itemView.findViewById(R.id.delete);
            share.setOnClickListener(v -> listener.share_highlight(highlightedFilteredVerses.get(getAdapterPosition())));
            copy.setOnClickListener(v -> listener.copy_highlight(highlightedFilteredVerses.get(getAdapterPosition())));
            delete.setOnClickListener(v -> listener.delete_highlight(highlightedFilteredVerses.get(getAdapterPosition()),getAdapterPosition()));
        }
    }


    public HighlightedVersesAdapter(Context context, List<SBible> highlightedVerses, HighlightsListener listener) {
        if(highlightedVerses==null)highlightedVerses = new ArrayList<>();
        this.context = context;
        this.highlightedVerses = highlightedVerses;
        this.highlightedFilteredVerses = highlightedVerses;
        this.listener = listener;
    }

    public void setAdapterItems(List<SBible> highlightedVerses){
        this.highlightedFilteredVerses.clear();
        this.highlightedVerses.clear();
        this.highlightedVerses = highlightedVerses;
        this.highlightedFilteredVerses = highlightedVerses;
        this.notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.highlighted_bible_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final SBible sBible = highlightedFilteredVerses.get(position);
        Spanny spanny = new Spanny(String.valueOf(sBible.getBook() +" "+ sBible.getChapter())+":"+sBible.getVerse(), new StyleSpan(Typeface.BOLD));
        holder.title.setText(spanny);
        holder.version.setText(sBible.getVersion());

        Spanny spanny2 = new Spanny(sBible.getContent(), new BackgroundColorSpan(sBible.getColorCode()));
        holder.content.setText(spanny2);
    }

    @Override
    public int getItemCount() {
        return highlightedFilteredVerses.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    highlightedFilteredVerses = highlightedVerses;
                } else {
                    List<SBible> filteredList = new ArrayList<>();
                    for (SBible row : highlightedVerses) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getContent().toLowerCase().contains(charString.toLowerCase()) || row.getBook().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    highlightedFilteredVerses = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = highlightedFilteredVerses;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                highlightedFilteredVerses = (ArrayList<SBible>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface HighlightsListener {
       void share_highlight(SBible sBible);
       void copy_highlight(SBible sBible);
       void delete_highlight(SBible sBible, int position);
    }
}