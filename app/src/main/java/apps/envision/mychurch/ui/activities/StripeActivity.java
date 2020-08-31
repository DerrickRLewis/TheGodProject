package apps.envision.mychurch.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.GooglePayConfig;
import com.stripe.android.Stripe;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.model.PaymentMethodCreateParams;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.utils.Constants;
import apps.envision.mychurch.utils.Utility;
import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;
import co.paystack.android.Transaction;
import co.paystack.android.model.Card;
import co.paystack.android.model.Charge;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StripeActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private UserData userDetails;
    private Utility utility;
    private AppCompatEditText amount,email,reason;
    private int pos = 0;
    private PaymentsClient paymentsClient;
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 53;
    private Stripe stripe;

    private int amount_ = 0;
    private String _email = "";
    private String _reason = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stripe);

        userDetails = PreferenceSettings.getUserData();
        utility = new Utility(this);
        stripe = new Stripe(
                getApplicationContext(),
                getString(R.string.Stripe_publishable_key)
        );

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        init_views();

        paymentsClient = Wallet.getPaymentsClient(this,
                new Wallet.WalletOptions.Builder()
                        .setEnvironment(Constants.STRIPE.PAYMENTS_ENVIRONMENT)
                        .build());
        isReadyToPay();
    }

    private void isReadyToPay() {
        final IsReadyToPayRequest request;
        try {
            request = createIsReadyToPayRequest();
            paymentsClient.isReadyToPay(request)
                    .addOnCompleteListener(
                            new OnCompleteListener<Boolean>() {
                                public void onComplete(@NotNull Task<Boolean> task) {
                                    if (task.isSuccessful()) {
                                        // show Google Pay as payment option
                                       // Toast.makeText(App.getContext(),"Googple pay is ready",Toast.LENGTH_SHORT).show();
                                    } else {
                                        // hide Google Pay as payment option
                                        Toast.makeText(App.getContext(), R.string.google_pay_not_available,Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                            }
                    );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private IsReadyToPayRequest createIsReadyToPayRequest() throws JSONException {
        final JSONArray allowedAuthMethods = new JSONArray();
        allowedAuthMethods.put("PAN_ONLY");
        allowedAuthMethods.put("CRYPTOGRAM_3DS");

        final JSONArray allowedCardNetworks = new JSONArray();
        allowedCardNetworks.put("AMEX");
        allowedCardNetworks.put("DISCOVER");
        allowedCardNetworks.put("JCB");
        allowedCardNetworks.put("MASTERCARD");
        allowedCardNetworks.put("VISA");

        final JSONObject isReadyToPayRequestJson = new JSONObject();
        isReadyToPayRequestJson.put("allowedAuthMethods", allowedAuthMethods);
        isReadyToPayRequestJson.put("allowedCardNetworks", allowedCardNetworks);

        return IsReadyToPayRequest.fromJson(isReadyToPayRequestJson.toString());
    }

    @NonNull
    private JSONObject createPaymentDataRequest(int price) {
        final JSONObject tokenizationSpec;
        JSONObject paymentDataRequest = new JSONObject();
        try {
            tokenizationSpec = new GooglePayConfig(this).getTokenizationSpecification();
            final JSONObject cardPaymentMethod = new JSONObject()
                    .put("type", "CARD")
                    .put(
                            "parameters",
                            new JSONObject()
                                    .put("allowedAuthMethods", new JSONArray()
                                            .put("PAN_ONLY")
                                            .put("CRYPTOGRAM_3DS"))
                                    .put("allowedCardNetworks",
                                            new JSONArray()
                                                    .put("AMEX")
                                                    .put("DISCOVER")
                                                    .put("JCB")
                                                    .put("MASTERCARD")
                                                    .put("VISA"))

                                    // require billing address
                                    //.put("billingAddressRequired", true)
                                    .put(
                                            "billingAddressParameters",
                                            new JSONObject()
                                                    // require full billing address
                                                    .put("format", "FULL")

                                                    // require phone number
                                                   // .put("phoneNumberRequired", true)
                                    )
                    )
                    .put("tokenizationSpecification", tokenizationSpec);
            // create PaymentDataRequest

            paymentDataRequest.put("apiVersion", 2)
                    .put("apiVersionMinor", 0)
                    .put("allowedPaymentMethods",
                            new JSONArray().put(cardPaymentMethod))
                    .put("transactionInfo", new JSONObject()
                            .put("totalPrice", String.valueOf(price))
                            .put("totalPriceStatus", "FINAL")
                            .put("currencyCode", "USD")
                    )
                    .put("merchantInfo", new JSONObject()
                            .put("merchantName", "Example Merchant"))

                    // require email address
                    .put("emailRequired", true);

            //return PaymentDataRequest.fromJson(paymentDataRequest.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return paymentDataRequest;
    }



    private void init_views(){
        amount = findViewById(R.id.amount);
        email = findViewById(R.id.email);
        reason = findViewById(R.id.reason);

        if(userDetails!=null){
            email.setText(userDetails.getEmail());
        }
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){

        }
    }

    public void pay_now(View view){
        String _amount = amount.getText().toString().trim();
        String _email = email.getText().toString().trim();
        String _reason = reason.getText().toString().trim();

        if(_amount.equalsIgnoreCase("") || _reason.equalsIgnoreCase("")
                || _email.equalsIgnoreCase("")){
            utility.show_alert("Error",getString(R.string.fill_all_fields_hint));
            return;
        }

        amount_ = Integer.parseInt(_amount);

        if(amount_ < 0){
            Toast.makeText(App.getContext(), R.string.invalid_amount_hint,Toast.LENGTH_LONG).show();
            return;
        }

        AutoResolveHelper.resolveTask(
                paymentsClient.loadPaymentData(PaymentDataRequest.fromJson(createPaymentDataRequest(amount_).toString())),
                this,
                LOAD_PAYMENT_DATA_REQUEST_CODE
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LOAD_PAYMENT_DATA_REQUEST_CODE: {
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        if (data != null) {
                            onGooglePayResult(data);
                        }
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        // Canceled
                        break;
                    }
                    case AutoResolveHelper.RESULT_ERROR: {
                        // Log the status for debugging
                        // Generally there is no need to show an error to
                        // the user as the Google Payment API will do that
                        final Status status =
                                AutoResolveHelper.getStatusFromIntent(data);
                        break;
                    }
                    default: {
                        // Do nothing.
                    }
                }
                break;
            }
            default: {
                // Handle any other startActivityForResult calls you may have made.
            }
        }
    }

    private void onGooglePayResult(@NonNull Intent data) {
        final PaymentData paymentData = PaymentData.getFromIntent(data);
        if (paymentData == null) {
            return;
        }

        final PaymentMethodCreateParams paymentMethodCreateParams;
        try {
            paymentMethodCreateParams = PaymentMethodCreateParams.createFromGooglePay(
                    new JSONObject(paymentData.toJson()));
             stripe.createPaymentMethod(
                    paymentMethodCreateParams,
                    new ApiResultCallback<PaymentMethod>() {
                        @Override
                        public void onSuccess(@NonNull PaymentMethod result) {
                            String customerId = result.customerId;
                            send_payment_to_server(customerId, result.billingDetails.name);
                        }

                        @Override
                        public void onError(@NonNull Exception e) {
                        }
                    }
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void send_payment_to_server(String trans_ref, String name){
        utility.show_loader();
        NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("email", _email);
            jsonData.put("name", name);
            jsonData.put("amount", String.valueOf(amount_));
            jsonData.put("reason", _reason);
            jsonData.put("method", "Google Pay");
            jsonData.put("reference", trans_ref);
            String requestBody = jsonData.toString();
            Log.e("donate",requestBody);

            Call<String> callAsync = service.saveDonation(requestBody);

            callAsync.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    Log.e("response",String.valueOf(response.body()));
                    utility.hide_loader();
                    if(response.body()==null){
                        utility.show_alert("Error","Something went wrong");
                        return;
                    }
                    try {
                        JSONObject res = new JSONObject(response.body());
                        // Add Your Logic
                        if(res.getString("status").equalsIgnoreCase("ok")){
                            utility.show_alert("Operation Successful",res.getString("msg"));
                        }else{
                            utility.show_alert(res.getString("status"),res.getString("msg"));
                        }


                    }catch (Exception e){
                        e.printStackTrace();
                        Log.e("error",e.getMessage());
                        utility.show_alert("Error",e.getMessage());
                    }

                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                    //System.out.println(throwable);
                    Log.e("error",String.valueOf(throwable.getMessage()));
                    utility.hide_loader();
                    utility.show_alert("Error",getString(R.string.timeout_error));
                }
            });

        } catch (JSONException e) {
            Log.e("parse error",e.getMessage());
            e.printStackTrace();
            utility.hide_loader();
            utility.show_alert("Error",e.getMessage());
        }
    }
}
