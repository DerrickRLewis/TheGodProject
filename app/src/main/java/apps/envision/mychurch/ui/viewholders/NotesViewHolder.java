package apps.envision.mychurch.ui.viewholders;

import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.interfaces.NotesListener;
import apps.envision.mychurch.libs.htmltextview.HtmlFormatter;
import apps.envision.mychurch.libs.htmltextview.HtmlFormatterBuilder;
import apps.envision.mychurch.pojo.Comments;
import apps.envision.mychurch.pojo.Note;
import apps.envision.mychurch.utils.LetterTileProvider;
import apps.envision.mychurch.utils.TimUtil;
import apps.envision.mychurch.utils.Utility;


public class NotesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView title;
    private TextView content;
    private TextView time;
    private ImageView avatar;
    private NotesListener notesListener;
    private LetterTileProvider letterTileProvider;
    private Note note;

    public NotesViewHolder(View itemView, NotesListener notesListener) {
        super(itemView);
        letterTileProvider = new LetterTileProvider(App.getContext());
        title = itemView.findViewById(R.id.title);
        content = itemView.findViewById(R.id.content);
        avatar = itemView.findViewById(R.id.avatar);
        time = itemView.findViewById(R.id.time);
        ImageView edit = itemView.findViewById(R.id.edit);
        ImageView share = itemView.findViewById(R.id.share);
        ImageView copy = itemView.findViewById(R.id.copy);
        ImageView delete = itemView.findViewById(R.id.delete);
        LinearLayout holder_layout = itemView.findViewById(R.id.holder_layout);

        this.notesListener = notesListener;
        edit.setOnClickListener(this);
        share.setOnClickListener(this);
        copy.setOnClickListener(this);
        delete.setOnClickListener(this);
        holder_layout.setOnClickListener(this);
    }

    public void bind(Note note){
        this.note = note;
        avatar.setImageBitmap(letterTileProvider.getLetterTile(note.getTitle()));
        title.setText(StringUtils.capitalize(note.getTitle()));
        //content.setText(note.getContent());
        time.setText(TimUtil.noteTimeAgo(note.getTime()));
       // Spanned formattedHtml = HtmlFormatter.formatHtml(new HtmlFormatterBuilder().setHtml(note.getContent()));
        content.setText(StringUtils.capitalize(Utility.stripHtmlNotes(note.getContent())));
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.edit:
                notesListener.edit_note(note);
                break;
            case R.id.share:
                notesListener.share_note(note);
                break;
            case R.id.copy:
                notesListener.copy_note(note);
                break;
            case R.id.delete:
                notesListener.delete_note(note);
                break;
            case R.id.holder_layout:
                notesListener.open_note(note);
                break;
        }
    }
}
