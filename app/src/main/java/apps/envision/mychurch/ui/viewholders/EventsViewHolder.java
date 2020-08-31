package apps.envision.mychurch.ui.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.joda.time.DateTime;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.interfaces.EventsClickListener;
import apps.envision.mychurch.pojo.Events;
import apps.envision.mychurch.utils.LetterTileProvider;

public class EventsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView title,time;
    private ImageView thumbnail;
    private EventsClickListener eventsClickListener;
    private Events events;
    private LetterTileProvider letterTileProvider;

    public EventsViewHolder(View itemView, EventsClickListener eventsClickListener) {
        super(itemView);
        LinearLayout holder = itemView.findViewById(R.id.itm_holder);
        title = itemView.findViewById(R.id.title);
        thumbnail = itemView.findViewById(R.id.thumbnail);
        time = itemView.findViewById(R.id.time);

        this.eventsClickListener = eventsClickListener;
        letterTileProvider = new LetterTileProvider(App.getContext());
        holder.setOnClickListener(this);
    }

    public void bind(Events events,DateTime dateTime){
        this.events = events;
        title.setText(events.getTitle());
        time.setText(events.getTime());
        thumbnail.setImageBitmap(letterTileProvider.getLetterTile(events.getTitle()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.itm_holder:
                eventsClickListener.OnItemClick(events);
                break;
        }
    }
}
