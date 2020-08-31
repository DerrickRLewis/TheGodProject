package apps.envision.mychurch.ui.viewholders;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.joda.time.DateTime;

import apps.envision.mychurch.R;
import apps.envision.mychurch.interfaces.EventsClickListener;
import apps.envision.mychurch.interfaces.InboxListener;
import apps.envision.mychurch.pojo.Events;
import apps.envision.mychurch.pojo.Inbox;

public class InboxViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView title,time,MonthText,dayOfMonthText,dayOfWeekText,yearText;
    private InboxListener inboxListener;
    private Inbox inbox;

    public InboxViewHolder(View itemView, InboxListener inboxListener) {
        super(itemView);
        LinearLayout holder = itemView.findViewById(R.id.itm_holder);
        title = itemView.findViewById(R.id.title);
        MonthText = itemView.findViewById(R.id.MonthText);
        dayOfMonthText = itemView.findViewById(R.id.dayOfMonthText);
        dayOfWeekText = itemView.findViewById(R.id.dayOfWeekText);
        yearText = itemView.findViewById(R.id.yearText);
        time = itemView.findViewById(R.id.time);

        this.inboxListener = inboxListener;
        holder.setOnClickListener(this);
    }

    public void bind(Inbox inbox){
        this.inbox = inbox;
        title.setText(inbox.getTitle());

        DateTime dateTime =  new DateTime(inbox.getDate() * 1000L);
        String mer_ = dateTime.getHourOfDay()<12?"AM":"PM";
        time.setText(dateTime.getHourOfDay() +":"+dateTime.getMinuteOfHour()+mer_);
        dayOfMonthText.setText(String.valueOf(dateTime.getDayOfMonth()));
        dayOfWeekText.setText(dateTime.dayOfWeek().getAsShortText());
        MonthText.setText(dateTime.monthOfYear().getAsShortText());
        yearText.setText(String.valueOf(dateTime.getYear()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.itm_holder:
                inboxListener.OnItemClick(inbox);
                break;
        }
    }
}
