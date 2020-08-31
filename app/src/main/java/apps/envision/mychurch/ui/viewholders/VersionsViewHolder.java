package apps.envision.mychurch.ui.viewholders;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import org.joda.time.DateTime;

import java.util.Locale;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.interfaces.EventsClickListener;
import apps.envision.mychurch.interfaces.VersionsClickListener;
import apps.envision.mychurch.pojo.BibleDownload;
import apps.envision.mychurch.pojo.BibleVersions;
import apps.envision.mychurch.pojo.Events;

public class VersionsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView name,description,progress;
    private AppCompatButton cancel,download;
    private VersionsClickListener versionsClickListener;
    private BibleVersions bibleVersions;

    public VersionsViewHolder(View itemView, VersionsClickListener versionsClickListener) {
        super(itemView);
        LinearLayout holder = itemView.findViewById(R.id.itm_holder);
        name = itemView.findViewById(R.id.name);
        description = itemView.findViewById(R.id.description);
        progress = itemView.findViewById(R.id.progress);
        cancel = itemView.findViewById(R.id.cancel);
        download = itemView.findViewById(R.id.download);
        download.setOnClickListener(this);
        cancel.setOnClickListener(this);
        this.versionsClickListener = versionsClickListener;
    }

    public void bind(BibleVersions bibleVersions){
        this.bibleVersions = bibleVersions;
        name.setText(bibleVersions.getTitle());
        description.setText(bibleVersions.getDescription());
        BibleDownload bibleDownload = PreferenceSettings.getCurrentBibleDownload(bibleVersions.getBook());
        if(bibleDownload==null){
            download.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
        }else{
            if(bibleDownload.isCompleted()){
                progress.setVisibility(View.GONE);
                if(bibleDownload.isProcessing()){
                    progress.setText(App.getContext().getString(R.string.just_a_second));
                    progress.setVisibility(View.VISIBLE);
                }
                download.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.GONE);
                download.setText("Downloaded");
                download.setClickable(false);
                download.setTextColor(App.getContext().getResources().getColor(R.color.material_blue_grey_300));
            }else if(bibleDownload.isOngoing()){
                download.setVisibility(View.GONE);
                cancel.setVisibility(View.VISIBLE);
                progress.setVisibility(View.VISIBLE);
                progress.setText(String.format(Locale.getDefault(),App.getContext().getResources().getString(R.string.downloaded_progress),bibleDownload.getProgress(),100));
            }else{
                download.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
            }

        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.download:
                versionsClickListener.downloadVersion(bibleVersions);
                break;
            case R.id.cancel:
                BibleDownload bibleDownload = PreferenceSettings.getCurrentBibleDownload(bibleVersions.getBook());
                versionsClickListener.cancelDownload(bibleDownload.getDownload_id(), bibleDownload.getBook());
                break;
        }
    }
}
