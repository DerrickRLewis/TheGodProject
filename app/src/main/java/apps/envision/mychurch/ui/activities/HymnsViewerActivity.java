package apps.envision.mychurch.ui.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.libs.htmltextview.HtmlHttpImageGetter;
import apps.envision.mychurch.libs.htmltextview.HtmlTextView;
import apps.envision.mychurch.pojo.Devotionals;
import apps.envision.mychurch.pojo.Hymns;
import apps.envision.mychurch.utils.ImageLoader;

public class HymnsViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hymns_viewer);
        init_views();
    }

    private void init_views(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.hymns));

        Gson gson = new Gson();
        Hymns hymns = gson.fromJson(getIntent().getStringExtra("hymns"), Hymns.class);

        if(hymns ==null){
            Toast.makeText(App.getContext(),getString(R.string.no_data),Toast.LENGTH_SHORT).show();
            finish();
        }


        ImageView thumbnail = findViewById(R.id.thumbnail);
        TextView title = findViewById(R.id.title);
        HtmlTextView content = findViewById(R.id.content);

        if(!hymns.getThumbnail().equalsIgnoreCase("")){
            ImageLoader.loadHymnImage(thumbnail, hymns.getThumbnail());
        }
        title.setText(hymns.getTitle());
        content.setHtml(hymns.getContent(),
                new HtmlHttpImageGetter(content,null,true));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
