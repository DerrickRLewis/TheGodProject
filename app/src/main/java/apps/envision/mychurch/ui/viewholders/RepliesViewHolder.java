package apps.envision.mychurch.ui.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.interfaces.RepliesListener;
import apps.envision.mychurch.pojo.Replies;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.ui.fonts.OpenSansRegularTextView;
import apps.envision.mychurch.utils.ImageLoader;
import apps.envision.mychurch.utils.LetterTileProvider;
import apps.envision.mychurch.utils.TimUtil;


public class RepliesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView fullname,time,edited;
    private OpenSansRegularTextView content;
    private ImageView avatar,edit,delete,report;
    private RepliesListener repliesListener;
    private LetterTileProvider letterTileProvider;
    private Replies comments;

    public RepliesViewHolder(View itemView, RepliesListener repliesListener) {
        super(itemView);
        letterTileProvider = new LetterTileProvider(App.getContext());
        fullname = itemView.findViewById(R.id.fullname);
        content = itemView.findViewById(R.id.content);
        avatar = itemView.findViewById(R.id.avatar);
        time = itemView.findViewById(R.id.time);
        edited = itemView.findViewById(R.id.edited);
        edit = itemView.findViewById(R.id.edit);
        delete = itemView.findViewById(R.id.delete);
        report = itemView.findViewById(R.id.report);

        this.repliesListener = repliesListener;
        edit.setOnClickListener(this);
        delete.setOnClickListener(this);
        report.setOnClickListener(this);
        fullname.setOnClickListener(this);
    }

    public void bind(Replies comments){
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
        if(repliesListener.isUserComment(comments.getEmail())){
            edit.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
        }else{
            edit.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
        }

        if(!repliesListener.isUserComment(comments.getEmail()) && PreferenceSettings.isUserLoggedIn()){
            report.setVisibility(View.VISIBLE);
        }else{
            report.setVisibility(View.GONE);
        }
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.edit:
                repliesListener.editComment(comments,this.getAdapterPosition());
                break;
            case R.id.delete:
                repliesListener.deleteComment(comments.getId(),this.getAdapterPosition());
                break;
            case R.id.report:
                repliesListener.reportComment(comments,this.getAdapterPosition());
                break;
            case R.id.fullname:
                if(PreferenceSettings.isUserLoggedIn() && PreferenceSettings.getUserData()!=null) {
                    UserData userData = new UserData();
                    userData.setEmail(comments.getEmail());
                    userData.setName(comments.getName());
                    userData.setAvatar(comments.getAvatar());
                    userData.setCover_photo(comments.getCover_photo());
                    repliesListener.view_profile(userData);
                }
                break;
        }
    }
}
