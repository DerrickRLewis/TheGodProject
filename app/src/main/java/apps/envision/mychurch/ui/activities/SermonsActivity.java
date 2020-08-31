package apps.envision.mychurch.ui.activities;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.palette.graphics.Palette;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Type;
import java.util.List;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.Categories;
import apps.envision.mychurch.ui.fragments.CategoriesMediaFragment;
import apps.envision.mychurch.ui.fragments.MediaFragment;

import static apps.envision.mychurch.utils.Utility.darkenColor;

public class SermonsActivity extends AppCompatActivity {

    private CollapsingToolbarLayout collapsingToolbar;
    private String media_type = "audio";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sermons);

        media_type = getIntent().getStringExtra("media_type");

        Toolbar toolbar = (Toolbar) findViewById(R.id.anim_toolbar);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbar.setTitle("");
        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            if (Math.abs(verticalOffset)- appBarLayout1.getTotalScrollRange() == 0) {
                //  Collapsed
                if(media_type.equalsIgnoreCase("video")){
                    collapsingToolbar.setTitle(getString(R.string.video_tracks));
                }else{
                    collapsingToolbar.setTitle(getString(R.string.audio_tracks));
                }
            } else {
                //Expanded
                collapsingToolbar.setTitle("");
            }
        });
        appBarLayout.setExpanded(false);
        setCategoryDetails();
        setFragment(MediaFragment.newInstance(media_type));
    }

    private void setCategoryDetails(){
        ImageView header_img = findViewById(R.id.header_img);
        Glide.with(App.getContext())
                .asBitmap().load(R.drawable.messages)
                .listener(new RequestListener<Bitmap>() {
                              @Override
                              public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Bitmap> target, boolean b) {
                                  return false;
                              }

                              @Override
                              public boolean onResourceReady(Bitmap bitmap, Object o, Target<Bitmap> target, DataSource dataSource, boolean b) {
                                  header_img.setImageBitmap(bitmap);
                                  Palette.from(bitmap).generate(palette -> {
                                      if (palette != null) {
                                          int vibrantColor = palette.getVibrantColor(getResources().getColor(R.color.primary));
                                          collapsingToolbar.setContentScrimColor(vibrantColor);
                                          collapsingToolbar.setStatusBarScrimColor(getResources().getColor(R.color.black_trans80));
                                          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                              getWindow().setStatusBarColor(
                                                      darkenColor(vibrantColor));
                                          }
                                      }
                                  });
                                  return false;
                              }
                          }
                ).submit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }
}
