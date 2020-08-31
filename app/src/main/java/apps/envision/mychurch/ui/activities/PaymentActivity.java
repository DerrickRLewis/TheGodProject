package apps.envision.mychurch.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.libs.billing.BillingManager;
import apps.envision.mychurch.libs.billing.BillingProvider;
import apps.envision.mychurch.libs.billing.skulist.SkusAdapter;
import apps.envision.mychurch.libs.billing.skulist.row.SkuRowData;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessage;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageCallback;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.utils.Utility;

import static apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager.*;


public class PaymentActivity extends AppCompatActivity implements BillingProvider, LocalMessageCallback {

    private static final String TAG = "SubscriptionActivity";

    private RecyclerView mRecyclerView;
    private SkusAdapter mAdapter;
    private View mLoadingView;
    private TextView mErrorTextView;
    private BillingProvider mBillingProvider;
    private BillingManager mBillingManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.start_subscription));
        //initialise views
        init_views();

        // Create and initialize BillingManager which talks to BillingLibrary
        mBillingManager = new BillingManager(this);
        setWaitScreen(true);
        onManagerReady(this);
    }

    private void init_views(){
        mErrorTextView = (TextView) findViewById(R.id.error_textview);
        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mLoadingView = findViewById(R.id.screen_wait);
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
     * Notifies the fragment that billing manager is ready and provides a BillingProvider
     * instance to access it
     */
    public void onManagerReady(BillingProvider billingProvider) {
        mBillingProvider = billingProvider;
        if (mRecyclerView != null) {
            mAdapter = new SkusAdapter(mBillingProvider);
            if (mRecyclerView.getAdapter() == null) {
                mRecyclerView.setAdapter(mAdapter);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            }
        }
    }
    /**
     * Enables or disables "please wait" screen.
     */
    private void setWaitScreen(boolean set) {
        mRecyclerView.setVisibility(set ? View.GONE : View.VISIBLE);
        mLoadingView.setVisibility(set ? View.VISIBLE : View.GONE);
    }

    /**
     * Executes query for SKU details at the background thread
     */
    private void handleManagerAndUiReady() {
        final List<SkuRowData> inList = new ArrayList<>();
        SkuDetailsResponseListener responseListener = (responseCode, skuDetailsList) -> {
            Log.e(TAG, "Found sku: " + String.valueOf(responseCode));
            if (responseCode.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                // Repacking the result for an adapter
                for (SkuDetails details : skuDetailsList) {
                    //Log.e(TAG, "Found sku: " + details);
                    //Toast.makeText(App.getContext(),"onBillingSetupFinished() response:"+details,Toast.LENGTH_SHORT).show();
                    inList.add(new SkuRowData(details,details.getSku(), details.getTitle(),
                            details.getPrice(), details.getDescription(),
                            details.getType()));
                }
                if (inList.size() == 0) {
                    displayAnErrorIfNeeded();
                } else {
                    mAdapter.updateData(inList);
                    setWaitScreen(false);
                }
            }else{
                Toast.makeText(App.getContext(),"error quering billing",Toast.LENGTH_SHORT).show();
            }
        };

        // Start querying for in-app SKUs
        List<String> skus = mBillingProvider.getBillingManager().getSkus(BillingClient.SkuType.INAPP);
        mBillingProvider.getBillingManager().querySkuDetailsAsync(BillingClient.SkuType.INAPP, skus, responseListener);
    }

    private void displayAnErrorIfNeeded() {
        mLoadingView.setVisibility(View.GONE);
        mErrorTextView.setVisibility(View.VISIBLE);
        mErrorTextView.setText(getText(R.string.error_codelab_not_finished));

        // TODO: Here you will need to handle various respond codes from BillingManager
    }

    @Override
    public BillingManager getBillingManager() {
        return mBillingManager;
    }

    @Override
    protected void onStart() {
        super.onStart();
        getInstance().addListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBillingManager.destroy();
        getInstance().removeListener(this);
        PreferenceSettings.setBillingOngoing(false);
    }

    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
        if(localMessage.getId() == R.id.inapp_billing_ready){
            handleManagerAndUiReady();
        }
        if(localMessage.getId() == R.id.request_login){
            new Utility(PaymentActivity.this).show_create_account_alert();
        }
        if(localMessage.getId() == R.id.inapp_billing_completed){
            finish();
        }
    }
}
