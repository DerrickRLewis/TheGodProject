package apps.envision.mychurch.interfaces;


import apps.envision.mychurch.pojo.Replies;
import apps.envision.mychurch.pojo.UserData;

/**
 * Created by Ray on 6/25/2018.
 */

public interface RepliesListener {
    void editComment(Replies replies, int position);
    void deleteComment(long id, int position);
    void reportComment(Replies replies, int position);
    void loadMore();
    boolean isUserComment(String email);
    void view_profile(UserData userData);
}
