package apps.envision.mychurch.socials;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.gson.Gson;
import com.yashoid.instacropper.InstaCropperActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.libs.post_worker.PostWorker;
import apps.envision.mychurch.pojo.MakePosts;
import apps.envision.mychurch.pojo.Uploads;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.socials.adapters.UploadsAdapter;
import apps.envision.mychurch.utils.FileManager;
import apps.envision.mychurch.utils.Utility;
import gun0912.tedimagepicker.builder.TedImagePicker;
import gun0912.tedimagepicker.builder.type.MediaType;

public class NewPostActivity extends AppCompatActivity implements UploadsAdapter.UploadsListener, LocalMessageCallback {

    private AppCompatEditText text;
    private RadioGroup visibility;
    private UserData userData;
    private Utility utility;
    private String _visibility = "";
    private List<Uploads> uploadsList = new ArrayList<>();
    private UploadsAdapter uploadsAdapter;
    private int crop_position = -1;
    private MakePosts makePosts = null;
    RadioButton _public;
    RadioButton _private;
    private MenuItem done;
    private ProgressBar my_progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        userData = PreferenceSettings.getUserData();
        utility = new Utility(this);
        makePosts = PreferenceSettings.getCurrentPostData();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.new_post);

        my_progressBar = findViewById(R.id.my_progressBar);
        visibility = findViewById(R.id.visibility);
        _public = findViewById(R.id._public);
        _private = findViewById(R.id._private);
        text = findViewById(R.id.text);
        init_attachments_view();
        set_existing_post_data();
    }

    private void  init_attachments_view(){
        RecyclerView recyclerView =findViewById(R.id.recyclerView);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        uploadsAdapter = new UploadsAdapter(this);
        recyclerView.setAdapter(uploadsAdapter);
        enable_draggable_recyclerview(recyclerView);
    }

    public void attach_file(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(NewPostActivity.this);
        builder.setTitle("Choose Media Type");
// add a list
        String[] animals = {"Images", "Videos"};
        builder.setItems(animals, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        select_file_type(MediaType.IMAGE,getString(R.string.select_images));
                        break;
                    case 1:
                        select_file_type(MediaType.VIDEO,getString(R.string.select_videos));
                        break;
                }
            }
        });
