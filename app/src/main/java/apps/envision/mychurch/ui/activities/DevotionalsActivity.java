package apps.envision.mychurch.ui.activities;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.db.AppDb;
import apps.envision.mychurch.db.DataInterfaceDao;
import apps.envision.mychurch.libs.RotateLoading;
import apps.envision.mychurch.libs.horizontalpicker.DatePickerListener;
import apps.envision.mychurch.libs.horizontalpicker.HorizontalPicker;
import apps.envision.mychurch.libs.htmltextview.HtmlHttpImageGetter;
import apps.envision.mychurch.libs.htmltextview.HtmlTextView;
import apps.envision.mychurch.pojo.Devotionals;
import apps.envision.mychurch.utils.ImageLoader;
import apps.envision.mychurch.utils.JsonParser;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DevotionalsActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout article_layout,info_layout;
    private RotateLoading progressBar;
    private TextView message,title,author, date;
    private ImageView thumbnail;
    private HtmlTextView content,bible_reading,confession,studies;
    private String selected_date = "";
    private String formatted_date = "";
    private DataInterfaceDao dataInterfaceDao;
    private ExecutorService executorService;
    private DatePickerDialog picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppDb db = AppDb.getDatabase(App.getContext());
        dataInterfaceDao = db.dataDbInterface();
        executorService = Executors.newSingleThreadExecutor();

        setContentView(R.layout.activity_devotionals);
        init_views();
    }

    private void init_views(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.devotionals));


        thumbnail = findViewById(R.id.thumbnail);
        title = findViewById(R.id.title);
        date = findViewById(R.id.date);
        author = findViewById(R.id.author);
        bible_reading = findViewById(R.id.bible_reading);
        content = findViewById(R.id.content);
        confession = findViewById(R.id.confession);
        studies = findViewById(R.id.studies);
        message = findViewById(R.id.message);
        info_layout = findViewById(R.id.info_layout);
        info_layout.setOnClickListener(this);
        article_layout = findViewById(R.id.article_layout);
        progressBar = findViewById(R.id.rotateloading);

        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        // date picker dialog
        picker = new DatePickerDialog(DevotionalsActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                       fetch_date_data(year,monthOfYear,dayOfMonth);
                    }
                }, year, month, day);
        fetch_date_data(year,month,day);
    }

    private void fetch_date_data(int year, int monthOfYear, int dayOfMonth){
        int month = monthOfYear + 1;
        String _month = String.valueOf(month);
        if(month<10){
            _month = String.format(Locale.getDefault(),"%02d", month);
        }
        String _day = String.valueOf(dayOfMonth);
        if(dayOfMonth<10){
            _day = String.format(Locale.getDefault(),"%02d", dayOfMonth);
        }
        selected_date = (new StringBuilder().append(year).append("-").append(_month).append("-").append(_day)).toString();
        //selected_date = year+"-"+monthOfYear+"-"+dayOfMonth;
        SimpleDateFormat dateFormatprev = new SimpleDateFormat("yyyy-MM-dd");
        Date d = null;
        try {
            d = dateFormatprev.parse(selected_date);
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd MMMM yyyy");
            formatted_date = dateFormat.format(d);
            Log.e("formatted_date", formatted_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        executorService.execute(() -> {
            Devotionals devotionals = dataInterfaceDao.getDevotional(selected_date);
            if(devotionals != null && !devotionals.getTitle().equalsIgnoreCase("")){
                runOnUiThread(() -> show_content(devotionals));
            }else{
                runOnUiThread(() -> {
                    try {
                        fetchDevotionals();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            case R.id.date:
                picker.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.date, menu);
        /*Drawable drawable = menu.findItem(R.id.date).getIcon();

        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(this,R.color.white));
        menu.findItem(R.id.date).setIcon(drawable);*/
        return true;
    }

    @Override
    public void onClick(View v) {
      switch (v.getId()){
          case R.id.info_layout:
              try {
                  fetchDevotionals();
              } catch (JSONException e) {
                  e.printStackTrace();
              }
              break;
      }
    }

    private void show_content(Devotionals devotionals){
        progressBar.stop();
        progressBar.setVisibility(View.GONE);
        info_layout.setVisibility(View.GONE);
        article_layout.setVisibility(View.VISIBLE);

        if(!devotionals.getThumbnail().equalsIgnoreCase("") ||
        !devotionals.getThumbnail().equalsIgnoreCase("empty")){
            ImageLoader.loadDevotionalImage(thumbnail, devotionals.getThumbnail());
        }
        title.setText(devotionals.getTitle());
        date.setText(formatted_date);
        author.setText(devotionals.getAuthor());
        bible_reading.setHtml(devotionals.getBible_reading(),
                new HtmlHttpImageGetter(bible_reading,null,true));
        content.setHtml(devotionals.getContent(),
                new HtmlHttpImageGetter(content,null,true));
        confession.setHtml(devotionals.getConfession(),
                new HtmlHttpImageGetter(confession,null,true));
        studies.setHtml(devotionals.getStudies(),
                new HtmlHttpImageGetter(studies,null,true));
    }

    private void show_loader(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.start();
        info_layout.setVisibility(View.GONE);
        article_layout.setVisibility(View.GONE);
    }

    private void display_message(String msg){
        info_layout.setVisibility(View.VISIBLE);
        progressBar.stop();
        progressBar.setVisibility(View.GONE);
        article_layout.setVisibility(View.GONE);
        message.setText(msg);
    }

    private void fetchDevotionals() throws JSONException {
        show_loader();
        NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);
        JSONObject jsonData = new JSONObject();
        jsonData.put("date", selected_date);
        String requestBody = jsonData.toString();
        Log.e("request", String.valueOf(requestBody));
        Call<String> callAsync = service.devotionals(requestBody);

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
                    if(jsonObj.getString("status").equalsIgnoreCase("ok")) {
                        Devotionals devotionals = JsonParser.getDevotionals(jsonObj.getJSONObject("devotional"));
                        executorService.execute(() -> dataInterfaceDao.insertDevotional(devotionals));
                        show_content(devotionals);
                    }else{
                        display_message(getString(R.string.no_devotionals));
                    }

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
