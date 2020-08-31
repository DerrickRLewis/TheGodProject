package apps.envision.mychurch.libs.materialbanner.holder;

/**
 * Created by Sai on 15/12/14.
 * @param <T> 任何你指定的对象
 */

import android.content.Context;
import android.view.View;

import apps.envision.mychurch.interfaces.MediaClickListener;


public interface Holder<T>{
    View createView(Context context, MediaClickListener mediaClickListener);
    void UpdateUI(Context context, int position, T data);
}