// create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void select_file_type(MediaType mediaType, String title){
        TedImagePicker.Builder tedpicker = TedImagePicker.with(this);
        tedpicker.mediaType(mediaType)
                .title(title)
                .max(10 - uploadsList.size(),getString(R.string.upload_files_size_hint));
        tedpicker.startMultiImage(uri -> {
                    for(int i=0; i<uri.size(); i++){
                        Uploads uploads = new Uploads();
                        uploads.setUri(String.valueOf(uri.get(i)));
                        File file = new File(uri.get(i).getPath());
                        uploads.setFile_path(file.getPath());
                        uploads.setLength(file.length());
                        if(mediaType == MediaType.IMAGE){
                            uploads.setFile_type("image");
                        }else{
                            uploads.setFile_type("video");
                        }
                        uploadsList.add(uploads);
                        uploadsAdapter.setAdapter(uploadsList);
                    }
                });
    }

    private void set_existing_post_data(){
        if(makePosts!=null){
            text.setText(makePosts.getContent());
            if(makePosts.getVisibility().equalsIgnoreCase("public")){
                _public.setChecked(true);
                _private.setChecked(false);
            }else{
                _private.setChecked(true);
                _public.setChecked(false);
            }
            uploadsList = Utility.getUploadsMedia(makePosts.getMediaFiles());
            uploadsAdapter.setAdapter(uploadsList);
        }
        if(PreferenceSettings.isPostUploadInProgress()){
            my_progressBar.setVisibility(View.VISIBLE);
        }else{
            my_progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_profile, menu);
        done = menu.findItem(R.id.done);
        if(PreferenceSettings.isPostUploadInProgress()){
            done.setVisible(false);
        }else{
            done.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                onBackClick();
                return true;
            case R.id.done:
                // app icon in action bar clicked; goto parent activity.
                get_input_and_submit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void get_input_and_submit(){
        if(PreferenceSettings.isPostUploadInProgress()){
            utility.show_alert(getString(R.string.in_progress),getString(R.string.processing_post_request));
            return;
        }

        String upload_check = FileManager.getFileSizeTotal(uploadsList);
        if(!upload_check.equalsIgnoreCase("")){
            utility.show_alert(getString(R.string.error),upload_check);
            return;
        }

        String _text = text.getText().toString().trim();
        int selectedRadioButtonID = visibility.getCheckedRadioButtonId();
        if (selectedRadioButtonID != -1) {
            RadioButton selectedRadioButton = (RadioButton) findViewById(selectedRadioButtonID);
            _visibility = selectedRadioButton.getText().toString();
        }

        if(_text.equalsIgnoreCase("") && uploadsList.size() == 0){
            return;
        }

        MakePosts makePosts = new MakePosts();
        makePosts.setContent(_text);
        makePosts.setEmail(userData.getEmail());
        makePosts.setVisibility(_visibility);
        Gson gson = new Gson();
        String json = gson.toJson(uploadsList);
        makePosts.setMediaFiles(json);
        PreferenceSettings.setCurrentPostData(makePosts);
        startPostWorker();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 2:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    if(crop_position != -1){
                        Uploads uploads = uploadsList.get(crop_position);
                        uploads.setUri(String.valueOf(uri));
                        File file = new File(uri.getPath());
                        uploads.setFile_path(file.getPath());
                        uploads.setLength(file.length());
                        uploadsList.set(crop_position,uploads);
                        uploadsAdapter.setAdapter(uploadsList);
                    }
                }
                return;
        }
    }

    private void startPostWorker(){
        Constraints constraints = new Constraints.Builder()
                .setRequiresStorageNotLow(true)
                .setRequiresBatteryNotLow(true)
                .build();
        // ...then create and enqueue a OneTimeWorkRequest that uses those arguments
        OneTimeWorkRequest downloadWork = new OneTimeWorkRequest.Builder(PostWorker.class)
                //.setInputData(myData)
                .addTag("upload_task_001")
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance()
                .enqueueUniqueWork("upload_task_001", ExistingWorkPolicy.REPLACE, downloadWork);

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
        done.setVisible(false);
        my_progressBar.setVisibility(View.VISIBLE);
        utility.show_alert("Request started",getString(R.string.post_request_in_progress));
    }

    private void enable_draggable_recyclerview(RecyclerView recyclerView){
        ItemTouchHelper.Callback _ithCallback = new ItemTouchHelper.Callback() {
            int dragFrom = -1;
            int dragTo = -1;
            //and in your imlpementaion of
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                // get the viewHolder's and target's positions in your adapter data, swap them
                if(viewHolder.getItemViewType() != target.getItemViewType()){
                    return false;
                }
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();


                if(dragFrom == -1) {
                    dragFrom =  fromPosition;
                }
                dragTo = toPosition;

                if(dragFrom != -1 && dragTo != -1 && dragFrom != dragTo) {
                    reallyMoved(dragFrom, dragTo);
                    dragFrom = dragTo = -1;
                }

                // and notify the adapter that its dataset has changed
                uploadsAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                Collections.swap(uploadsList, viewHolder.getAdapterPosition(), target.getAdapterPosition());
               // uploadsAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
               // uploadsAdapter.notifyItemChanged(target.getAdapterPosition());
                //nestedScrollView.requestDisallowInterceptTouchEvent(false);
                //recyclerView.setNestedScrollingEnabled(false);
                //uploadsAdapter.setItems(uploadsList);

                return true;
            }

            private void reallyMoved(int dragFrom, int dragTo) {
                if(dragFrom == 0 || dragTo == uploadsList.size()){
                    return;
                }
                Log.e("dragFrom",String.valueOf(dragFrom));
                Log.e("dragTo",String.valueOf(dragTo));
                Collections.swap(uploadsList, dragFrom, dragTo);
                uploadsAdapter.setItems(uploadsList);
                //uploadsAdapter.notifyItemChanged(dragFrom);
                //uploadsAdapter.notifyItemChanged(dragTo);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                //TODO
            }


            //defines the enabled move directions in each state (idle, swiping, dragging).
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                /*if(viewHolder.getItemViewType() == CustomAdapter.HEADERVIEW){
                    return  makeMovementFlags(0,0);
                }*/
                int dragflags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;

                return makeMovementFlags(dragflags,0);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(_ithCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void removeItem(int position) {
        uploadsList.remove(position);
        uploadsAdapter.setAdapter(uploadsList);
    }

    @Override
    public void cropItem(int position) {
        crop_position = position;
        File file = new File(Uri.parse(uploadsList.get(position).getUri()).getPath());
        Intent intent = InstaCropperActivity.getIntent(this, Uri.fromFile(file), Uri.fromFile(file), 720, 50);
        startActivityForResult(intent, 2);
    }

    @Override
    public void onBackPressed() {
        onBackClick();
    }

    private void onBackClick(){
        String _text = text.getText().toString().trim();
        if(PreferenceSettings.isPostUploadInProgress()){
            finish();
        }else if(!_text.equalsIgnoreCase("") || uploadsList.size() != 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(NewPostActivity.this);
            builder.setTitle(getString(R.string.discard_changes));
            builder.setMessage(getString(R.string.discard_changes_hint));

            builder.setPositiveButton("Ok",
                    (dialog, which) -> {
                        // positive button logic
                        dialog.dismiss();
                        finish();

                    });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            // display dialog
            dialog.show();
        }else{
            finish();
        }
    }

    @Override
    protected void onStart() {
        LocalMessageManager.getInstance().addListener(this);
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        LocalMessageManager.getInstance().removeListener(this);
        super.onDestroy();
    }

    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
        //
         if(localMessage.getId()==R.id.upload_request_error){
             String msg = (String) localMessage.getObject();
             done.setVisible(true);
             utility.show_alert(getString(R.string.error),msg);
             my_progressBar.setVisibility(View.GONE);
         }

        if(localMessage.getId()==R.id.upload_request_success){
            String msg = (String) localMessage.getObject();
            done.setVisible(true);
            text.setText("");
            uploadsList.clear();
            uploadsAdapter.setAdapter(uploadsList);
            utility.showUploadSuccessAlert(getString(R.string.success),msg);
            my_progressBar.setVisibility(View.GONE);
        }
    }
}
