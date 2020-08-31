package apps.envision.mychurch.interfaces;


import android.view.View;

import apps.envision.mychurch.pojo.Media;

/**
 * Created by Ray on 6/25/2018.
 */

public interface MediaClickListener {
    void OnItemClick(Media media, String type);
    void OnOptionClick(Media media, View view);
}
