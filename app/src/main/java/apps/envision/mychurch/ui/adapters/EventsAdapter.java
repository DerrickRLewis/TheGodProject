package apps.envision.mychurch.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import apps.envision.mychurch.R;
import apps.envision.mychurch.interfaces.EventsClickListener;
import apps.envision.mychurch.pojo.Events;
import apps.envision.mychurch.ui.viewholders.EventsViewHolder;


public class EventsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


    private ArrayList<Events> eventsLists = new ArrayList<>();
    private EventsClickListener eventsClickListener;
    private DateTime dateTime;

    public EventsAdapter(EventsClickListener eventsClickListener) {
        this.eventsClickListener = eventsClickListener;
    }

    public void setAdapter(List<Events> eventsLists) {
        this.eventsLists = new ArrayList<>();
        this.eventsLists.addAll(eventsLists);
        this.notifyDataSetChanged();
    }

    public void setDate(DateTime dateTime){
        this.dateTime = dateTime;
    }


    @Override
    public int getItemCount() {
        return eventsLists != null ? eventsLists.size() : 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int i) {
        final Events events = eventsLists.get(i);
        EventsViewHolder viewHolder = (EventsViewHolder) holder;
        viewHolder.bind(events,dateTime);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.events_list, viewGroup, false);
        final EventsViewHolder viewHolder = new EventsViewHolder(v,eventsClickListener);
        return viewHolder;
    }
}






