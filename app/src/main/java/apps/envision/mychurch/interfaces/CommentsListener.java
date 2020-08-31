package apps.envision.mychurch.interfaces;


import apps.envision.mychurch.pojo.Comments;
import apps.envision.mychurch.pojo.UserData;

/**
 * Created by Ray on 6/25/2018.
 */

public interface CommentsListener {
    void viewReplies(Comments comments, int position);
    void editComment(Comments comments,int position);
    void deleteComment(long id, int position);
    void loadMore();
    boolean isUserComment(String email);
    void reportComment(Comments comments, int position);
    void view_profile(UserData userData);
}
