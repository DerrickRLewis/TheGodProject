package apps.envision.mychurch.ui.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.gson.Gson;
import com.thefinestartist.finestwebview.FinestWebView;

import org.imaginativeworld.whynotimagecarousel.CarouselItem;
import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.OnItemClickListener;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.pojo.Radio;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.ui.activities.BibleActivity;
import apps.envision.mychurch.ui.activities.DevotionalsActivity;
import apps.envision.mychurch.ui.activities.EventsActivity;
import apps.envision.mychurch.ui.activities.FlutterWavesActivity;
import apps.envision.mychurch.ui.activities.GooglePayActivity;
import apps.envision.mychurch.ui.activities.HymnsActivity;
import apps.envision.mychurch.ui.activities.LiveStreamsPlayer;
import apps.envision.mychurch.ui.activities.MessagesCategoryActivity;
import apps.envision.mychurch.ui.activities.NotesActivity;
import apps.envision.mychurch.ui.activities.PaystackActivity;
import apps.envision.mychurch.ui.activities.RadioPlayerActivity;
import apps.envision.mychurch.ui.activities.SermonsActivity;
import apps.envision.mychurch.socials.EditProfileActivity;
import apps.envision.mychurch.socials.SocialActivity;
import apps.envision.mychurch.ui.activities.StripeActivity;
import apps.envision.mychurch.utils.ImageLoader;
import apps.envision.mychurch.utils.JsonParser;
import apps.envision.mychurch.utils.MediaOptions;
import apps.envision.mychurch.utils.ObjectMapper;
import apps.envision.mychurch.utils.TimUtil;
import apps.envision.mychurch.utils.Utility;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements View.OnClickListener, LocalMessageCallback {

    private View view;
    private DataViewModel dataViewModel;
    private Radio radio;
    private Livestreams livestreams;
    private  String selected = "";
    private List<Media> sliderList = new ArrayList<>();
    private  ImageCarousel carousel;

    private ImageView image_one, image_two, image_three, image_four,
                      image_five, image_six, image_seven, image_eight;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);
        init_views();

        carousel = view.findViewById(R.id.carousel);
        view.findViewById(R.id.messages).setOnClickListener(this);
        view.findViewById(R.id.radio).setOnClickListener(this);
        view.findViewById(R.id.livestreams).setOnClickListener(this);
        view.findViewById(R.id.notes).setOnClickListener(this);
        view.findViewById(R.id.devotionals).setOnClickListener(this);
        view.findViewById(R.id.bible).setOnClickListener(this);
        view.findViewById(R.id.go_social).setOnClickListener(this);
        view.findViewById(R.id.website).setOnClickListener(this);
        view.findViewById(R.id.donate).setOnClickListener(this);
        view.findViewById(R.id.hymns).setOnClickListener(this);
        if(view.findViewById(R.id.audios)!=null)
        view.findViewById(R.id.audios).setOnClickListener(this);
        if(view.findViewById(R.id.videos)!=null)
            view.findViewById(R.id.videos).setOnClickListener(this);

        image_one = view.findViewById(R.id.image_one);
        image_two = view.findViewById(R.id.image_two);
        image_three = view.findViewById(R.id.image_three);
        image_four = view.findViewById(R.id.image_four);
        image_five = view.findViewById(R.id.image_five);
        image_six = view.findViewById(R.id.image_six);
        image_seven = view.findViewById(R.id.image_seven);
        load_images();

        // Get a new or existing ViewModel from the ViewModelProvider.
        dataViewModel = ViewModelProviders.of(this).get(DataViewModel.class);
        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        dataViewModel.getRadio().observe(this, radioList -> {
            if(radioList!=null && radioList.size()>0){
                radio = radioList.get(0);
            }
        });
        dataViewModel.getAllLiveStreams().observe(this, livestreamsList -> {
            if(livestreamsList!=null && livestreamsList.size()>0){
                livestreams = livestreamsList.get(0);
            }
        });

        init_slider();
        //fetch data from server
        discover();
        return view;
    }

    private void init_views(){
        CardView go_premium = view.findViewById(R.id.go_premium);
        go_premium.setOnClickListener(this);
        if(PreferenceSettings.isUserLoggedIn()
                && PreferenceSettings.getIsUserSubscribed()
                && TimUtil.isValidFutureDate(PreferenceSettings.getUserSubscriptionExpiryDate())){
            go_premium.setVisibility(View.GONE);
        }else{
            go_premium.setVisibility(View.GONE);
        }
    }

    private void init_slider(){
        if(carousel==null)return;

        //we observe bookmarks table, to increment bookmarks badge
        this.sliderList = PreferenceSettings.getSliderMedia();
        if(sliderList==null || sliderList.size()==0) {
            carousel.setVisibility(View.GONE);
            return;
        }
        carousel.setVisibility(View.VISIBLE);

        List<CarouselItem> list = new ArrayList<>();
        for (Media slider: sliderList) {
            list.add(
                    new CarouselItem(
                            slider.getCover_photo(),
                            slider.getTitle()
                    )
            );
        }
        carousel.addData(list);
        carousel.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onClick(int i, @NotNull CarouselItem carouselItem) {
                new MediaOptions(getActivity()).play_media(dataViewModel, sliderList, sliderList.get(i));
            }

            @Override
            public void onLongClick(int i, @NotNull CarouselItem carouselItem) {

            }
        });
    }

    private void load_images(){
        if(!PreferenceSettings.getHomePageImages("image_one").equalsIgnoreCase("")){
            ImageLoader.loadImage(image_one,PreferenceSettings.getHomePageImages("image_one"));
        }
        if(!PreferenceSettings.getHomePageImages("image_two").equalsIgnoreCase("")){
            ImageLoader.loadImage(image_two,PreferenceSettings.getHomePageImages("image_two"));
        }
        if(!PreferenceSettings.getHomePageImages("image_three").equalsIgnoreCase("")){
            ImageLoader.loadImage(image_three,PreferenceSettings.getHomePageImages("image_three"));
        }
        if(!PreferenceSettings.getHomePageImages("image_four").equalsIgnoreCase("")){
            ImageLoader.loadImage(image_four,PreferenceSettings.getHomePageImages("image_four"));
        }
        if(!PreferenceSettings.getHomePageImages("image_five").equalsIgnoreCase("")){
            ImageLoader.loadImage(image_five,PreferenceSettings.getHomePageImages("image_five"));
        }
        if(!PreferenceSettings.getHomePageImages("image_six").equalsIgnoreCase("")){
            ImageLoader.loadImage(image_six,PreferenceSettings.getHomePageImages("image_six"));
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.go_premium:
                new Utility(getActivity()).load_subscriptions();
                break;
            case R.id.hymns:
                startActivity(new Intent(getActivity(), HymnsActivity.class));
                break;
            case R.id.devotionals:
                startActivity(new Intent(getActivity(), DevotionalsActivity.class));
                break;
            case R.id.events:
                startActivity(new Intent(getActivity(), EventsActivity.class));
                break;
            case R.id.notes:
                startActivity(new Intent(getActivity(), NotesActivity.class));
                break;
            case R.id.messages:
                startActivity(new Intent(getActivity(), MessagesCategoryActivity.class));
                break;
            case R.id.bible:
                startActivity(new Intent(getActivity(), BibleActivity.class));
                break;
            case R.id.website:
                //startActivity(new Intent(getActivity(), WebsitePageActivity.class));
                new FinestWebView.Builder(getActivity()).theme(R.style.FinestWebViewTheme)
                        .titleDefault(getString(R.string.our_website))
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
                        .show(PreferenceSettings.getWebsiteURL());
                break;
            case R.id.donate:
                String[] items = getResources().getStringArray(R.array.donateOptions);
                selected = items[0];
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.donate_with);
                builder.setIcon(R.drawable.donate);
                builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        selected = items[item];
                    }
                });

                builder.setPositiveButton("Proceed",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                switch (selected){
                                    case "PayStack": default:
                                        startActivity(new Intent(getActivity(), PaystackActivity.class));
                                        break;
                                    case "FlutterWave":
                                        startActivity(new Intent(getActivity(), FlutterWavesActivity.class));
                                        break;
                                    case "Google Pay":
                                        startActivity(new Intent(getActivity(), StripeActivity.class));
                                        break;
                                }
                            }
                        });
                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                break;
            case R.id.radio:
                //startActivity(new Intent(getActivity(), MessagesCategoryActivity.class));
                if(radio!=null && !radio.getStream_url().equalsIgnoreCase("")){
                    Gson gson = new Gson();
                    String myJson = gson.toJson(ObjectMapper.mapMediaFromRadio(radio));
                    Intent intent = new Intent(getActivity(), RadioPlayerActivity.class);
                    intent.putExtra("media", myJson);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_up, R.anim.still);
                    //
                    PreferenceSettings.setRadioPlayerFlag(true);
                }else{
                    Toast.makeText(App.getContext(), getString(R.string.radio_station_not_available),Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.livestreams:
                if(livestreams!=null && !livestreams.getStream_url().equalsIgnoreCase("")){
                    Intent intent = new Intent(getActivity(), LiveStreamsPlayer.class);
                    Gson gson = new Gson();
                    String myJson = gson.toJson(livestreams);
                    intent.putExtra("livestreams", myJson);
                    startActivity(intent);
                }else{
                    Toast.makeText(App.getContext(), getString(R.string.livestreams_not_available),Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.videos:
                Intent intent = new Intent(getActivity(), SermonsActivity.class);
                intent.putExtra("media_type", "video");
                startActivity(intent);
                break;
            case R.id.audios:
                Intent intent2 = new Intent(getActivity(), SermonsActivity.class);
                intent2.putExtra("media_type", "audio");
                startActivity(intent2);
                break;
            case R.id.go_social:
                //new Utility(getActivity()).show_alert("Feature will soon be available","We are currently working on this feature to be integrated in mychurch app, we will update the project once its fully tested.");
                if(!PreferenceSettings.isUserLoggedIn() || PreferenceSettings.getUserData()==null){
                    new Utility(getActivity()).show_create_account_social_alert(getString(R.string.create_account),getString(R.string.social_login_hint));
                }else{
                    if(PreferenceSettings.getUserData().isActivated()){
                        startActivity(new Intent(getActivity(), SocialActivity.class));
                    }else{
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                        builder1.setTitle(getString(R.string.update_profile));
                        builder1.setMessage(getString(R.string.update_profile_hint));
                        builder1.setPositiveButton("OK",
                                (dialog, which) -> {
                                    // positive button logic
                                    dialog.dismiss();
                                    Intent intent3 = new Intent(getActivity(),EditProfileActivity.class);
                                    intent3.putExtra("onboarding", "true_");
                                    startActivity(intent3);
                                });
                        builder1.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

                        AlertDialog dialog = builder1.create();
                        dialog.setCancelable(false);
                        // display dialog
                        dialog.show();
                    }
                }
                 break;
        }
    }

    private void discover(){
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("media_type", 0);
            UserData userData = PreferenceSettings.getUserData();
            if(userData!=null){
                jsonData.put("email", userData.getEmail());
            }
            String requestBody = jsonData.toString();
            Log.e("requestBody",requestBody);
            NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);

            Call<String> callAsync = service.discover(requestBody);

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response", String.valueOf(response.body()));
                    if (response.body() == null) {
                        return;
                    }
                    try {
                        JSONObject jsonObj = new JSONObject(response.body());
                        String status = jsonObj.getString("status");
                        if (status.equalsIgnoreCase("ok")) {
                            dataViewModel.insertRadio(JsonParser.getRadio(jsonObj.getJSONObject("radios")));
                            dataViewModel.insertLivestream(JsonParser.getLiveStreams(jsonObj.getJSONObject("livestream")));
                            PreferenceSettings.setFacebookPage(jsonObj.getString("facebook_page"));
                            PreferenceSettings.setYoutubePage(jsonObj.getString("youtube_page"));
                            PreferenceSettings.setTwitterPage(jsonObj.getString("twitter_page"));
                            PreferenceSettings.setInstagramPage(jsonObj.getString("instagram_page"));
                            PreferenceSettings.setAdvertsInterval(jsonObj.getInt("ads_interval"));
                            PreferenceSettings.setWebsiteURL(jsonObj.getString("website_url"));
                            PreferenceSettings.setHomePageImages("image_one",jsonObj.getString("image_one"));
                            PreferenceSettings.setHomePageImages("image_two",jsonObj.getString("image_two"));
                            PreferenceSettings.setHomePageImages("image_three",jsonObj.getString("image_three"));
                            PreferenceSettings.setHomePageImages("image_four",jsonObj.getString("image_four"));
                            PreferenceSettings.setHomePageImages("image_five",jsonObj.getString("image_five"));
                            PreferenceSettings.setHomePageImages("image_six",jsonObj.getString("image_six"));
                            PreferenceSettings.setHomePageImages("image_seven",jsonObj.getString("image_seven"));
                            PreferenceSettings.setSliderMedia(JsonParser.getMedia(jsonObj.getJSONArray("slider_media")));
                            //PreferenceSettings.setHomePageImages("image_eight",jsonObj.getString("image_eight"));
                            load_images();
                            init_slider();
                        }
                    } catch (JSONException e) {
                        Log.e("Error", e.toString());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    //System.out.println(throwable);
                    Log.e("error", String.valueOf(throwable.getMessage()));
                }
            });
        }catch (JSONException e) {
            Log.e("parse error",e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalMessageManager.getInstance().addListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalMessageManager.getInstance().removeListener(this);
    }

    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
        
    }
}
