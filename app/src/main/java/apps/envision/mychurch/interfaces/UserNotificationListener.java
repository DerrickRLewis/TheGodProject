package apps.envision.mychurch.interfaces;

import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.pojo.UserNotifications;

/**
 * Created by Ray on 6/25/2018.
 */

public interface UserNotificationListener {
    void view_profile(UserData userData);
    void viewItem(UserNotifications userNotifications);
    void deleteItem(UserNotifications userNotifications);
}
