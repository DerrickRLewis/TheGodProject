package apps.envision.mychurch.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.libs.audio_playback.EqualizerUtils;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.db.DataViewModel;
import apps.envision.mychurch.interfaces.MediaOptionsListener;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.pojo.Radio;
import apps.envision.mychurch.ui.activities.ActivityVideoPlayer;
import apps.envision.mychurch.ui.activities.AddPlaylistActivity;
import apps.envision.mychurch.ui.activities.AudioPlayerActivity;
import apps.envision.mychurch.ui.activities.DownloadsActivity;
import apps.envision.mychurch.ui.activities.MainActivity;
import apps.envision.mychurch.ui.activities.PlaylistActivity;
import apps.envision.mychurch.ui.activities.RadioPlayerActivity;

/*
* PopupMenu utility class
 */

public class MediaOptions {
    private Context context;
    private MediaOptionsListener mediaOptionsListener;
    private Utility utility;
    /**
     * required class constructor
     * @param context
     */
    public MediaOptions(Context context, MediaOptionsListener mediaOptionsListener){
        this.context = context;
        utility = new Utility(context);
        this.mediaOptionsListener = mediaOptionsListener;
    }

    public MediaOptions(Context context){
        this.context = context;
        utility = new Utility(context);
    }

    /**
     * method to display popup menu
     * @param view
     */
    public void display(View view,Media media){
        PopupMenu popup = new PopupMenu(context, view);
        Menu menu = popup.getMenu();
        populateMenu(menu, media);
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            switch(id){
                case R.id.add_playlist:
                    addMediaToPlaylist(media);
                    break;
                case R.id.download:
                    download_media(media);
                    break;
                case R.id.pin:
                    mediaOptionsListener.bookmark(media);
                    break;
                case R.id.share:
                    shareThisMedia(media);
                    break;
            }
            return true;
        });

        popup.show();
    }

    /**
     * method to display popup menu
     * @param view
     */
    public void audioDisplay(View view,Media media){
        PopupMenu popup = new PopupMenu(context, view);
        Menu menu = popup.getMenu();
        populateAudioMenu(menu, media);
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            switch(id){
                case R.id.add_playlist:
                    Gson gson2 = new Gson();
                    String myJson2 = gson2.toJson(media);
                    Intent intent2 = new Intent(context, AddPlaylistActivity.class);
                    intent2.putExtra("media", myJson2);
                    context.startActivity(intent2);
                    break;
                case R.id.download:
                    download_media(media);
                    break;
                case R.id.equalizer:
                    LocalMessageManager.getInstance().send(R.id.open_equalizer);
                    break;
                case R.id.share:
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    String shareBody = "listen to  " + media.getTitle() + " "
                            + context.getResources().getString(R.string.share_text)
                            + " http://play.google.com/store/apps/details?id=" + context.getPackageName();
                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareBody);
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                    context.startActivity(Intent.createChooser(sharingIntent, "Share via"));
                    break;
            }
            return true;
        });

        popup.show();
    }

    /**
     * methos to share media
     * @param media
     */
    public void shareThisMedia(Media media){
        if(mediaOptionsListener.isDownloads()){
            File audio = new File(media.getStream_url());
            Intent intent = new Intent(Intent.ACTION_SEND).setType("*/*");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(audio));
            context.startActivity(Intent.createChooser(intent, "Share to"));
        }else {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "listen to  " + media.getTitle() + " "
                    + context.getResources().getString(R.string.share_text)
                    + " http://play.google.com/store/apps/details?id=" + context.getPackageName();
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareBody);
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            context.startActivity(Intent.createChooser(sharingIntent, "Share via"));
        }
    }

    public void addMediaToPlaylist(Media media){
        mediaOptionsListener.addToPlaylist(media);
    }

    /**
     * populate popup menu
     * @param menu
     * @param media
     */
    private void populateMenu(Menu menu, Media media){
        if(!mediaOptionsListener.isDownloads()){
            if(media.isCan_download()) {
                menu.add(0, R.id.download, Menu.FIRST, context.getString(R.string.download)).setIcon(context.getResources().getDrawable(R.drawable.download));
            }
        }else{
            menu.add(0, R.id.download, Menu.FIRST, context.getString(R.string.delete_media)).setIcon(context.getResources().getDrawable(R.drawable.download));
        }

        if(mediaOptionsListener.isPlaylistActivity()){
            menu.add(0, R.id.add_playlist, Menu.FIRST, context.getString(R.string.remove_from_playlist));
        }else {
            menu.add(0, R.id.add_playlist, Menu.FIRST, context.getString(R.string.add_to_playlist));
        }
        if(mediaOptionsListener.isBookmarked(media)) {
            menu.add(0, R.id.pin, Menu.FIRST, context.getString(R.string.remove_bookmark));
        }else{
            menu.add(0, R.id.pin, Menu.FIRST, context.getString(R.string.bookmark));
        }
        menu.add(0, R.id.share, Menu.FIRST, context.getString(R.string.share));
    }

    private void populateAudioMenu(Menu menu, Media media){
        if(media.isHttp() && media.isCan_download()) {
            menu.add(0, R.id.download, Menu.FIRST, context.getString(R.string.download)).setIcon(context.getResources().getDrawable(R.drawable.download));
        }
        menu.add(0, R.id.add_playlist, Menu.FIRST, context.getString(R.string.add_to_playlist));
        if(EqualizerUtils.hasEqualizer(context)) {
            menu.add(0, R.id.equalizer, Menu.FIRST, context.getString(R.string.equalizer));
        }
        menu.add(0, R.id.share, Menu.FIRST, context.getString(R.string.share));
    }

    /**
     * method to initiate media download
     * @param media
     */
    public void download_media(Media media){
        if(media.isHttp()) {
            if(utility.canUserDownloadMedia(media)){
                start_downloads(media);
            } else {
                new Utility(context).show_subscribe_alert(media);
            }
        }else{
            delete_file(media);
        }
    }

    /**
     * @param media
     */
    private void start_downloads(Media media){
        if(utility.isUserBlocked()){
            return;
        }
        Gson gson = new Gson();
        String myJson = gson.toJson(media);
        Intent intent = new Intent(context, DownloadsActivity.class);
        intent.putExtra("media", myJson);
        context.startActivity(intent);
    }

    /**
     * delete downloaded media file
     * @param media
     */
    private void delete_file(Media media){
        String header = App.getContext().getResources().getString(R.string.delete_media);
        String body = App.getContext().getResources().getString(R.string.delete_media_hint);

        new AlertDialog.Builder(context)
                .setTitle(header)
                .setMessage(body)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    try {
                        File file = new File(media.getStream_url());

                        if (file.delete()) {
                            LocalMessageManager.getInstance().send(R.id.reload_downloads,media.getMedia_type());
                            Toast.makeText(App.getContext(), media.getTitle()+" deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(App.getContext(), App.getContext().getResources().getString(R.string.delete_media_failed), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    // do nothing
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * play video/audio media
     * @param dataViewModel
     * @param mediaList
     * @param media
     */
    public void play_media(DataViewModel dataViewModel, List<Media> mediaList, Media media){
        if(utility.requiresUserSubscription(media)){//if user need to subscribe to play this media
            utility.show_play_subscribe_alert(media.getMedia_type());
        }else {
            if(utility.isUserBlocked()){ //if user account is blocked
                return;
            }
            Intent intent;
            Gson gson = new Gson();
            String myJson = gson.toJson(media);

            if (media.getMedia_type().equalsIgnoreCase(App.getContext().getString(R.string.audio))) {
                dataViewModel.clearPlayingAudios();
                dataViewModel.insertPlayingAudios(ObjectMapper.mapPlayingAudios(mediaList));
                intent = new Intent(context, AudioPlayerActivity.class);
            } else {
                dataViewModel.clearPlayingVideos();
                dataViewModel.insertPlayingVideos(ObjectMapper.mapPlayingVideos(mediaList));
                intent = new Intent(context, ActivityVideoPlayer.class);
            }
            intent.putExtra("media", myJson);
            context.startActivity(intent);
            if (media.getMedia_type().equalsIgnoreCase(App.getContext().getString(R.string.audio))) {
                if (context instanceof MainActivity) {
                    ((MainActivity) context).overridePendingTransition(R.anim.slide_up, R.anim.still);
                }
                if (context instanceof PlaylistActivity) {
                    ((PlaylistActivity) context).overridePendingTransition(R.anim.slide_up, R.anim.still);
                }
                if (context instanceof DownloadsActivity) {
                    ((DownloadsActivity) context).overridePendingTransition(R.anim.slide_up, R.anim.still);
                }

                PreferenceSettings.setRadioPlayerFlag(false);
            }
        }
    }

    /**
     * play audio media
     * @param radio
     */
    public void play_radio(Radio radio){
        Intent intent;
        Gson gson = new Gson();
        String myJson = gson.toJson(ObjectMapper.mapMediaFromRadio(radio));

        intent = new Intent(context, RadioPlayerActivity.class);
        intent.putExtra("media", myJson);
        context.startActivity(intent);
        if (context instanceof MainActivity) {
            ((MainActivity) context).overridePendingTransition(R.anim.slide_up, R.anim.still);
        }
        if (context instanceof PlaylistActivity) {
            ((PlaylistActivity) context).overridePendingTransition(R.anim.slide_up, R.anim.still);
        }
        if (context instanceof DownloadsActivity) {
            ((DownloadsActivity) context).overridePendingTransition(R.anim.slide_up, R.anim.still);
        }

        //
        PreferenceSettings.setRadioPlayerFlag(true);
    }
}