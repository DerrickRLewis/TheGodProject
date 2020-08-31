package apps.envision.mychurch.ui.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import apps.envision.mychurch.App;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.interfaces.RepliesListener;
import apps.envision.mychurch.libs.RotateLoading;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.Comments;
import apps.envision.mychurch.pojo.Replies;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.socials.UsersProfileActivity;
import apps.envision.mychurch.ui.adapters.RepliesAdapter;
import apps.envision.mychurch.utils.JsonParser;
import apps.envision.mychurch.utils.Utility;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RepliesFragment extends BottomSheetDialogFragment implements View.OnClickListener, RepliesListener {

    private  View view;
    private Comments comments;
    private int comment_position;
    private Toolbar toolbar;
    private RecyclerView recyclerview;
    private RepliesAdapter repliesAdapter;
    private ArrayList<Replies> repliesList = new ArrayList<>();
    private RelativeLayout container,bottom_sheet;
    private UserData userData;
    private AppCompatEditText comment;
    private RotateLoading rotateLoading;
    private ImageView send;

    private boolean editmode = false;
    private Replies edit_comment;
    private int edit_position = 0;
    private Utility utility;

    public RepliesFragment() {
        // Required empty public constructor
    }

    public static RepliesFragment newInstance(Comments comments, int position) {
        RepliesFragment repliesFragment = new RepliesFragment();
        Gson gson = new Gson();
        String myJson = gson.toJson(comments);
        Bundle args = new Bundle();
        args.putString("comment", myJson);
        args.putInt("position",position);
        repliesFragment.setArguments(args);
        return repliesFragment;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        // the content
        final RelativeLayout root = new RelativeLayout(getActivity());
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // creating the fullscreen dialog
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(root);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.activity_replies, container, false);
        utility = new Utility(getActivity());
        Gson gson = new Gson();
        assert getArguments() != null;
        comments = gson.fromJson(getArguments().getString("comment"), Comments.class);
        comment_position = getArguments().getInt("position");
        init_views();

        userData = PreferenceSettings.getUserData();
        if(userData==null  || userData.isBlocked()){
            hide_add_comment_layout();
        }

        //fetch comments
        load_comments(true);
        return view;
    }

    private void init_views(){
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.replies));
        toolbar.setTitleTextColor(getResources().getColor(R.color.black));
        toolbar.setNavigationIcon(R.drawable.ic_cancel_black_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            if(editmode){
                toggleEditMode();
            }else{
                dismiss();
            }
        });
        container = view.findViewById(R.id.container);
        bottom_sheet = view.findViewById(R.id.bottom_sheet);
        comment = view.findViewById(R.id.comment);
        send = view.findViewById(R.id.send);
        send.setOnClickListener(this);
        rotateLoading = view.findViewById(R.id.rotateLoading);
        recyclerview = view.findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        recyclerview.setHasFixedSize(true);
        //recyclerView.setFocusable(false);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        repliesAdapter = new RepliesAdapter(this);
        recyclerview.setAdapter(repliesAdapter);
        repliesAdapter.setHeader(comments);
        repliesAdapter.fetchLoading();
    }

    /**
     * method to hide bottom download viewer
     * and set bottom margin to our container
     */
    private void hide_add_comment_layout(){
        bottom_sheet.setVisibility(View.GONE);
        setMargins(container,0);
    }

    private void setMargins (View view, int bottom) {
        if(view==null)return;
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.bottomMargin = bottom;
            view.requestLayout();
        }
    }

    private void toggleEditMode(){
        if(editmode){
            editmode = false;
            edit_comment = null;
            toolbar.setTitle(getString(R.string.replies));
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
            comment.setText("");
            edit_position = 0;
        }else{
            toolbar.setTitle(getString(R.string.edit_reply));
            toolbar.setNavigationIcon(R.drawable.ic_undo_white_24dp);
            editmode = true;
            comment.setText(edit_comment.getContent());
        }
    }

    @Override
    public void editComment(Replies replies, int position) {
        if(!editmode) {
            this.edit_position = position;
            edit_comment = replies;
            toggleEditMode();
        }
    }

    @Override
    public void deleteComment(long id,int edit_position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.delete_comment));
        builder.setMessage(getString(R.string.delete_comment_hint));

        builder.setPositiveButton("Delete",
                (dialog, which) -> {
                    // positive button logic
                    dialog.dismiss();
                    delete_comment(id,edit_position);
                });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        // display dialog
        dialog.show();
    }

    @Override
    public void reportComment(Replies replies, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.report_comment));
        final int[] selected = {0};

        //list of items
        String[] items = getResources().getStringArray(R.array.report_comment_options);
        final boolean[] checkedItems = {false,false,false,false};
        builder.setSingleChoiceItems(items, 0,
                (dialog, which) -> {
                    // item selected logic
                    selected[0] = which;
                });

        builder.setPositiveButton(getString(R.string.report),
                (dialog, which) -> {
                    // positive button logic
                    report_comment(replies.getId(),position,items[selected[0]]);
                });

        builder.setNegativeButton( getString(android.R.string.cancel),
                (dialog, which) -> {
                    // negative button logic
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }

    @Override
    public void loadMore() {
        repliesAdapter.setLoading();
        load_comments(false);
    }

    @Override
    public boolean isUserComment(String email) {
        return userData != null && userData.getEmail().equalsIgnoreCase(email) && !userData.isBlocked();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.send:
                construct_comment();
                break;
        }
    }

    private void construct_comment(){
        String comment_ = Objects.requireNonNull(comment.getText()).toString().trim();
        if(editmode && comment_.equalsIgnoreCase(""))toggleEditMode();
        if(comment_.equalsIgnoreCase(""))return;

        try {
            show_make_comment_loader();
            JSONObject jsonData = new JSONObject();
            jsonData.put("content", Utility.getBase64EncodedString(comment_));
            jsonData.put("email", userData.getEmail());
            jsonData.put("comment", comments.getId());
            if(editmode)jsonData.put("id", edit_comment.getId());
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = editmode?service.editreply(requestBody):service.replycomment(requestBody);

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response", String.valueOf(response.body()));
                    hide_make_comment_loader();
                    if(response.body()==null){
                        construct_comment_error();
                        return;
                    }

                    try {
                        JSONObject jsonObj = new JSONObject(response.body());
                        String status = jsonObj.getString("status");
                        if (status.equalsIgnoreCase("ok")) {
                            Replies comments = JsonParser.getReply(jsonObj);
                            if(editmode){
                                repliesList.set(repliesList.indexOf(edit_comment),comments);
                                repliesAdapter.setEditedComment(comments,edit_position);
                                toggleEditMode();
                            }else{
                                repliesList.add(0,comments);
                                repliesAdapter.setNewComment(comments);
                                recyclerview.scrollToPosition(repliesList.size());
                                comment.setText("");
                                update_replies_count(jsonObj.getInt("total_count"));
                            }
                        }else{
                            Toast.makeText(App.getContext(),jsonObj.getString("message"),Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("Error", e.toString());
                        construct_comment_error();
                    }

                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    hide_make_comment_loader();
                    construct_comment_error();
                    Log.e("error", String.valueOf(throwable.getMessage()));
                }
            });
        }catch (JSONException e) {
            hide_make_comment_loader();
            construct_comment_error();
            e.printStackTrace();
        }

    }

    private void construct_comment_error(){
        if(editmode){
            Toast.makeText(App.getContext(), getString(R.string.edit_comment_error), Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(App.getContext(), getString(R.string.make_comment_error), Toast.LENGTH_SHORT).show();
        }
    }

    private void load_comments(boolean load_initial){
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("id", get_top_comment_id());
            jsonData.put("comment", comments.getId());
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = service.loadreplies(requestBody);

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response", String.valueOf(response.body()));
                    if(response.body()==null){
                        fetch_comment_error_response();
                        return;
                    }

                    try {
                        JSONObject jsonObj = new JSONObject(response.body());
                        String status = jsonObj.getString("status");
                        if (status.equalsIgnoreCase("ok")) {
                            repliesAdapter.removeLoader();

                            List<Replies> comments = JsonParser.getReplies(jsonObj);
                            if(comments.size()>0){
                                repliesList.addAll(comments);
                                repliesAdapter.setMoreAdapter(comments);
                            }
                            boolean has_more = jsonObj.getBoolean("has_more");
                            Log.e("has_more",String.valueOf(has_more));
                            if(has_more) repliesAdapter.setLoadMore(false);
                            if(load_initial)recyclerview.scrollToPosition(repliesList.size());
                            else recyclerview.scrollToPosition(comments.size() + 1);

                            update_replies_count(jsonObj.getInt("total_count"));

                        }else{
                            fetch_comment_error_response();
                        }
                    } catch (JSONException e) {
                        Log.e("Error", e.toString());
                        fetch_comment_error_response();
                    }

                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    Log.e("error", String.valueOf(throwable.getMessage()));
                    fetch_comment_error_response();
                }
            });
        }catch (JSONException e) {
            Log.e("parse error",e.getMessage());
            e.printStackTrace();
        }

    }

    private void delete_comment(long id,int edit_position){
        if(editmode)toggleEditMode();

        try {
            utility.show_loader();
            JSONObject jsonData = new JSONObject();
            jsonData.put("id", id);
            jsonData.put("comment", comments.getId());
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = service.deletereply(requestBody);

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response", String.valueOf(response.body()));
                    utility.hide_loader();
                    if(response.body()==null){
                        Toast.makeText(App.getContext(),getString(R.string.delete_comment_error),Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        JSONObject jsonObj = new JSONObject(response.body());
                        String status = jsonObj.getString("status");
                        if (status.equalsIgnoreCase("ok")) {
                            repliesAdapter.deleteComment(edit_position);
                            update_replies_count(jsonObj.getInt("total_count"));
                        }else{
                            Toast.makeText(App.getContext(),jsonObj.getString("message"),Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("Error", e.toString());
                        Toast.makeText(App.getContext(),getString(R.string.delete_comment_error),Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    Toast.makeText(App.getContext(),getString(R.string.delete_comment_error),Toast.LENGTH_SHORT).show();
                    Log.e("error", String.valueOf(throwable.getMessage()));
                    utility.hide_loader();
                }
            });
        }catch (JSONException e) {
            Toast.makeText(App.getContext(),getString(R.string.delete_comment_error),Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    private void report_comment(long id,int edit_position, String reason){
        if(editmode)toggleEditMode();

        try {
            utility.show_loader();
            JSONObject jsonData = new JSONObject();
            jsonData.put("id", id);
            jsonData.put("email", userData.getEmail());
            jsonData.put("type","replies");
            jsonData.put("reason",reason);
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = service.reportcomment(requestBody);

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response", String.valueOf(response.body()));
                    utility.hide_loader();
                    if(response.body()==null){
                        Toast.makeText(App.getContext(),getString(R.string.report_comment_error),Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        JSONObject jsonObj = new JSONObject(response.body());
                        String status = jsonObj.getString("status");
                        if (status.equalsIgnoreCase("ok")) {
                            repliesAdapter.deleteComment(edit_position);

                            update_replies_count(comments.getReplies()-1);
                        }else{
                            Toast.makeText(App.getContext(),jsonObj.getString("message"),Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("Error", e.toString());
                        Toast.makeText(App.getContext(),getString(R.string.report_comment_error),Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    Toast.makeText(App.getContext(),getString(R.string.report_comment_error),Toast.LENGTH_SHORT).show();
                    Log.e("error", String.valueOf(throwable.getMessage()));
                    utility.hide_loader();
                }
            });
        }catch (JSONException e) {
            Toast.makeText(App.getContext(),getString(R.string.report_comment_error),Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    private void fetch_comment_error_response(){
        if(repliesList.size()==0){
            repliesAdapter.removeLoader();
            repliesAdapter.setInfo(getString(R.string.no_comments));
        }else{
            repliesAdapter.setLoadMore(true);
            Toast.makeText(App.getContext(),getString(R.string.cant_load_more_comments),Toast.LENGTH_SHORT).show();
        }
    }

    private void show_make_comment_loader(){
        rotateLoading.setVisibility(View.VISIBLE);
        rotateLoading.start();
        send.setVisibility(View.GONE);
    }

    private void hide_make_comment_loader(){
        rotateLoading.setVisibility(View.GONE);
        send.setVisibility(View.VISIBLE);
    }

    private long get_top_comment_id(){
        if(repliesList.size()==0)return 0;
        Replies comments = repliesList.get(repliesList.size()-1);
        return comments.getId();
    }

    private void update_replies_count(int count){
        //we only update if the comments count changed
        if(comments.getReplies() != count){
            comments.setReplies(count);
            //notify comments activity of replies count change
            Bundle bundle = new Bundle();
            bundle.putInt("position",comment_position);
            Gson gson = new Gson();
            String myJson = gson.toJson(comments);
            bundle.putString("comment", myJson);
            LocalMessageManager.getInstance().send(R.id.update_comment_replies_count,bundle);
        }
    }

    @Override
    public void view_profile(UserData userData) {
        Intent intent;
        Gson gson = new Gson();
        String myJson = gson.toJson(userData);
        intent = new Intent(getActivity(), UsersProfileActivity.class);
        intent.putExtra("userdata", myJson);
        startActivity(intent);
    }

}
