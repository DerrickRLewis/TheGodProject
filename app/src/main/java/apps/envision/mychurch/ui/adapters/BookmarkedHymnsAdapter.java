package apps.envision.mychurch.ui.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.interfaces.HymnsClickListener;
import apps.envision.mychurch.libs.spans.Spanny;
import apps.envision.mychurch.pojo.Hymns;
import apps.envision.mychurch.pojo.SBible;
import apps.envision.mychurch.utils.ImageLoader;
import apps.envision.mychurch.utils.LetterTileProvider;
import apps.envision.mychurch.utils.Utility;

public class BookmarkedHymnsAdapter extends RecyclerView.Adapter<BookmarkedHymnsAdapter.MyViewHolder>
        implements Filterable {
    private Context context;
    private List<Hymns> bookmarkedList;
    private List<Hymns> filteredBookmarks;
    private HymnsClickListener listener;
    private LetterTileProvider letterTileProvider;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView content;
        private ImageView avatar, bookmark;
        private boolean isBookmarked = false;

        public MyViewHolder(View view) {
            super(view);
            title = itemView.findViewById(R.id.title);
            content = itemView.findViewById(R.id.content);
            avatar = itemView.findViewById(R.id.avatar);
            ImageView share = itemView.findViewById(R.id.share);
            ImageView copy = itemView.findViewById(R.id.copy);
            bookmark = itemView.findViewById(R.id.bookmark);
            LinearLayout holder_layout = itemView.findViewById(R.id.holder_layout);

            share.setOnClickListener(v -> listener.share_hymn(filteredBookmarks.get(getAdapterPosition())));
            copy.setOnClickListener(v -> listener.copy_hymn(filteredBookmarks.get(getAdapterPosition())));
            bookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   listener.bookmark(filteredBookmarks.get(getAdapterPosition()));
                   notifyItemRemoved(getAdapterPosition());
                }
            });
            holder_layout.setOnClickListener(v -> listener.open_hymn(filteredBookmarks.get(getAdapterPosition())));

        }
    }


    public BookmarkedHymnsAdapter(Context context, List<Hymns> bookmarkedList, HymnsClickListener listener) {
        if(bookmarkedList ==null) bookmarkedList = new ArrayList<>();
        this.context = context;
        this.bookmarkedList = bookmarkedList;
        this.filteredBookmarks = bookmarkedList;
        this.listener = listener;
        letterTileProvider = new LetterTileProvider(App.getContext());
    }

    public void setAdapterItems(List<Hymns> bookmarkedList){
        this.filteredBookmarks.clear();
        this.bookmarkedList.clear();
        this.bookmarkedList = bookmarkedList;
        this.filteredBookmarks = bookmarkedList;
        this.notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hymns_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Hymns hymns = filteredBookmarks.get(position);

        holder.avatar.setImageBitmap(letterTileProvider.getLetterTile(hymns.getTitle()));
        holder.title.setText(StringUtils.capitalize(hymns.getTitle()));
        holder.content.setText(StringUtils.capitalize(Utility.stripHtmlNotes(hymns.getContent())));
        set_bookmarked_view(holder.bookmark);
    }

    private void set_bookmarked_view(ImageView bookmark){
        Drawable mDrawable;
        mDrawable = ContextCompat.getDrawable(App.getContext(),R.drawable.bookmarks);
        mDrawable.setColorFilter(new
                PorterDuffColorFilter(App.getContext().getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN));
        bookmark.setImageDrawable(mDrawable);
    }

    @Override
    public int getItemCount() {
        return filteredBookmarks.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    filteredBookmarks = bookmarkedList;
                } else {
                    List<Hymns> filteredList = new ArrayList<>();
                    for (Hymns row : bookmarkedList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getTitle().toLowerCase().contains(charString.toLowerCase()) || row.getTitle().contains(charSequence)
                           || row.getContent().toLowerCase().contains(charString.toLowerCase()) || row.getContent().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    filteredBookmarks = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredBookmarks;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredBookmarks = (ArrayList<Hymns>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}