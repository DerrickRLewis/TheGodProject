package apps.envision.mychurch.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import apps.envision.mychurch.App;
import apps.envision.mychurch.BuildConfig;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.ui.activities.LoginActivity;
import apps.envision.mychurch.ui.activities.RegisterActivity;
import apps.envision.mychurch.ui.activities.SubscriptionActivity;
import apps.envision.mychurch.utils.TimUtil;

import static apps.envision.mychurch.utils.Constants.Terms_URL;

/**
 * Fragment to load some basic app settings.
 */
public class MoreFragment extends Fragment implements View.OnClickListener{

    private View view;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    public static MoreFragment newInstance() {
       return new MoreFragment();
    }

    /**
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_more, container, false);
        init_views();
        return view;
    }

    //init views and set click listeners
    private void init_views(){
        TextView version = view.findViewById(R.id.version);
        version.setText(getResources().getString(R.string.app_name) +" "+ BuildConfig.VERSION_NAME);
        LinearLayout feedback = view.findViewById(R.id.feedback);
        LinearLayout share_app = view.findViewById(R.id.share_app);
        LinearLayout rate_app = view.findViewById(R.id.rate_app);
        LinearLayout terms = view.findViewById(R.id.terms);
        LinearLayout create_account = view.findViewById(R.id.create_account);
        LinearLayout logout = view.findViewById(R.id.logout);
        LinearLayout subscribe = view.findViewById(R.id.subscribe);
        LinearLayout app_sub = view.findViewById(R.id.app_sub);
        TextView sub_expiry_date = view.findViewById(R.id.sub_expiry_date);
        if(PreferenceSettings.getUserSubscriptionExpiryDate()!=0) {
            sub_expiry_date.setText(getString(R.string.expires_on)+" "+TimUtil.formatExpiryDate(PreferenceSettings.getUserSubscriptionExpiryDate()));
        }

        share_app.setOnClickListener(this);
        feedback.setOnClickListener(this);
        rate_app.setOnClickListener(this);
        terms.setOnClickListener(this);
        create_account.setOnClickListener(this);
        logout.setOnClickListener(this);
        subscribe.setOnClickListener(this);

        if(PreferenceSettings.isUserLoggedIn()){
            logout.setVisibility(View.VISIBLE);
            create_account.setVisibility(View.GONE);
        }else{
            logout.setVisibility(View.GONE);
            create_account.setVisibility(View.VISIBLE);
        }

        if(PreferenceSettings.getIsUserSubscribed() && TimUtil.isValidFutureDate(PreferenceSettings.getUserSubscriptionExpiryDate())){
            app_sub.setVisibility(View.VISIBLE);
            subscribe.setVisibility(View.GONE);
        }else{
            app_sub.setVisibility(View.GONE);
            subscribe.setVisibility(View.VISIBLE);
        }
    }

    //load app in playstore so user can rate the app
    private void rateApp() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + getActivity().getPackageName())));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getActivity().getPackageName())));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.feedback:
                String[] TO = {getResources().getString(R.string.app_email)};
                String[] CC = {""};
                Intent emailIntent = new Intent(Intent.ACTION_SEND);

                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                emailIntent.putExtra(Intent.EXTRA_CC, CC);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.feedback_subject));
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "There is no email client installed.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.rate_app:
                rateApp();
                break;
            case R.id.subscribe:
                startActivity(new Intent(getActivity(), SubscriptionActivity.class));
                break;
            case R.id.share_app:
                share_app();
                break;
            case R.id.terms:
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(Terms_URL));
                startActivity(i);
                break;
            case R.id.create_account:
                builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("");
                builder.setMessage(getString(R.string.what_do_you_wish_to_do));
                builder.setPositiveButton(App.getContext().getString(R.string.create_account),
                        (dialog, which) -> {
                            // positive button logic
                            dialog.dismiss();
                            startActivity(new Intent(getActivity(), RegisterActivity.class));
                        });
                builder.setNegativeButton(App.getContext().getString(R.string.login_to_app), (dialog, which) ->{
                    dialog.dismiss();
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                });

                dialog = builder.create();
                // display dialog
                dialog.show();
                break;
            case R.id.logout:
                builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.logout));
                builder.setMessage(getString(R.string.logout_hint));

                builder.setPositiveButton(getString(R.string.continue_),
                        (dialog, which) -> {
                            // positive button logic
                            dialog.dismiss();

                            //clear user details
                            PreferenceSettings.setUserData(null);
                            PreferenceSettings.setIsUserLoggedIn(false);
                            PreferenceSettings.setUserSubscriptionExpiryDate(0);
                            //clear user subscription details
                            PreferenceSettings.setIsUserSubscribed(false);
                            Objects.requireNonNull(getActivity()).recreate();
                        });
                builder.setNegativeButton(getString(R.string.cancel), (dialog, which) ->{
                    dialog.dismiss();
                });

                dialog = builder.create();
                // display dialog
                dialog.show();
                break;
    }
    }

    /**
     * method to share app link to intent
     */
    private void share_app(){
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = getResources().getString(R.string.share_app_text)
                + " http://play.google.com/store/apps/details?id=" + getActivity().getPackageName();
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareBody);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        getActivity().startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    @Override
    public void onDestroy() {
        if(dialog!=null && dialog.isShowing())dialog.dismiss();
        super.onDestroy();
    }
}

