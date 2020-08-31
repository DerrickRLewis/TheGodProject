package apps.envision.mychurch.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import apps.envision.mychurch.ui.activities.ActivityVideoPlayer;
import apps.envision.mychurch.ui.activities.LiveStreamsPlayer;

public class ExoPlayerFullScreenHandler {

    private Context context;
    private boolean mExoPlayerFullscreen = false;
    private Dialog mFullScreenDialog;

    public ExoPlayerFullScreenHandler(Context context){
        this.context = context;
    }


    public void initFullscreenDialog(FrameLayout frameLayout,View view) {
        if(mFullScreenDialog!=null)mFullScreenDialog.dismiss();
        mFullScreenDialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            public void onBackPressed() {
                if (mExoPlayerFullscreen)
                    closeFullscreenDialog(frameLayout,view);
                super.onBackPressed();
            }
        };
       /* final WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        params.copyFrom(mFullScreenDialog.getWindow().getAttributes());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        mFullScreenDialog.getWindow().setAttributes(params);*/
    }

    public void fullscreenToggle(FrameLayout frameLayout,View view){
        if(mExoPlayerFullscreen){
            closeFullscreenDialog(frameLayout,view);
        }else{
            openFullscreenDialog(view);
        }
    }


    private void openFullscreenDialog(View view) {
        ((ViewGroup) view.getParent()).removeView(view);
        if(context instanceof ActivityVideoPlayer)
        ((ActivityVideoPlayer)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else if(context instanceof LiveStreamsPlayer)
            ((LiveStreamsPlayer)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mFullScreenDialog.addContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mExoPlayerFullscreen = true;
        mFullScreenDialog.show();
    }


    private void closeFullscreenDialog(FrameLayout frameLayout,View view) {
        if(context instanceof ActivityVideoPlayer)
            ((ActivityVideoPlayer)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else if(context instanceof LiveStreamsPlayer)
            ((LiveStreamsPlayer)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ((ViewGroup) view.getParent()).removeView(view);
        frameLayout.addView(view);
        mExoPlayerFullscreen = false;
        mFullScreenDialog.dismiss();
    }
}
