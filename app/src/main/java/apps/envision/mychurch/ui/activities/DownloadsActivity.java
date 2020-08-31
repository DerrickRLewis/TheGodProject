package apps.envision.mychurch.ui.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.DataViewModel;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.libs.downloads.DownloadWorker;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.Download;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.ui.fragments.DownloadsFragment;
import apps.envision.mychurch.utils.ImageLoader;
import apps.envision.mychurch.utils.ObjectMapper;

import static apps.envision.mychurch.libs.downloads.DownloadWorker.DOWNLOAD_ARG;


public class DownloadsActivity extends AppCompatActivity implements LocalMessageCallback {

    private static final int MY_PERMISSIONS_ACCESS_STORAGE = 1;
    public static boolean isStoragePermissionGranted = false;
    private LinearLayout container;
    private BottomSheetBehavior sheetBehavior;
    private ViewPager pager;

    private DataViewModel dataViewModel;

    private ImageView thumbnail,pause_resume;
    private TextView title,download_hint;
    private ProgressBar progressBar;

    private Download currentDownload = null;
    private boolean isDownloadsInitiated = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.downloads));
        //initialise views
        init_views();

        // Get a new or existing ViewModel from the ViewModelProvider.
        dataViewModel = ViewModelProviders.of(this).get(DataViewModel.class);

        //observe current download changes
        // The onChanged() method fires when the observed data changes and the fragment is
        // in the foreground.
        dataViewModel.observeCurrentDownload().observe(this, currentDownload -> {
            // Update the cached copy of the current download in the view.
            if(this.currentDownload == null){
                this.currentDownload = currentDownload;
            }
        });

        //if user passed a task for a media to be downloaded
        //get bundle parameter
        //if bundle is not null
        //we get the passed media file
        //first we check if the passed media file is currently downloading and return command
        //if the user is currently downloading another media file
        //since we can only download one media at a time, we notify the user
        if(getIntent().getStringExtra("media")!=null) {
            Gson gson = new Gson();
            Media media = gson.fromJson(getIntent().getStringExtra("media"), Media.class);
            Log.e("media to download",String.valueOf(media));
            if(media!=null) {
                isDownloadsInitiated = true;
                currentDownload = ObjectMapper.mapCurrentDownloadFromMedia(media);
            }
        }

        //check if permission to read file storage or store to folders is granted by the user
        //if user grants permission, load downloaded files or download requested media file
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(DownloadsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            request_permission();
        } else {
            isStoragePermissionGranted = true;
            LocalMessageManager.getInstance().send(R.id.request_storage_permission_granted);
            start_download();
        }

        if(currentDownload!=null){
            if (currentDownload.getMedia_type().equalsIgnoreCase("audio")) {
                pager.setCurrentItem(0);
            } else {
                pager.setCurrentItem(1);
            }
        }
    }

    private void init_views(){
        container = findViewById(R.id.container);
        CardView layoutBottomSheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        pager=(ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new FragmentAdapter(getSupportFragmentManager()));
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(pager);
        pager.setOffscreenPageLimit(2);

        //download progress views
        title = findViewById(R.id.title);
        download_hint = findViewById(R.id.download_hint);
        thumbnail = findViewById(R.id.thumbnail);
        pause_resume = findViewById(R.id.pause_resume);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.setIndeterminate(false);
    }

    /**
     * method to display bottom download controller
     * and set bottom margin to our container
     */
    private void show_bottom_download_viewer(){
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        sheetBehavior.setPeekHeight((int) getResources().getDimension(R.dimen.bottom_sheet_peek_height));
        setMargins(container,(int) getResources().getDimension(R.dimen.bottom_sheet_peek_height));
    }

    /**
     * method to hide bottom download viewer
     * and set bottom margin to our container
     */
    private void hide_bottom_download_viewer(){
        sheetBehavior.setPeekHeight(0);
        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        setMargins(container,0);
    }

    private void setMargins (View view, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.bottomMargin = bottom;
            view.requestLayout();
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

    /**
     * tell user why we need this permission
     */
    public void request_permission() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.request_permission_hint)
                .setMessage(R.string.request_folder_msg)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    //Prompt the user once explanation has been shown
                    ActivityCompat.requestPermissions(DownloadsActivity.this,new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_ACCESS_STORAGE);
                    dialogInterface.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .setCancelable(false)
                .create()
                .show();
    }

    /**
     * permission request result
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isStoragePermissionGranted = true;
                    LocalMessageManager.getInstance().send(R.id.request_storage_permission_granted);
                    start_download();
                }else{
                    isStoragePermissionGranted = false;
                    // permission denied
                    Toast.makeText(this,getResources().getString(R.string.permission_denied),Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalMessageManager.getInstance().addListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalMessageManager.getInstance().removeListener(this);
        dataViewModel.clearDownloads();
    }

    /**
     * @param localMessage event message
     *  subscribe to updates to ongoing download progress
     */
    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
        switch (localMessage.getId()){
            case R.id.download_progess:
                currentDownload = (Download) localMessage.getObject();
                set_current_download_view(currentDownload);
                break;
        }

    }


    public class FragmentAdapter extends FragmentPagerAdapter {

        public FragmentAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            String media_type = position == 0 ? App.getContext().getString(R.string.audio) : App.getContext().getString(R.string.video);
            return (DownloadsFragment.newInstance(media_type));
        }

        @Override
        public String getPageTitle(int position) {
            return position == 0 ? App.getContext().getString(R.string.audios) : App.getContext().getString(R.string.videos);
        }
    }

    //set the current download data
    private void set_current_download_view(Download currentDownload){
        if(currentDownload != null) {
            title.setText(currentDownload.getTitle());
            ImageLoader.loadThumbnail(thumbnail,currentDownload.getCover_photo());
            progressBar.setProgress(currentDownload.getProgress());

            if(currentDownload.getStatus() == 0){
                pause_resume.setVisibility(View.GONE);
                pause_resume.setImageDrawable(App.getContext().getDrawable(R.drawable.music_pause_button));
                download_hint.setText(App.getContext().getString(R.string.pending));
            }else if(currentDownload.getStatus() == 1){
                pause_resume.setVisibility(View.GONE);
                pause_resume.setImageDrawable(App.getContext().getDrawable(R.drawable.music_pause_button));
                download_hint.setText(String.format(App.getContext().getString(R.string.mb_format),currentDownload.getCurrent_file_size(),currentDownload.getTotal_file_size()));
                progressBar.getProgressDrawable().setColorFilter(
                        App.getContext().getResources().getColor(R.color.accent), android.graphics.PorterDuff.Mode.SRC_IN);
            }else if(currentDownload.getStatus() == 2){
                pause_resume.setVisibility(View.GONE);
                pause_resume.setImageDrawable(App.getContext().getDrawable(R.drawable.ic_refresh_white_24dp));
                download_hint.setText(String.format(App.getContext().getString(R.string.mb_format),currentDownload.getCurrent_file_size(),currentDownload.getTotal_file_size()));
            }else if(currentDownload.getStatus() == 3){
                pause_resume.setVisibility(View.GONE);
                download_hint.setText(App.getContext().getString(R.string.download_complete));
                progressBar.setProgress(100);
                progressBar.getProgressDrawable().setColorFilter(
                        App.getContext().getResources().getColor(R.color.material_blue_500), android.graphics.PorterDuff.Mode.SRC_IN);
            }else{
                pause_resume.setVisibility(View.GONE);
                pause_resume.setImageDrawable(App.getContext().getDrawable(R.drawable.ic_refresh_white_24dp));
                download_hint.setText(App.getContext().getString(R.string.download_error));
            }
        }
    }

    private void start_download(){
        //if there is no request for a new download, return
        if(PreferenceSettings.isDownloadInProgress()){
            show_bottom_download_viewer();
            return;
        }
        if (currentDownload == null){
            hide_bottom_download_viewer();
            return;
        }
        show_bottom_download_viewer();
        set_current_download_view(currentDownload);

        Gson gson = new Gson();
        String myJson = gson.toJson(currentDownload);

        // Create the Data object:
        Data myData = new Data.Builder()
                .putString(DOWNLOAD_ARG, myJson)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiresStorageNotLow(true)
                .setRequiresBatteryNotLow(true)
                .build();

        // ...then create and enqueue a OneTimeWorkRequest that uses those arguments
        OneTimeWorkRequest downloadWork = new OneTimeWorkRequest.Builder(DownloadWorker.class)
                .setInputData(myData)
                .addTag("download_task_001")
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance()
                .enqueueUniqueWork("download_task_001", ExistingWorkPolicy.REPLACE, downloadWork);

        WorkManager.getInstance().getWorkInfoByIdLiveData(downloadWork.getId())
                .observe(this, workInfo -> {
                    // Do something with the status
                    Log.e("SimpleWorkRequest: ", String.valueOf(workInfo));
                    if (workInfo != null) {

                        Log.e("SimpleWorkRequest: ", workInfo.getState().name() + "\n");
                    }
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        Log.e("SimpleWorkRequest: ", "finished executing");
                    }
                });
    }

    //cancel current download
    public void cancel_current_download_warning(View view) {
        if(currentDownload!=null && currentDownload.getProgress() <100) {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(DownloadsActivity.this);
            builder.setTitle(getString(R.string.cancel_single_download));
            builder.setMessage(getString(R.string.cancel_current_download));
            String positiveText = getString(android.R.string.ok);
            builder.setPositiveButton(positiveText,
                    (dialog, which) -> {
                        //cancel current job
                        cancel_current_job();
                    });

            String negativeText = getString(android.R.string.cancel);
            builder.setNegativeButton(negativeText,
                    (dialog, which) -> {
                        // negative button logic
                    });

            AlertDialog dialog = builder.create();
            // display dialog
            dialog.show();
        }else{
            hide_bottom_download_viewer();
        }
    }

    /**
     * method to cancel the current download job
     */
    private void cancel_current_job(){
        // positive button logic
        Toast.makeText(App.getContext(), "Current download cancelled", Toast.LENGTH_SHORT).show();
        //set a flag to clear uncompleted download, since user clicked on cancel button
        //set a flag not to show user download error, since he initiated the process
        PreferenceSettings.setIsUserInitiatedProcess(true);
        WorkManager.getInstance().cancelAllWorkByTag("download_task_001");
        PreferenceSettings.setDownloadInProgress(false);
        hide_bottom_download_viewer();
    }


}
