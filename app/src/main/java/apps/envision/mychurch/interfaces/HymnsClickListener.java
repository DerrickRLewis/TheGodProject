package apps.envision.mychurch.interfaces;


import android.view.View;

import apps.envision.mychurch.pojo.Hymns;
import apps.envision.mychurch.pojo.Media;

/**
 * Created by Ray on 6/25/2018.
 */

public interface HymnsClickListener {
    void OnItemClick(Hymns hymns);
    void share_hymn(Hymns hymns);
    void copy_hymn(Hymns hymns);
    void open_hymn(Hymns hymns);
    void bookmark(Hymns hymns);
    boolean isBookmarked(Hymns hymns);
}
