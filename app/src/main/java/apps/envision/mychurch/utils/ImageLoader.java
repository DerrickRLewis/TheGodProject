package apps.envision.mychurch.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.libs.frisson.FrissonView;
import glimpse.glide.GlimpseTransformation;

/**
 * Created by link on 19/04/2018.
 * Glide image loader helper class
 */

public class ImageLoader {

    public static void loadUserAvatar(ImageView imageView, String url){
        RequestOptions options = new RequestOptions()
                //.centerCrop()
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .dontAnimate()
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .priority(Priority.HIGH);

        Glide.with(App.getContext()).load(url)
                .apply(options)
                .transform(new GlimpseTransformation())
                .into(imageView);
    }

    public static void loadImage(ImageView imageView, String url){
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.image)
                .error(R.drawable.image)
                .dontAnimate()
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);

        Glide.with(App.getContext()).load(url)
                .apply(options)
                .into(imageView);
    }

    public static void loadVideoThumbnail(ImageView imageView, String url){
        long interval = 2000;
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.video_thumbnail)
                .error(R.drawable.video_thumbnail)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .frame(interval);
        Glide.with(App.getContext()).asBitmap()
                .load(url)
                .apply(options)
                //.diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .transform(new GlimpseTransformation())
                .into(imageView);
    }

    public static void loadUploadImage(ImageView imageView, String url){
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.image)
                .error(R.drawable.image)
                .dontAnimate()
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.LOW);

        Glide.with(App.getContext()).load(url)
                .apply(options)
                .into(imageView);
    }

    public static void loadPostImage(ImageView imageView, String url){
        RequestOptions options = new RequestOptions()
                //.centerCrop()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.no_image_placeholder)
                .dontAnimate()
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .priority(Priority.HIGH);

        Glide.with(App.getContext())
                .load(url)
                .apply(options)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .transform(new GlimpseTransformation())
                .into(imageView);
    }

    public static void loadCoverPhotoImage(FrissonView imageView, String url){
        Glide.with(App.getContext())
                .asBitmap().load(url)
                .listener(new RequestListener<Bitmap>() {
                              @Override
                              public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Bitmap> target, boolean b) {

                                  return false;
                              }

                              @Override
                              public boolean onResourceReady(Bitmap bitmap, Object o, Target<Bitmap> target, DataSource dataSource, boolean b) {
                                  imageView.setBitmap(bitmap);
                                  return true;
                              }
                          }
                ).submit();
    }

    public static void loadCoverPhotoImage(FrissonView imageView, Uri url){
        Glide.with(App.getContext())
                .asBitmap().load(url)
                .listener(new RequestListener<Bitmap>() {
                              @Override
                              public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Bitmap> target, boolean b) {

                                  return false;
                              }

                              @Override
                              public boolean onResourceReady(Bitmap bitmap, Object o, Target<Bitmap> target, DataSource dataSource, boolean b) {
                                  imageView.setBitmap(bitmap);
                                  return true;
                              }
                          }
                ).submit();
    }

    public static void loadImageThumbnail(ImageView imageView, String url){
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.event)
                .error(R.drawable.event)
                .dontAnimate()
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);

        Glide.with(App.getContext()).load(url)
                .apply(options)
                .into(imageView);
    }

    public static void loadDevotionalImage(ImageView imageView, String url){
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.devotionals)
                .error(R.drawable.devotionals)
                .dontAnimate()
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);

        Glide.with(App.getContext()).load(url)
                .apply(options)
                .into(imageView);
    }


    public static void loadHymnImage(ImageView imageView, String url){
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.worship)
                .error(R.drawable.worship)
                .dontAnimate()
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);

        Glide.with(App.getContext()).load(url)
                .apply(options)
                .into(imageView);
    }

    public static RequestOptions getCircularDisplayRequestOptions(){
        return new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.radio)
                .error(R.drawable.radio)
                .override(800, 800)
                .transform(new CircleTransform())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);
    }

    public static RequestOptions getDisplayRequestOptions(){
        return new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.image)
                .error(R.drawable.image)
                .transform(new BlurTransformation(App.getContext()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);
    }

    public static void loadThumbnail(ImageView imageView, String url){
        RequestOptions options = new RequestOptions()
                .centerCrop()
                //.placeholder(R.drawable.loading)
                //.error(R.drawable.no_image_placeholder)
                .dontAnimate()
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);

        Glide.with(App.getContext()).load(url)
                .apply(options)
                .into(imageView);
    }
}
