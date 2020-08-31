package apps.envision.mychurch.ui.viewholders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.interfaces.MediaClickListener;
import apps.envision.mychurch.libs.materialbanner.holder.Holder;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.utils.FileManager;
import apps.envision.mychurch.utils.ImageLoader;


public class ImageHolderView implements Holder<Media> {
    private RelativeLayout itm_holder;
    private ImageView imageView,play;
    private TextView title,category,duration;
    private MediaClickListener mediaClickListener;

    @Override
    public View createView(Context context, MediaClickListener mediaClickListener) {
        this.mediaClickListener = mediaClickListener;
        View view = LayoutInflater.from(context).inflate(R.layout.slider_layout,null);
        itm_holder = view.findViewById(R.id.itm_holder);
        imageView = view.findViewById(R.id.thumbnail);
        play = view.findViewById(R.id.play);
        title = view.findViewById(R.id.title);
        category = view.findViewById(R.id.category);
        duration = view.findViewById(R.id.duration);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return view;
    }

    @Override
    public void UpdateUI(Context context, int position, Media media) {
        title.setText(media.getTitle());
        ImageLoader.loadImage(imageView, media.getCover_photo());

        if(category!=null)
            category.setText(media.getCategory());

        //if media type is video, show play icon
        if(play!=null) {
            if (media.getMedia_type().equalsIgnoreCase(App.getContext().getString(R.string.video))) {
                play.setVisibility(View.VISIBLE);
            } else {
                play.setVisibility(View.GONE);
            }
        }

        //set duration for either remote url or local file
        if(duration!=null) {
            if (media.isHttp()) {
                FileManager.set_file_duration(media.getDuration(), duration);
            } else {
                FileManager.set_file_duration(media.getStream_url(), duration);
            }
        }
        itm_holder.setOnClickListener(v -> mediaClickListener.OnItemClick(media,"slider"));
        itm_holder.setOnLongClickListener(v -> {
            mediaClickListener.OnOptionClick(media,title);
            return false;
        });
    }

}
