package apps.envision.mychurch.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.gson.Gson;
import com.thefinestartist.finestwebview.FinestWebView;

import org.json.JSONException;
import org.json.JSONObject;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.db.DataViewModel;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.Livestreams;
import apps.envision.mychurch.pojo.Radio;
import apps.envision.mychurch.ui.activities.BibleActivity;
import apps.envision.mychurch.ui.activities.DevotionalsActivity;
import apps.envision.mychurch.ui.activities.EventsActivity;
import apps.envision.mychurch.ui.activities.LiveStreamsPlayer;
import apps.envision.mychurch.ui.activities.MessagesCategoryActivity;
import apps.envision.mychurch.ui.activities.NotesActivity;
import apps.envision.mychurch.ui.activities.NotesViewerActivity;
import apps.envision.mychurch.ui.activities.RadioPlayerActivity;
import apps.envision.mychurch.ui.activities.SocialsPageActivity;
import apps.envision.mychurch.ui.activities.SubscriptionActivity;
import apps.envision.mychurch.utils.JsonParser;
import apps.envision.mychurch.utils.ObjectMapper;
import apps.envision.mychurch.utils.TimUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SocialsFragment extends Fragment implements View.OnClickListener {

    public SocialsFragment() {
        // Required empty public constructor
    }

    public static SocialsFragment newInstance() {
        return new SocialsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_socials, container, false);

        view.findViewById(R.id.facebook_page).setOnClickListener(this);
        view.findViewById(R.id.twitter_page).setOnClickListener(this);
        view.findViewById(R.id.youtube_page).setOnClickListener(this);
        view.findViewById(R.id.instagram_page).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), SocialsPageActivity.class);
        switch (v.getId()){
            case R.id.facebook_page:
                //intent.putExtra("page", "Facebook");
                //startActivity(intent);
                open_website(getString(R.string.facebook_page),PreferenceSettings.geFacebookPage());
                break;
            case R.id.twitter_page:
                //intent.putExtra("page", "Twitter");
                //startActivity(intent);
                open_website(getString(R.string.twitter_page),PreferenceSettings.geTwitterPage());
                break;
            case R.id.youtube_page:
                //intent.putExtra("page", "Youtube");
                //startActivity(intent);
                open_website(getString(R.string.youtube_page),PreferenceSettings.geYoutubePage());
                break;
            case R.id.instagram_page:
                //intent.putExtra("page", "Instagram");
                //startActivity(intent);
                open_website(getString(R.string.instagram_page),PreferenceSettings.getInstagramPage());
                break;

        }
    }

    private void open_website(String title, String url){
        new FinestWebView.Builder(getActivity()).theme(R.style.FinestWebViewTheme)
                .titleDefault(title)
                .showUrl(false)
                .statusBarColorRes(R.color.primary)
                .toolbarColorRes(R.color.primary)
                .titleColorRes(R.color.finestWhite)
                .urlColorRes(R.color.colorPrimary)
                .iconDefaultColorRes(R.color.finestWhite)
                .progressBarColorRes(R.color.finestWhite)
                .stringResCopiedToClipboard(R.string.copied_to_clipboard)
                .stringResCopiedToClipboard(R.string.copied_to_clipboard)
                .stringResCopiedToClipboard(R.string.copied_to_clipboard)
                .showSwipeRefreshLayout(true)
                .swipeRefreshColorRes(R.color.darkColorPrimary)
                .menuSelector(R.drawable.selector_light_theme)
                .menuTextGravity(Gravity.CENTER)
                .menuTextPaddingRightRes(R.dimen.defaultMenuTextPaddingLeft)
                .dividerHeight(0)
                .gradientDivider(false)
                .setCustomAnimations(R.anim.slide_up, R.anim.hold, R.anim.hold, R.anim.slide_down)
                .show(url);
    }

}
