package apps.envision.mychurch.ui.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.interfaces.CommentsListener;
import apps.envision.mychurch.pojo.Comments;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.ui.fonts.OpenSansRegularTextView;
import apps.envision.mychurch.utils.ImageLoader;
import apps.envision.mychurch.utils.LetterTileProvider;
import apps.envision.mychurch.utils.TimUtil;
import apps.envision.mychurch.utils.Utility;


public class CommentsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView fullname;
    private OpenSansRegularTextView content;
    private TextView time;
    private TextView edited;
    private TextView replies;
    private ImageView avatar,edit,delete,report;
    private CommentsListener commentsListener;
    private LetterTileProvider letterTileProvider;
    private Comments comments;

    public CommentsViewHolder(View itemView, CommentsListener commentsListener) {
        super(itemView);
        letterTileProvider = new LetterTileProvider(App.getContext());
        fullname = itemView.findViewById(R.id.fullname);
        content = itemView.findViewById(R.id.content);
        avatar = itemView.findViewById(R.id.avatar);
        time = itemView.findViewById(R.id.time);
        edited = itemView.findViewById(R.id.edited);
        edit = itemView.findViewById(R.id.edit);
        delete = itemView.findViewById(R.id.delete);
        replies = itemView.findViewById(R.id.replies);
        TextView reply = itemView.findViewById(R.id.reply);
        report = itemView.findViewById(R.id.report);

        this.commentsListener = commentsListener;
        edit.setOnClickListener(this);
        delete.setOnClickListener(this);
        replies.setOnClickListener(this);
        reply.setOnClickListener(this);
        report.setOnClickListener(this);
        fullname.setOnClickListener(this);
    }

    public void bind(Comments comments){
        this.comments = comments;
        if(comments.getAvatar().equalsIgnoreCase("")){
            avatar.setImageBitmap(letterTileProvider.getLetterTile(comments.getName()));
        }else{
            ImageLoader.loadUserAvatar(avatar, comments.getAvatar());
        }
        fullname.setText(comments.getName());
        content.setContent(comments.getContent());
        time.setText(TimUtil.timeAgo(comments.getDate()));
        if(comments.getEdited()==0){
            edited.setVisibility(View.VISIBLE);
        }else{
            edited.setVisibility(View.GONE);
        }
        if(commentsListener.isUserComment(comments.getEmail())){
            edit.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
        }else{
            edit.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
        }

        if(comments.getReplies()>0){
            replies.setVisibility(View.VISIBLE);
            replies.setText(Utility.reduceCountToString(comments.getReplies()) + App.getContext().getString(R.string.replies));
        }else{
            replies.setVisibility(View.GONE);
        }

        if(!commentsListener.isUserComment(comments.getEmail()) && PreferenceSettings.isUserLoggedIn()){
            report.setVisibility(View.VISIBLE);
        }else{
            report.setVisibility(View.GONE);
        }
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.edit:
                commentsListener.editComment(comments,this.getAdapterPosition());
                break;
            case R.id.delete:
                commentsListener.deleteComment(comments.getId(),this.getAdapterPosition());
                break;
            case R.id.replies:case R.id.reply:
                commentsListener.viewReplies(comments,this.getAdapterPosition());
                break;
            case R.id.report:
                commentsListener.reportComment(comments,this.getAdapterPosition());
                break;
            case R.id.fullname:
                if(PreferenceSettings.isUserLoggedIn() && PreferenceSettings.getUserData()!=null) {
                    UserData userData = new UserData();
                    userData.setEmail(comments.getEmail());
                    userData.setName(comments.getName());
                    userData.setAvatar(comments.getAvatar());
                    userData.setCover_photo(comments.getCover_photo());
                    commentsListener.view_profile(userData);
                }
                break;
        }
    }
}
