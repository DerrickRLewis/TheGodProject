package apps.envision.mychurch.ui.activities;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.libs.RotateLoading;
import apps.envision.mychurch.libs.htmltextview.HtmlHttpImageGetter;
import apps.envision.mychurch.libs.htmltextview.HtmlTextView;
import apps.envision.mychurch.pojo.Events;
import apps.envision.mychurch.pojo.Inbox;
import apps.envision.mychurch.utils.NetworkUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static apps.envision.mychurch.utils.Utility.darkenColor;

public class InboxViewerActivity extends AppCompatActivity implements View.OnClickListener {

    private RotateLoading progressBar;
    private LinearLayout info_layout;
    private HtmlTextView details;
    private Inbox inbox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox_viewer);

        Gson gson = new Gson();
        inbox = gson.fromJson(getIntent().getStringExtra("inbox"), Inbox.class);

        if(inbox ==null){
            Toast.makeText(App.getContext(),getString(R.string.no_data),Toast.LENGTH_SHORT).show();
            finish();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.inbox));

        TextView title = findViewById(R.id.title);
        TextView date = findViewById(R.id.date);
        TextView time = findViewById(R.id.time);
        //ImageView thumbnail = findViewById(R.id.thumbnail);
        details = findViewById(R.id.details);
        progressBar = findViewById(R.id.progress_bar);
        info_layout = findViewById(R.id.info_layout);
        info_layout.setOnClickListener(this);

        DateTime dateTime =  new DateTime(inbox.getDate() * 1000L);
        String mer_ = dateTime.getHourOfDay()<12?"AM":"PM";

        title.setText(inbox.getTitle());
        date.setText(dateTime.dayOfWeek().getAsText()+" "+dateTime.monthOfYear().getAsText()+" "+dateTime.getYear());
        time.setText(dateTime.getHourOfDay() +":"+dateTime.getMinuteOfHour()+mer_);

        if(inbox.getMessage().equalsIgnoreCase("")){
            try {
                fetchData();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else {
            details.setHtml(inbox.getMessage(),
                    new HtmlHttpImageGetter(details, null, true));
        }

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

    @Override
    public void onClick(View v) {
        try {
            fetchData();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void show_content(){
        progressBar.stop();
        progressBar.setVisibility(View.GONE);
        info_layout.setVisibility(View.GONE);
        details.setVisibility(View.VISIBLE);
    }

    private void show_loader(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.start();
        info_layout.setVisibility(View.GONE);
        details.setVisibility(View.GONE);
    }

    private void display_message(String msg){
        info_layout.setVisibility(View.VISIBLE);
        progressBar.stop();
        progressBar.setVisibility(View.GONE);
        details.setVisibility(View.GONE);
    }

    private void fetchData() throws JSONException {
        if(!NetworkUtil.hasConnection(this)){
            display_message(getString(R.string.network_error));
            return;
        }
        show_loader();
        NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);
        JSONObject jsonData = new JSONObject();
        jsonData.put("id", inbox.getId());
        jsonData.put("type","inbox");
        String requestBody = jsonData.toString();
        Log.e("request", String.valueOf(requestBody));
        Call<String> callAsync = service.get_article_content(requestBody);

        callAsync.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                Log.e("response", String.valueOf(response.body()));
                if(response.body()==null){
                    display_message(App.getContext().getString(R.string.data_error));
                    return;
                }
                try {
                    JSONObject jsonObj = new JSONObject(response.body());
                    inbox.setMessage(jsonObj.getString("content"));
                    details.setHtml(jsonObj.getString("content"),
                            new HtmlHttpImageGetter(details,null,true));
                    show_content();
                } catch (JSONException e) {
                    Log.d("json parse Error", e.toString());
                    display_message(App.getContext().getString(R.string.data_error));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                display_message(App.getContext().getString(R.string.data_error));
                Log.e("error", String.valueOf(throwable.getMessage()));
            }
        });
    }
}
