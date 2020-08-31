package apps.envision.mychurch.libs.post_worker;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.MakePosts;
import apps.envision.mychurch.pojo.Uploads;
import apps.envision.mychurch.utils.FileUtils;
import apps.envision.mychurch.utils.Utility;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ray on 12/12/2018.
 * worker class to handle download tasks
 */

public class PostWorker extends Worker {

    private CountDownLatch latch;
    private PostNotification postNotification;
    private MakePosts makePosts;
    private boolean isCancelled = false;

    public PostWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Need to wait for the download to finish or error to be encountered.
            latch = new CountDownLatch(1);
            makePosts = PreferenceSettings.getCurrentPostData();

            //init notification class
            postNotification = new PostNotification();
            //start the download process
            postNotification.init_notifications();
            startpostupload();
            //wait indefinitely till download is successfull, an error is encountered
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Result.success();
    }

    @Override
    public void onStopped() {
        //cancel download
        isCancelled = true;
        PreferenceSettings.setPostUploadInProgress(false);
        latch.countDown();
        super.onStopped();
    }


    public void startpostupload() {
        postNotification.sendNotification();
        PreferenceSettings.setPostUploadInProgress(true);
        // create upload service client
        NetworkService service = StringApiClient.createUploadServiceWithToken(NetworkService.class);
        // create list of file parts (photo, video, ...)
        List<MultipartBody.Part> parts = new ArrayList<>();
        List<Uploads> uploadsList = Utility.getUploadsMedia(makePosts.getMediaFiles());
        for (Uploads uploads: uploadsList) {
            parts.add(FileUtils.prepareFilePart("files_"+uploadsList.indexOf(uploads), Uri.parse(uploads.getUri())));
        }
        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("email", FileUtils.createPartFromString(makePosts.getEmail()));
        map.put("content", FileUtils.createPartFromString(Utility.getBase64EncodedString(makePosts.getContent())));
        map.put("visibility", FileUtils.createPartFromString(makePosts.getVisibility()));
        Log.e("body", String.valueOf(map));

        // finally, execute the request
        Call<String> call = service.make_post(parts, map);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                PreferenceSettings.setPostUploadInProgress(false);
                Log.e("response", String.valueOf(response.body()));
                if (response.body() == null) {
                    onPostError(App.getContext().getString(R.string.post_request_error));
                    return;
                }
                try {
                    JSONObject jsonObj = new JSONObject(response.body());
                    String status = jsonObj.getString("status");
                    if(status.equalsIgnoreCase("ok")){
                        PreferenceSettings.setCurrentPostData(null);
                        postNotification.onPostUploadCompleteNotification();
                        LocalMessageManager.getInstance().send(R.id.upload_request_success, App.getContext().getResources().getString(R.string.post_request_success_msg));
                        LocalMessageManager.getInstance().send(R.id.reload_posts_view);
                        latch.countDown();
                    }else{
                        String msg = jsonObj.getString("msg");
                        onPostError(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    onPostError(e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                onPostError(t.getLocalizedMessage());
            }
        });

    }

    private void onPostError(String message){
        PreferenceSettings.setPostUploadInProgress(false);
        postNotification.onPostErrorNotification(message);
        LocalMessageManager.getInstance().send(R.id.upload_request_error,message);
        latch.countDown();
    }
}
