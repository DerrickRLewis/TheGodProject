package apps.envision.mychurch.ui.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.libs.audio_playback.AudioPlayerService;
import apps.envision.mychurch.libs.audio_playback.PlaybackInfoListener;
import apps.envision.mychurch.libs.audio_playback.PlayerAdapter;
import apps.envision.mychurch.libs.billing.BillingManager;
import apps.envision.mychurch.libs.duonavigationdrawer.views.DuoDrawerLayout;
import apps.envision.mychurch.libs.duonavigationdrawer.views.DuoMenuView;
import apps.envision.mychurch.libs.duonavigationdrawer.widgets.DuoDrawerToggle;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.pojo.Notification;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.ui.adapters.MenuAdapter;
import apps.envision.mychurch.ui.fragments.BookmarksFragment;
import apps.envision.mychurch.ui.fragments.BranchesFragment;
import apps.envision.mychurch.ui.fragments.CategoriesFragment;
import apps.envision.mychurch.ui.fragments.EventsFragment;
import apps.envision.mychurch.ui.fragments.HomeFragment;
import apps.envision.mychurch.ui.fragments.MediaFragment;
import apps.envision.mychurch.ui.fragments.MoreFragment;
import apps.envision.mychurch.ui.fragments.MyInboxFragment;
import apps.envision.mychurch.ui.fragments.MyPlaylistFragment;
import apps.envision.mychurch.ui.fragments.PurchasedMediaFragment;
import apps.envision.mychurch.ui.fragments.SocialsFragment;
import apps.envision.mychurch.ui.fragments.TrendingMediaFragment;
import apps.envision.mychurch.ui.fragments.WebsiteFragment;
import apps.envision.mychurch.utils.ImageLoader;
import apps.envision.mychurch.utils.LetterTileProvider;
import apps.envision.mychurch.utils.Utility;

import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static apps.envision.mychurch.utils.TimUtil.getProgressPercentage;

public class MainActivity extends AppCompatActivity implements LocalMessageCallback , DuoMenuView.OnMenuClickListener{

    private ViewGroup.MarginLayoutParams container_params;
    private BottomSheetBehavior sheetBehavior;
    ImageView play_pause,albumArt;
    TextView song_title;
    private ProgressBar progressBar,bottom_progress;
    private boolean mIsBound;
    private AudioPlayerService audioPlayerService;
    private PlayerAdapter mPlayerAdapter;

    private MenuAdapter mMenuAdapter;
    private ViewHolder mViewHolder;
    private ArrayList<String> mTitles = new ArrayList<>();
    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new BillingManager(this);
        setContentView(R.layout.activity_main);
        setupNavigationMenu(savedInstanceState);
        //set bottom media controller
        setup_bottom_media_controller();
        //bind to service
        doBindService();


