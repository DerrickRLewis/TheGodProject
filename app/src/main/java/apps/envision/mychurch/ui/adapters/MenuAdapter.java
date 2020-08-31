package apps.envision.mychurch.ui.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import apps.envision.mychurch.libs.duonavigationdrawer.views.DuoOptionView;
import java.util.ArrayList;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;

/**
 * Created by PSD on 13-04-17.
 */

public class MenuAdapter extends BaseAdapter {
    private ArrayList<String> mOptions = new ArrayList<>();
    private ArrayList<DuoOptionView> mOptionViews = new ArrayList<>();

    public MenuAdapter(ArrayList<String> options) {
        mOptions = options;
    }

    @Override
    public int getCount() {
        return mOptions.size();
    }

    @Override
    public Object getItem(int position) {
        return mOptions.get(position);
    }

    public void setViewSelected(int position, boolean selected) {

        // Looping through the options in the menu
        // Selecting the chosen option
        for (int i = 0; i < mOptionViews.size(); i++) {
            if (i == position) {
                mOptionViews.get(i).setSelected(selected);
            } else {
                mOptionViews.get(i).setSelected(!selected);
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String option = mOptions.get(position);

        // Using the DuoOptionView to easily recreate the demo
        final DuoOptionView optionView;
        if (convertView == null) {
            optionView = new DuoOptionView(parent.getContext());
        } else {
            optionView = (DuoOptionView) convertView;
        }

        // Using the DuoOptionView's default selectors

        switch (position){
            case 0: default:
                optionView.bind(option, App.getContext().getDrawable(R.drawable.home),false);
                break;
            case 1:
                optionView.bind(option, App.getContext().getDrawable(R.drawable.ic_location_24dp),false);
                break;
            case 2:
                optionView.bind(option, App.getContext().getDrawable(R.drawable.inbox),false);
                break;
            case 3:
                optionView.bind(option, App.getContext().getDrawable(R.drawable.events_cal),false);
                break;
            case 4:
                optionView.bind(option, App.getContext().getDrawable(R.drawable.playlists),false);
                break;
            case 5:
                optionView.bind(option, App.getContext().getDrawable(R.drawable.bookmarks),false);
                break;
            case 6:
                optionView.bind(option, App.getContext().getDrawable(R.drawable.socials),false);
                break;
            case 7:
                optionView.bind(option, App.getContext().getDrawable(R.drawable.more),false);
                break;
            case 8:
                optionView.bind(option, App.getContext().getDrawable(R.drawable.home),false);
                break;
        }

        // Adding the views to an array list to handle view selection
        mOptionViews.add(optionView);

        return optionView;
    }
}
