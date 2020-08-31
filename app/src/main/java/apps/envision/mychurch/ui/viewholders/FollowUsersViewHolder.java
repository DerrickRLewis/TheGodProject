package apps.envision.mychurch.ui.viewholders;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.interfaces.HymnsClickListener;
import apps.envision.mychurch.interfaces.UserProfileListener;
import apps.envision.mychurch.libs.RotateLoading;
import apps.envision.mychurch.pojo.Hymns;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.utils.ImageLoader;
import apps.envision.mychurch.utils.LetterTileProvider;
import apps.envision.mychurch.utils.Utility;


public class FollowUsersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView name;
    private TextView location;
    private ImageView avatar;
    private UserProfileListener userProfileListener;
    private UserData userData;
    private RotateLoading rotateLoading;
    private TextView follow;
    private int position = -1;

    public FollowUsersViewHolder(View itemView, UserProfileListener userProfileListener) {
        super(itemView);
        name = itemView.findViewById(R.id.name);
        location = itemView.findViewById(R.id.location);
        avatar = itemView.findViewById(R.id.avatar);
        rotateLoading = itemView.findViewById(R.id.rotateLoading);
        follow = itemView.findViewById(R.id.follow_unfollow);
        this.userProfileListener = userProfileListener;

        name.setOnClickListener(this);
        follow.setOnClickListener(this);
    }

    public void bind(UserData userData, int position){
        this.userData = userData;
        this.position = position;
        name.setText(userData.getName());
        location.setText(userData.getLocation());
        ImageLoader.loadUserAvatar(avatar,userData.getAvatar());
        if(userData.isFollowing()){
            follow.setText(App.getContext().getString(R.string.unfollow));
            follow.setTextColor(App.getContext().getResources().getColor(R.color.primary_dark));
        }else{
            follow.setText(App.getContext().getString(R.string.follow));
            follow.setTextColor(App.getContext().getResources().getColor(R.color.black));
        }
        rotateLoading.setVisibility(View.GONE);
        follow.setVisibility(View.VISIBLE);

        UserData thisUser = PreferenceSettings.getUserData();
        if(thisUser!=null && userData.getEmail().equalsIgnoreCase(thisUser.getEmail())){
            follow.setVisibility(View.GONE);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.follow_unfollow:
                follow.setVisibility(View.GONE);
                rotateLoading.setVisibility(View.VISIBLE);
                rotateLoading.start();
                userProfileListener.follow_unfollow(userData,position);
                break;
            case R.id.name:
                userProfileListener.view_profile(userData);
                break;
        }
    }
}
