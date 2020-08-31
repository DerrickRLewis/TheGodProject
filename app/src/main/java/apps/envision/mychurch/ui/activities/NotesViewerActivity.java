package apps.envision.mychurch.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;

import org.joda.time.DateTime;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.pojo.Note;
import es.dmoral.markdownview.MarkdownView;

public class NotesViewerActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_viewer);

        Gson gson = new Gson();
        Note note = gson.fromJson(getIntent().getStringExtra("note"), Note.class);

        if(note ==null){
            Toast.makeText(App.getContext(),getString(R.string.no_data),Toast.LENGTH_SHORT).show();
            finish();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.notes));

        TextView title = findViewById(R.id.title);
        TextView date = findViewById(R.id.date);
        TextView time = findViewById(R.id.time);
        MarkdownView content = findViewById(R.id.markdown_view);

        DateTime dateTime =  new DateTime(note.getTime());
        String mer_ = dateTime.getHourOfDay()<12?"AM":"PM";

        title.setText(note.getTitle());
        date.setText(dateTime.dayOfWeek().getAsText()+" "+dateTime.monthOfYear().getAsText()+" "+dateTime.getYear());
        time.setText(dateTime.getHourOfDay() +":"+dateTime.getMinuteOfHour()+mer_);
        content.loadFromText(note.getContent());

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
