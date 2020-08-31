package apps.envision.mychurch.ui.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.interfaces.MediaClickListener;
import apps.envision.mychurch.libs.audio_playback.EqualizerView;
import apps.envision.mychurch.libs.audio_playback.PlaybackInfoListener;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.utils.FileManager;
import apps.envision.mychurch.utils.ImageLoader;
import apps.envision.mychurch.utils.Utility;

public class MediaViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView title,category,media_views,duration;
    private ImageView thumbnail,options,play;
    private MediaClickListener mediaClickListener;
    private EqualizerView equalizer;
    private Media media;
    private String type = "";

    public MediaViewHolder(View itemView, MediaClickListener mediaClickListener) {
        super(itemView);
        LinearLayout holder = itemView.findViewById(R.id.itm_holder);
        title = itemView.findViewById(R.id.title);
        category = itemView.findViewById(R.id.category);
        media_views = itemView.findViewById(R.id.media_views);
        thumbnail = itemView.findViewById(R.id.thumbnail);
        options = itemView.findViewById(R.id.options);
        duration = itemView.findViewById(R.id.duration);
        play = itemView.findViewById(R.id.play);
        equalizer=itemView.findViewById(R.id.equalizer);

        this.mediaClickListener = mediaClickListener;
        holder.setOnClickListener(this);
        thumbnail.setOnClickListener(this);
        title.setOnClickListener(this);
    }

    public void setType(String type){
        this.type = type;
    }

    public void bind(Media media){
        this.media = media;
        title.setText(media.getTitle());
        if(category!=null)
        category.setText(media.getCategory());

        //set duration for either remote url or local file
        if(duration!=null) {
            if (media.isHttp()) {
                FileManager.set_file_duration(media.getDuration(), duration);
            } else {
                FileManager.set_file_duration(media.getStream_url(), duration);
            }
        }

        //if media type is video, show play icon
        if(play!=null) {
            if (media.getMedia_type().equalsIgnoreCase(App.getContext().getString(R.string.video))) {
                play.setVisibility(View.VISIBLE);
            } else {
                play.setVisibility(View.GONE);
            }
        }
        if(media.isHttp()) {
            ImageLoader.loadImage(thumbnail, media.getCover_photo());
        }else{
            if(media.getMedia_type().equalsIgnoreCase(App.getContext().getString(R.string.video))){
                ImageLoader.loadThumbnail(thumbnail,media.getStream_url());
            }
        }

        if(options!=null) {
            options.setOnClickListener(v -> mediaClickListener.OnOptionClick(media, options));
        }

        if(media_views!=null && media.getViews_count()>0) {
            media_views.setText(Utility.reduceCountToString(media.getViews_count()) + App.getContext().getString(R.string.views));
        }

        if(equalizer!=null){
            equalizer.setVisibility(View.GONE);
            if (equalizer.isAnimating()) {
                equalizer.stopBars();
            }
            thumbnail.setVisibility(View.VISIBLE);
        }
    }

    public void setEqualizerView(int state){
        if(equalizer!=null){
            if(state== PlaybackInfoListener.State.PLAYING || state== PlaybackInfoListener.State.RESUMED){
                equalizer.animateBars();
            }else{
                if (equalizer.isAnimating()) {
                    equalizer.stopBars();
                }
            }
            equalizer.setVisibility(View.VISIBLE);
            options.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.holder: case R.id.thumbnail: case R.id.play: case R.id.title:
                mediaClickListener.OnItemClick(media,type);
                break;
        }
    }
}