        mMenuAdapter.setViewSelected(0, true);
        setFragment(HomeFragment.newInstance());
        if(Utility.shouldLoadAds()){
            //loadInterstitialAd();
            //scheduleInterstitial();
        }
    }

    private void setupNavigationMenu(Bundle savedInstanceState) {
        mTitles = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.menuOptions)));

        // Initialize the views
        mViewHolder = new ViewHolder();

        // Handle toolbar actions
        handleToolbar();

        // Handle menu actions
        handleMenu();

        // Handle drawer actions
        handleDrawer();
    }


    private void set_nav_header(){
        View hView =  mViewHolder.mDuoMenuView.getHeaderView();
        TextView email = (TextView)hView.findViewById(R.id.duo_view_header_text_title);
        TextView name = (TextView)hView.findViewById(R.id.duo_view_header_text_sub_title);
        ImageView avatar = hView.findViewById(R.id.avatar);

        View fView =  mViewHolder.mDuoMenuView.getFooterView();
        Button create_account = fView.findViewById(R.id.duo_view_footer_text);
        UserData userData = PreferenceSettings.getUserData();
        if(userData!=null){
            email.setText(userData.getEmail());
            name.setText(userData.getName());
            avatar.setImageBitmap(new LetterTileProvider(this).getLetterTile(userData.getEmail()));
            create_account.setText(getString(R.string.logout_app));
        }else{
            //create_account.setVisibility(View.VISIBLE);
            email.setText(getString(R.string.guest_email));
            name.setText(getString(R.string.guest_user));
            avatar.setImageBitmap(new LetterTileProvider(this).getLetterTile("Guest User"));
            create_account.setText(getString(R.string.log_in));
        }
        create_account.setOnClickListener(view -> {
            if(userData!=null){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.logout));
                builder.setMessage(getString(R.string.logout_hint));

                builder.setPositiveButton(getString(R.string.continue_),
                        (dialog, which) -> {
                            // positive button logic
                            dialog.dismiss();
                            //clear user details
                            PreferenceSettings.setUserData(null);
                            PreferenceSettings.setIsUserLoggedIn(false);
                            recreate();
                            //finish();
                            //startActivity(getIntent());
                        });
                builder.setNegativeButton(getString(R.string.cancel), (dialog, which) ->{
                    dialog.dismiss();
                });

                AlertDialog dialog = builder.create();
                // display dialog
                dialog.show();
            }else {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.action_search){
            startActivity(new Intent(MainActivity.this,SearchActivity.class));
        }
        if(id == R.id.downloads){
            startActivity(new Intent(MainActivity.this, DownloadsActivity.class));
        }
        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    /**
     * When user clicks on the nav menu, set the selected fragment
     * @param fragment
     */
    protected void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalMessageManager.getInstance().addListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
        LocalMessageManager.getInstance().removeListener(this);
    }


    /**
     * handle local events
     * @param localMessage event message
     */
    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
        switch(localMessage.getId()){
            case R.id.song_selected:
                show_bottom_media_controller();
                initPlayerInfo();
                showLoader();
                break;
            case R.id.player_state_changed:
                if(mPlayerAdapter.getState()==PlaybackInfoListener.State.PLAYING){
                    if(progressBar.getVisibility()==View.VISIBLE)
                        hideLoader();
                }
                updatePlayingStatus();
                break;
            case R.id.stop_player:
                hide_bottom_media_controller();
                break;
            case R.id.audio_player_progress:
                if(mPlayerAdapter==null)break;
                int position = localMessage.getArg1();
                int totalDuration = mPlayerAdapter.getPlayerDuration();
                if(position>totalDuration)return;
                bottom_progress.setProgress(getProgressPercentage(position, totalDuration));
                break;
            case R.id.mainActivity_subscription_alert:
                Media media = mPlayerAdapter.getCurrentMedia();
                new Utility(MainActivity.this).show_play_subscribe_alert(media.getMedia_type());
                break;
            case R.id.recreate_mainactivity:
                recreate();
                break;
            case R.id.home_screen_notification:
                Notification notification = (Notification) localMessage.getObject();
                if(notification!=null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("");
                    // set the custom layout
                    final View customLayout = getLayoutInflater().inflate(R.layout.notification_widget, null);
                    TextView title = customLayout.findViewById(R.id.title);
                    title.setText(notification.title);
                    TextView details = customLayout.findViewById(R.id.details);
                    details.setText(notification.message);
                    builder.setView(customLayout);
                    // add a button
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // send data from the AlertDialog to the Activity
                            dialog.dismiss();
                        }
                    });
                    // create and show the alert dialog
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;
        }
    }

    /**
     * method to display bottom media controller
     * and set bottom margin to our container
     */
    private void show_bottom_media_controller(){
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        sheetBehavior.setPeekHeight((int) getResources().getDimension(R.dimen.bottom_media_peek_height));
        container_params.bottomMargin = (int) getResources().getDimension(R.dimen.bottom_media_peek_height);
    }

    /**
     * method to hide bottom media controller
     * and set bottom margin to our container
     */
    private void hide_bottom_media_controller(){
        container_params.bottomMargin = 0;
        sheetBehavior.setPeekHeight(0);
        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        recreate();
    }

    /**
     * set current player status
     */
    private void currentPlayerStatus(){
        show_bottom_media_controller();
        initPlayerInfo();
        if(mPlayerAdapter.isMediaPlayer()
                && (mPlayerAdapter.getState()== PlaybackInfoListener.State.PLAYING
                || mPlayerAdapter.getState()==PlaybackInfoListener.State.PAUSED
                || mPlayerAdapter.getState()==PlaybackInfoListener.State.RESUMED)){
            updatePlayingStatus();
        }else if(mPlayerAdapter.isMediaPlayer() && mPlayerAdapter.getState() == PlaybackInfoListener.State.LOADING){
            showLoader();
        }
    }

    /**
     * init bottom view media controller for mainactivity
     */
    private void setup_bottom_media_controller(){
        LinearLayout container = findViewById(R.id.container);
        container_params = (ViewGroup.MarginLayoutParams) container.getLayoutParams();
        CardView layoutBottomSheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        progressBar = findViewById(R.id.progressBar);
        bottom_progress = findViewById(R.id.bottom_progress);
        song_title = findViewById(R.id.title);
        play_pause = findViewById(R.id.play_pause);
        albumArt = findViewById(R.id.albumArt);

        layoutBottomSheet.setOnClickListener(view -> {
            Intent intent;
            if(!PreferenceSettings.getRadioPlayerFlag())
                intent = new Intent(MainActivity.this, AudioPlayerActivity.class);
            else
                intent = new Intent(MainActivity.this, RadioPlayerActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.still);
        });
    }

    private void updatePlayingStatus() {
        final int drawable = mPlayerAdapter.getState() != PlaybackInfoListener.State.PAUSED ? R.drawable.music_pause_button : R.drawable.music_play_button;
        play_pause.post(() -> play_pause.setImageResource(drawable));
    }

    private void showLoader(){
        progressBar.setVisibility(View.VISIBLE);
        play_pause.setVisibility(View.GONE);
    }

    private void hideLoader(){
        progressBar.setVisibility(View.GONE);
        play_pause.setVisibility(View.VISIBLE);
    }

    private void initPlayerInfo(){
        final Media currentPlayingMedia = mPlayerAdapter.getCurrentMedia();
        song_title.post(() -> song_title.setText(currentPlayingMedia.getTitle()));
        ImageLoader.loadImage(albumArt,currentPlayingMedia.getCover_photo());
    }

    public void skipPrev(@NonNull final View v) {
        if(new Utility(this).isUserBlocked()){
            return;
        }
        if (mPlayerAdapter.isMediaPlayer()) {
            mPlayerAdapter.instantReset();
            if (mPlayerAdapter.isReset()) {
                mPlayerAdapter.reset();
            }
        }
    }

    public void resumeOrPause(@NonNull final View v) {
        if(new Utility(this).isUserBlocked()){
            return;
        }
        if (mPlayerAdapter.isMediaPlayer()) {
            mPlayerAdapter.resumeOrPause();
        }
    }

    public void skipNext(@NonNull final View v) {
        if(new Utility(this).isUserBlocked()){
            return;
        }
        if (mPlayerAdapter.isMediaPlayer()) {
            mPlayerAdapter.skip(true);
        }
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(@NonNull final ComponentName componentName, @NonNull final IBinder iBinder) {
            audioPlayerService = ((AudioPlayerService.LocalBinder) iBinder).getInstance();
            mPlayerAdapter = audioPlayerService.getMediaPlayerHolder();

           /* if (mPlaybackListener == null) {
                mPlaybackListener = new PlaybackListener();
                mPlayerAdapter.setPlaybackInfoListener(mPlaybackListener);
            }*/

            if(mPlayerAdapter.isMediaPlayer()){
                currentPlayerStatus();
            }
            mIsBound = true;
        }

        @Override
        public void onServiceDisconnected(@NonNull final ComponentName componentName) {
            audioPlayerService = null;
        }
    };

    private void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(this,
                AudioPlayerService.class), mConnection, Context.BIND_AUTO_CREATE);

        final Intent startNotStickyIntent = new Intent(this, AudioPlayerService.class);
        startService(startNotStickyIntent);
    }

    private void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }


    private void handleToolbar() {
        setSupportActionBar(mViewHolder.mToolbar);
    }

    private void handleDrawer() {
        DuoDrawerToggle duoDrawerToggle = new DuoDrawerToggle(this,
                mViewHolder.mDuoDrawerLayout,
                mViewHolder.mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        mViewHolder.mDuoDrawerLayout.setDrawerListener(duoDrawerToggle);
        duoDrawerToggle.syncState();

    }

    private void handleMenu() {
        mMenuAdapter = new MenuAdapter(mTitles);
        mViewHolder.mDuoMenuView.setOnMenuClickListener(this);
        mViewHolder.mDuoMenuView.setAdapter(mMenuAdapter);
        set_nav_header();
    }

    @Override
    public void onFooterClicked() {
        //Toast.makeText(this, "onFooterClicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onHeaderClicked() {
        // Toast.makeText(this, "onHeaderClicked", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onOptionClicked(int position, Object objectClicked) {
        // Set the toolbar title
        setTitle(mTitles.get(position));
        // Set the right options selected
        mMenuAdapter.setViewSelected(position, true);

        switch (position){
            case 0:
                setFragment(HomeFragment.newInstance());
                break;
            case 1:
                setFragment(BranchesFragment.newInstance());
                break;
            case 2:
                setFragment(MyInboxFragment.newInstance());
                break;
            case 3:
                setFragment(EventsFragment.newInstance());
                break;
            case 4:
                setFragment(MyPlaylistFragment.newInstance());
                break;
            case 5:
                setFragment(BookmarksFragment.newInstance());
                break;
            case 6:
                setFragment(SocialsFragment.newInstance());
                break;
            case 7:
                setFragment(MoreFragment.newInstance());
                break;
            case 8:
                setFragment(MoreFragment.newInstance());
                break;
        }

        // Close the drawer
        mViewHolder.mDuoDrawerLayout.closeDrawer();
    }

    private class ViewHolder {
        private DuoDrawerLayout mDuoDrawerLayout;
        private DuoMenuView mDuoMenuView;
        private Toolbar mToolbar;

        ViewHolder() {
            mDuoDrawerLayout = (DuoDrawerLayout) findViewById(R.id.drawer);
            mDuoMenuView = (DuoMenuView) mDuoDrawerLayout.getMenuView();
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
        }
    }

    private void loadInterstitialAd() {
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.Main_Interstitial_AD));
        interstitialAd.loadAd(new AdRequest.Builder().build());

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                //interstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });
    }

    private void scheduleInterstitial() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> runOnUiThread(() -> {
            displayInterstitial();
            loadInterstitialAd();
        }), 5, PreferenceSettings.getAdvertsInterval(), TimeUnit.MINUTES);

    }


    private void displayInterstitial() {

        if (interstitialAd != null) {

            if (interstitialAd.isLoaded()) {
                interstitialAd.show();
            }
        }


    }
}
