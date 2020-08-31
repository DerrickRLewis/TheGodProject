package apps.envision.mychurch.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;

import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.ui.activities.SocialsPageActivity;

public class WebsiteFragment extends Fragment {

    private ProgressBar spinner;

    public WebsiteFragment() {
        // Required empty public constructor
    }

    public static WebsiteFragment newInstance() {
        return new WebsiteFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_website, container, false);

        spinner = (ProgressBar) view.findViewById(R.id.progressBar1);
        WebView content = view.findViewById(R.id.webView);
        content.getSettings().setJavaScriptEnabled(true);
        content.setWebViewClient(new MyWebViewClient());
        content.loadUrl(PreferenceSettings.getWebsiteURL());

        return view;
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            spinner.setVisibility(View.GONE);
            view.setVisibility(View.VISIBLE);
            super.onPageFinished(view, url);

        }
    }
}
