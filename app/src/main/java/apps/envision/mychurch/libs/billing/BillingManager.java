/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package apps.envision.mychurch.libs.billing;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.utils.Constants;
import apps.envision.mychurch.utils.TimUtil;
import apps.envision.mychurch.utils.Utility;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * (via Billing library), maintain connection to it through BillingClient and cache
 * temporary states/data if needed.
 */
public class BillingManager implements PurchasesUpdatedListener {
    private static final String TAG = "BillingManager";

    private final BillingClient mBillingClient;
    private final Activity mActivity;

    public BillingManager(Activity activity) {
        mActivity = activity;
        mBillingClient = BillingClient.newBuilder(mActivity).setListener(this).enablePendingPurchases().build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    Log.e(TAG, "onBillingSetupFinished() response: " + billingResult.getResponseCode());
                    LocalMessageManager.getInstance().send(R.id.inapp_billing_ready);
                    //if user does not have an active subscription plan stored in shared preferences
                    //we verify if user have an active subscription
                    queryPurchases(billingResult);
                } else {
                    Log.e(TAG, "onBillingSetupFinished() error code: " + billingResult.getResponseCode());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {

            }
        });
    }

    private void queryPurchases(BillingResult billingResult) {
        Runnable queryToExecute = () -> {
            Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP);

            if (purchasesResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                return;
            }
            onPurchasesUpdated(billingResult,purchasesResult.getPurchasesList());
        };

        startServiceConnectionIfNeeded(queryToExecute);
    }

    private static final HashMap<String, List<String>> SKUS;
    static
    {
        SKUS = new HashMap<>();
        SKUS.put(BillingClient.SkuType.SUBS, Arrays.asList(
                Constants.BILLING.ONE_WEEK_SUB,
                Constants.BILLING.ONE_MONTH_SUB,
                Constants.BILLING.THREE_MONTHS_SUB,
                Constants.BILLING.SIX_MONTHS_SUB,
                Constants.BILLING.ONE_YEAR_SUB));
    }

    public List<String> getSkus(@BillingClient.SkuType String type) {
        return SKUS.get(type);
    }

    public void querySkuDetailsAsync(@BillingClient.SkuType final String itemType,
                                     final List<String> skuList, final SkuDetailsResponseListener listener) {
        // Specify a runnable to start when connection to Billing client is established
        Runnable executeOnConnectedService = () -> {
            SkuDetailsParams skuDetailsParams = SkuDetailsParams.newBuilder()
                    .setSkusList(skuList).setType(itemType).build();
            mBillingClient.querySkuDetailsAsync(skuDetailsParams, new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> list) {
                    listener.onSkuDetailsResponse(billingResult, list);
                }
            });
        };

        // If Billing client was disconnected, we retry 1 time
        // and if success, execute the query
        startServiceConnectionIfNeeded(executeOnConnectedService);
    }


    private void startServiceConnectionIfNeeded(final Runnable executeOnSuccess) {
        if (mBillingClient.isReady()) {
            if (executeOnSuccess != null) {
                executeOnSuccess.run();
            }
        } else {
            mBillingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        Log.i(TAG, "onBillingSetupFinished() response: " + billingResult.getResponseCode());
                        if (executeOnSuccess != null) {
                            executeOnSuccess.run();
                        }
                    } else {
                        Log.w(TAG, "onBillingSetupFinished() error code: " + billingResult.getResponseCode());
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {

                }
            });
        }
    }

    public void startPurchaseFlow(final SkuDetails skuDetails) {
        // Specify a runnable to start when connection to Billing client is established
        Runnable executeOnConnectedService = new Runnable() {
            @Override
            public void run() {
                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        //.setType(billingType)
                        .setSkuDetails(skuDetails)
                        .build();
                mBillingClient.launchBillingFlow(mActivity, billingFlowParams);
            }
        };

        // If Billing client was disconnected, we retry 1 time
        // and if success, execute the query
        startServiceConnectionIfNeeded(executeOnConnectedService);
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if(billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK
                || purchases==null || purchases.size()==0){
            if(PreferenceSettings.getBillingOngoing()) {
                PreferenceSettings.setUserSubscriptionExpiryDate(0);
                PreferenceSettings.setIsUserSubscribed(false);
                PreferenceSettings.setBillingOngoing(false);
            }
            return;
        }
        //we can only offer premium services to logged in users
        //so we check user login status and return if user is not logged in
        if(!PreferenceSettings.isUserLoggedIn()){
            return;
        }
        Purchase purchase = purchases.get(0);
        Log.d(TAG, String.valueOf(purchase.getPurchaseTime()));
        Log.d(TAG, String.valueOf(purchase.getSku()));
        verify_user_subscription(purchase.getPurchaseToken(),purchase.getSku());
    }

    private void verify_user_subscription(String token,String sku){
        UserData userData = PreferenceSettings.getUserData();
        if(userData==null){
            return;
        }

        NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("email", userData.getEmail());
            jsonData.put("token", token);
            jsonData.put("plan", sku);
            jsonData.put("packageName", mActivity.getPackageName());
            String requestBody = jsonData.toString();
            Log.e("verify requestBody",requestBody);

            Call<String> callAsync = service.verifyUserSubscription(requestBody);

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("verify response",String.valueOf(response.body()));
                    if(response.body()==null){
                        billing_error_alert();
                        return;
                    }

                    try {
                        JSONObject res = new JSONObject(response.body());
                        if(res.getString("status").equalsIgnoreCase("ok")){
                            Utility.setUserBillingData(mActivity,sku,res.getLong("expiry_date"));
                        }else{
                            billing_error_alert();
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                        billing_error_alert();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    //System.out.println(throwable);
                    Log.e("error",String.valueOf(throwable.getMessage()));
                    billing_error_alert();
                }
            });

        } catch (JSONException e) {
            Log.e("billing error",e.getMessage());
            billing_error_alert();
        }
    }

    //if user initiated the payment process
    //show error alert to user if we couldnt verify the subscription
    private void billing_error_alert(){
        if(PreferenceSettings.getBillingOngoing()){
            new Utility(mActivity).show_alert(App.getContext().getString(R.string.sub_not_successful),App.getContext().getString(R.string.sub_verify_error));
            PreferenceSettings.setBillingOngoing(false);
        }
    }


    public void destroy() {
        mBillingClient.endConnection();
    }
}
