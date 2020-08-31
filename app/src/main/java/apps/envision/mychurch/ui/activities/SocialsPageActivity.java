package apps.envision.mychurch.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.ui.fragments.CategoriesFragment;
import es.dmoral.markdownview.MarkdownView;

public class SocialsPageActivity extends AppCompatActivity {

    String page = "Facebook";
    String url = "https://www.facebook.com";
    private ProgressBar spinner;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_page);
        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        WebView content = findViewById(R.id.webView);
        content.getSettings().setJavaScriptEnabled(true);
        content.setWebViewClient(new MyWebViewClient());

        if(getIntent().getStringExtra("page")!=null){
            page = getIntent().getStringExtra("page");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        switch (page){
            case "Facebook":
                getSupportActionBar().setTitle(getString(R.string.facebook_page));
                url = PreferenceSettings.geFacebookPage();
                break;
            case "Youtube":
                getSupportActionBar().setTitle(getString(R.string.youtube_page));
                url = PreferenceSettings.geYoutubePage();
                break;
            case "Twitter":
                getSupportActionBar().setTitle(getString(R.string.twitter_page));
                url = PreferenceSettings.geTwitterPage();
                break;
            case "Instagram":
                getSupportActionBar().setTitle(getString(R.string.instagram_page));
                url = PreferenceSettings.getInstagramPage();
                break;
        }
        content.loadUrl(url);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
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
