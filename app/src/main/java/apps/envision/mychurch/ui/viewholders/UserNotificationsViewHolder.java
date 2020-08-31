package apps.envision.mychurch.ui.viewholders;

import android.util.TimeUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.interfaces.UserNotificationListener;
import apps.envision.mychurch.interfaces.UserProfileListener;
import apps.envision.mychurch.libs.RotateLoading;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.pojo.UserNotifications;
import apps.envision.mychurch.utils.ImageLoader;
import apps.envision.mychurch.utils.TimUtil;


public class UserNotificationsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView name;
    private ImageView avatar;
    private UserNotificationListener userNotificationListener;
    private UserNotifications userNotifications;
    private TextView message, time;
    private RelativeLayout view_post;
    private int position = -1;

    public UserNotificationsViewHolder(View itemView, UserNotificationListener userNotificationListener) {
        super(itemView);
        name = itemView.findViewById(R.id.name);
        avatar = itemView.findViewById(R.id.avatar);
        message = itemView.findViewById(R.id.message);
        time = itemView.findViewById(R.id.time);
        view_post = itemView.findViewById(R.id.view_post);
        ImageView proceed = itemView.findViewById(R.id.proceed);
        this.userNotificationListener = userNotificationListener;

        name.setOnClickListener(this);
        avatar.setOnClickListener(this);
        time.setOnClickListener(this);
        message.setOnClickListener(this);
        proceed.setOnClickListener(this);
        view_post.setOnClickListener(this);
    }

    public void bind(UserNotifications userNotifications, int position){
        this.userNotifications = userNotifications;
        this.position = position;
        name.setText(userNotifications.getName());
        ImageLoader.loadUserAvatar(avatar,userNotifications.getAvatar());
        message.setText(userNotifications.getMessage());
        time.setText(TimUtil.timeAgo(userNotifications.getTimestamp()));
        if(userNotifications.getType().equalsIgnoreCase("follow")){
            view_post.setVisibility(View.GONE);
        }else{
            view_post.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.proceed: case R.id.time: case R.id.view_post:
                 userNotificationListener.viewItem(userNotifications);
                break;
            case R.id.name: case R.id.avatar:
                if(PreferenceSettings.isUserLoggedIn() && PreferenceSettings.getUserData()!=null) {
                    UserData userData = new UserData();
                    userData.setEmail(userNotifications.getEmail());
                    userData.setName(userNotifications.getName());
                    userData.setAvatar(userNotifications.getAvatar());
                    userData.setCover_photo(userNotifications.getCover_photo());
                    userNotificationListener.view_profile(userData);
                }
                break;
        }
    }
}
