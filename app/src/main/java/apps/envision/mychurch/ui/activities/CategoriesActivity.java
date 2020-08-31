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

import static apps.envision.mychurch.utils.Utility.darkenColor;

public class CategoriesActivity extends AppCompatActivity implements LocalMessageCallback, View.OnClickListener {

    private CollapsingToolbarLayout collapsingToolbar;
    Categories categories = null;
    private ChipGroup chipGroup;
    private boolean sub_category_loaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        Gson gson = new Gson();
        categories = gson.fromJson(getIntent().getStringExtra("category"), Categories.class);

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
                collapsingToolbar.setTitle(categories.getTitle().substring(0, 1).toUpperCase() + categories.getTitle().substring(1));
            } else {
                //Expanded
                collapsingToolbar.setTitle("");
            }
        });
        appBarLayout.setExpanded(false);
        setCategoryDetails(categories);
        setFragment(CategoriesMediaFragment.newInstance(categories.getId()));

        Chip all_items = findViewById(R.id.all_messages);
        all_items.setOnClickListener(this);
        Chip videos = findViewById(R.id.videos);
        videos.setOnClickListener(this);
        Chip audios = findViewById(R.id.audios);
        audios.setOnClickListener(this);
    }

    private void setCategoryDetails(Categories category){
        chipGroup = findViewById(R.id.chip_group);
        TextView title = findViewById(R.id.title);
        title.setText(category.getTitle().substring(0, 1).toUpperCase() + category.getTitle().substring(1));
        TextView media_count = findViewById(R.id.media_count);
        if(category.getMedia_count()>0) {
            media_count.setVisibility(View.VISIBLE);
            String _mdia_count = category.getMedia_count() > 100 ? "100+ " : category.getMedia_count()+" ";
            media_count.setText(_mdia_count + getString(R.string.media_tracks));
        }else{
            media_count.setVisibility(View.GONE);
        }
        ImageView header_img = findViewById(R.id.header_img);
        if(category.getThumbnail().equalsIgnoreCase("")){
            header_img.setImageDrawable(getResources().getDrawable(R.drawable.messages));
        }else {
            Glide.with(App.getContext())
                    .asBitmap().load(category.getThumbnail())
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void add_subcategory_chips(List<Categories> categoriesList){
        if(sub_category_loaded)return;
        chipGroup.removeAllViews();
        for (Categories categories: categoriesList) {
            Chip chip = new Chip(CategoriesActivity.this);
            chip.setText(categories.getTitle());
            //chip.setChipIcon(App.getContext().getResources().getDrawable(R.drawable.genre));
            chip.setChipIconSize(40);
            chip.setChipStartPadding(15);
            chip.setCheckable(true);
            //chip.setChipBackgroundColorResource(R.color.bg_chip_state_list);
            //chip.setTextColor(getResources().getColor(R.color.text_color_chip_state_list));
            chip.setOnClickListener(v -> {
               LocalMessageManager.getInstance().send(R.id.fetch_sub_categories_media,categories.getId());
            });
            chipGroup.addView(chip);
        }
        sub_category_loaded = true;
    }

    protected void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalMessageManager.getInstance().addListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalMessageManager.getInstance().removeListener(this);
    }

    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
       /* if (localMessage.getId() == R.id.sub_categories) {
            String _sub_cats = (String) localMessage.getObject();
            Type listType = new TypeToken<List<Categories>>() {
            }.getType();
            List<Categories> sub_categories = new Gson().fromJson(_sub_cats, listType);
            if (sub_categories != null)
                add_subcategory_chips(sub_categories);
        }*/
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.all_messages:
                LocalMessageManager.getInstance().send(R.id.fetch_sub_categories_media,"all");
                break;
            case R.id.videos:
                LocalMessageManager.getInstance().send(R.id.fetch_sub_categories_media,"video");
                break;
            case R.id.audios:
                LocalMessageManager.getInstance().send(R.id.fetch_sub_categories_media,"audio");
                break;
        }
    }
}
