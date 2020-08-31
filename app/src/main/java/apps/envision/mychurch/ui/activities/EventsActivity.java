package apps.envision.mychurch.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.db.AppDb;
import apps.envision.mychurch.db.DataInterfaceDao;
import apps.envision.mychurch.interfaces.EventsClickListener;
import apps.envision.mychurch.libs.RotateLoading;
import apps.envision.mychurch.libs.horizontalpicker.DatePickerListener;
import apps.envision.mychurch.libs.horizontalpicker.HorizontalPicker;
import apps.envision.mychurch.pojo.Events;
import apps.envision.mychurch.ui.adapters.EventsAdapter;
import apps.envision.mychurch.utils.JsonParser;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsActivity extends AppCompatActivity implements DatePickerListener, View.OnClickListener, EventsClickListener {

    private LinearLayout info_layout;
    private RecyclerView events;
    private RotateLoading progressBar;
    private String selected_date = "";
    private DataInterfaceDao dataInterfaceDao;
    private ExecutorService executorService;
    private TextView message;
    private EventsAdapter eventsAdapter;
    private DateTime dateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppDb db = AppDb.getDatabase(App.getContext());
        dataInterfaceDao = db.dataDbInterface();
        executorService = Executors.newSingleThreadExecutor();
        setContentView(R.layout.activity_events);
        init_views();
    }

    private void init_views(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.events));

        message = findViewById(R.id.message);
        info_layout = findViewById(R.id.info_layout);
        info_layout.setOnClickListener(this);
        progressBar = findViewById(R.id.rotateloading);

        events = findViewById(R.id.events);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        events.setLayoutManager(new GridLayoutManager(this, 1));

        events.setHasFixedSize(true);
        events.setItemAnimator(new DefaultItemAnimator());
        eventsAdapter = new EventsAdapter(this);
        events.setAdapter(eventsAdapter);

        HorizontalPicker picker= (HorizontalPicker) findViewById(R.id.datePicker);
        picker.setListener(this)
                .setDays(356)
                .setOffset(7)
                .showTodayButton(false)
                .setDateSelectedColor(Color.DKGRAY)
                .setDateSelectedTextColor(Color.WHITE)
                .setMonthAndYearTextColor(Color.DKGRAY)
                .setTodayButtonTextColor(getResources().getColor(R.color.colorPrimary))
                .setTodayDateTextColor(getResources().getColor(R.color.colorPrimary))
                .setTodayDateBackgroundColor(Color.GRAY)
                .setUnselectedDayTextColor(Color.DKGRAY)
                .setDayOfWeekTextColor(Color.DKGRAY )
                .setUnselectedDayTextColor(getResources().getColor(R.color.primaryTextColor))
                .init();
        picker.setBackgroundColor(Color.LTGRAY);
        picker.setDate(new DateTime());
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
    public void onDateSelected(DateTime dateSelected) {
        dateTime = dateSelected;
        eventsAdapter.setDate(dateTime);
        selected_date = dateSelected.getYear()+"-"+dateSelected.getMonthOfYear()+"-"+dateSelected.getDayOfMonth();
        executorService.execute(() -> {
            List<Events> eventsList = dataInterfaceDao.getAllEvents(selected_date);
            if(eventsList != null && eventsList.size()!=0){
                runOnUiThread(() -> show_content(eventsList));
            }else{
                runOnUiThread(() -> {
                    try {
                        fetchEvents();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.info_layout:
                try {
                    fetchEvents();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void show_content(List<Events> eventsList){
        progressBar.stop();
        progressBar.setVisibility(View.GONE);
        info_layout.setVisibility(View.GONE);
        events.setVisibility(View.VISIBLE);
        eventsAdapter.setAdapter(eventsList);
    }

    private void show_loader(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.start();
        info_layout.setVisibility(View.GONE);
        events.setVisibility(View.GONE);
    }

    private void display_message(String msg){
        info_layout.setVisibility(View.VISIBLE);
        progressBar.stop();
        progressBar.setVisibility(View.GONE);
        events.setVisibility(View.GONE);
        message.setText(msg);
    }

    private void fetchEvents() throws JSONException {
        show_loader();
        NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);
        JSONObject jsonData = new JSONObject();
        jsonData.put("date", selected_date);
        String requestBody = jsonData.toString();
        Log.e("request", String.valueOf(requestBody));
        Call<String> callAsync = service.fetch_events(requestBody);

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
                        List<Events> eventsList = JsonParser.getEvents(jsonObj.getJSONArray("events"));
                        executorService.execute(() -> dataInterfaceDao.insertAllEvents(eventsList));
                        if(eventsList.size()>0){
                            show_content(eventsList);
                        }else{
                            display_message(getString(R.string.no_events_to_display));
                        }
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

    @Override
    public void OnItemClick(Events events) {
        Gson gson = new Gson();
        String myJson = gson.toJson(events);
        Intent intent = new Intent(this, EventsViewerActivity.class);
        intent.putExtra("events", myJson);
        intent.putExtra("date", dateTime.dayOfWeek().getAsText()+" "+dateTime.monthOfYear().getAsText()+" "+dateTime.getYear());
        startActivity(intent);
    }
}
