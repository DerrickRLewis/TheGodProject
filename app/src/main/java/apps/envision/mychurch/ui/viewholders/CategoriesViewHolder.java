package apps.envision.mychurch.ui.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.interfaces.CategoriesClickListener;
import apps.envision.mychurch.pojo.Categories;
import apps.envision.mychurch.utils.ImageLoader;

public class CategoriesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView title;
    //private Chip media_count;
    private ImageView thumbnail;
    private CategoriesClickListener categoriesClickListener;
    private Categories categories;

    public CategoriesViewHolder(View itemView, CategoriesClickListener categoriesClickListener) {
        super(itemView);
        LinearLayout holder = itemView.findViewById(R.id.itm_holder);
        title = itemView.findViewById(R.id.title);
        thumbnail = itemView.findViewById(R.id.thumbnail);
       // media_count = itemView.findViewById(R.id.media_count);

        this.categoriesClickListener = categoriesClickListener;
        holder.setOnClickListener(this);
        thumbnail.setOnClickListener(this);
    }

    public void bind(Categories categories){
        this.categories = categories;

        String _mdia_count = categories.getMedia_count() > 100 ? "100+" : categories.getMedia_count()+" ";

       // media_count.setText(_mdia_count);
        if(categories.getTitle().equalsIgnoreCase(App.getContext().getString(R.string.all_messages))){
         //   media_count.setVisibility(View.GONE);
            thumbnail.setImageDrawable(App.getContext().getDrawable(R.drawable.messages));
            title.setText(categories.getTitle());
        }else{
            //media_count.setVisibility(View.VISIBLE);
            ImageLoader.loadImage(thumbnail, categories.getThumbnail());
            title.setText(categories.getTitle()+" ("+_mdia_count+")");
        }

        //media_count.setChipIcon(App.getContext().getResources().getDrawable(R.drawable.play_blue));
        //media_count.setChipIconSize(30);
        //media_count.setChipStartPadding(15);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.holder: case R.id.thumbnail: case R.id.play:
                categoriesClickListener.OnItemClick(categories);
                break;
        }
    }
}
