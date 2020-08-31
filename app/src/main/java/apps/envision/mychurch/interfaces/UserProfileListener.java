package apps.envision.mychurch.interfaces;

import apps.envision.mychurch.pojo.UserData;

/**
 * Created by Ray on 6/25/2018.
 */

public interface UserProfileListener {
    void view_profile(UserData userData);
    void follow_unfollow(UserData userData, int position);
}
