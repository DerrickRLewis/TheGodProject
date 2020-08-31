package apps.envision.mychurch.ui.viewholders;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.interfaces.HymnsClickListener;
import apps.envision.mychurch.interfaces.NotesListener;
import apps.envision.mychurch.pojo.Hymns;
import apps.envision.mychurch.pojo.Note;
import apps.envision.mychurch.utils.LetterTileProvider;
import apps.envision.mychurch.utils.TimUtil;
import apps.envision.mychurch.utils.Utility;


public class HymnsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView title;
    private TextView content;
    private ImageView avatar, bookmark;
    private HymnsClickListener hymnsClickListener;
    private LetterTileProvider letterTileProvider;
    private Hymns hymns;
    private boolean isBookmarked = false;

    public HymnsViewHolder(View itemView, HymnsClickListener hymnsClickListener) {
        super(itemView);
        letterTileProvider = new LetterTileProvider(App.getContext());
        title = itemView.findViewById(R.id.title);
        content = itemView.findViewById(R.id.content);
        avatar = itemView.findViewById(R.id.avatar);
        ImageView share = itemView.findViewById(R.id.share);
        ImageView copy = itemView.findViewById(R.id.copy);
        bookmark = itemView.findViewById(R.id.bookmark);
        LinearLayout holder_layout = itemView.findViewById(R.id.holder_layout);

        this.hymnsClickListener = hymnsClickListener;
        share.setOnClickListener(this);
        copy.setOnClickListener(this);
        bookmark.setOnClickListener(this);
        holder_layout.setOnClickListener(this);
    }

    public void bind(Hymns hymns){
        this.hymns = hymns;
        avatar.setImageBitmap(letterTileProvider.getLetterTile(hymns.getTitle()));
        title.setText(StringUtils.capitalize(hymns.getTitle()));
        content.setText(StringUtils.capitalize(Utility.stripHtmlNotes(hymns.getContent())));
        set_bookmarked_view(hymnsClickListener.isBookmarked(hymns));
    }

    private void set_bookmarked_view(boolean isBookmarked){
        this.isBookmarked = isBookmarked;
        Drawable mDrawable;
        if(!isBookmarked){
            mDrawable = ContextCompat.getDrawable(App.getContext(),R.drawable.bookmarks);
            mDrawable.setColorFilter(new
                    PorterDuffColorFilter(App.getContext().getResources().getColor(R.color.material_grey_700), PorterDuff.Mode.SRC_IN));
        }else{
            mDrawable = ContextCompat.getDrawable(App.getContext(),R.drawable.bookmarks);
            mDrawable.setColorFilter(new
                    PorterDuffColorFilter(App.getContext().getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN));
        }
        bookmark.setImageDrawable(mDrawable);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.share:
                hymnsClickListener.share_hymn(hymns);
                break;
            case R.id.copy:
                hymnsClickListener.copy_hymn(hymns);
                break;
            case R.id.holder_layout:
                hymnsClickListener.open_hymn(hymns);
                break;
            case R.id.bookmark:
                hymnsClickListener.bookmark(hymns);
                if(isBookmarked){
                    set_bookmarked_view(false);
                }else{
                    set_bookmarked_view(true);
                }
                break;
        }
    }
}
