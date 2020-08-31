package apps.envision.mychurch.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.utils.PaymentsUtil;
import apps.envision.mychurch.utils.Utility;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GooglePayActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private UserData userDetails;
    private Utility utility;
    private AppCompatEditText amount,email,name,reason,phone;
    private String _name="", _email = "", _reason="",  _amount = "", reference = "";
    private int pos = 0;

    /**
     * A client for interacting with the Google Pay API.
     *
     * @see <a
     *     href="https://developers.google.com/android/reference/com/google/android/gms/wallet/PaymentsClient">PaymentsClient</a>
     */
    private PaymentsClient mPaymentsClient;
    /**
     * A Google Pay payment button presented to the viewer for interaction.
     *
     * @see <a href="https://developers.google.com/pay/api/android/guides/brand-guidelines">Google Pay
     *     payment button brand guidelines</a>
     */
    private AppCompatButton mGooglePayButton;

    /**
     * Arbitrarily-picked constant integer you define to track a request for payment data activity.
     *
     * @value #LOAD_PAYMENT_DATA_REQUEST_CODE
     */
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;
    /**
     * Initialize the Google Pay API on creation of the activity
     *
     * @see Activity#onCreate(android.os.Bundle)
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_googlepay);

        userDetails = PreferenceSettings.getUserData();
        utility = new Utility(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        init_views();

        // Initialize a Google Pay API client for an environment suitable for testing.
        // It's recommended to create the PaymentsClient object inside of the onCreate method.
        mPaymentsClient = PaymentsUtil.createPaymentsClient(this);
        possiblyShowGooglePayButton();

    }

    private void init_views(){
        amount = findViewById(R.id.amount);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        name = findViewById(R.id.name);
        reason = findViewById(R.id.reason);


        if(userDetails!=null){
            email.setText(userDetails.getEmail());
            name.setText(userDetails.getName());
        }
        mGooglePayButton = findViewById(R.id.googlepay_button);
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
        _amount = amount.getText().toString().trim();
        _email = email.getText().toString().trim();
        _name = name.getText().toString().trim();
        _reason = reason.getText().toString().trim();
        String _phone = phone.getText().toString().trim();

        if(_amount.equalsIgnoreCase("")
                || _phone.equalsIgnoreCase("") || _reason.equalsIgnoreCase("")
                || _email.equalsIgnoreCase("") ||  _name.equalsIgnoreCase("")){
            utility.show_alert("Error",getString(R.string.fill_all_fields_hint));
            return;
        }

        int amount_ = Integer.parseInt(_amount);

        if(amount_ < 100){
            Toast.makeText(App.getContext(), R.string.invalid_amount_hint,Toast.LENGTH_LONG).show();
            return;
        }

    }



    private void send_payment_to_server(){
        utility.show_loader();
        NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("email", _email);
            jsonData.put("name", _name);
            jsonData.put("amount", _amount);
            jsonData.put("reason", _reason);
            jsonData.put("method", "flutter_waves");
            jsonData.put("reference", reference);
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
                            utility.show_alert("Operation Successful",res.getString("message"));
                        }else{
                            utility.show_alert(res.getString("status"),res.getString("message"));
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

    /**
     * Determine the viewer's ability to pay with a payment method supported by your app and display a
     * Google Pay payment button.
     *
     * @see <a href=
     *     "https://developers.google.com/android/reference/com/google/android/gms/wallet/PaymentsClient.html#isReadyToPay(com.google.android.gms.wallet.IsReadyToPayRequest)">PaymentsClient#IsReadyToPay</a>
     */
    private void possiblyShowGooglePayButton() {
        final JSONObject isReadyToPayJson = PaymentsUtil.getIsReadyToPayRequest();
        Log.e("isReadyToPayJson",String.valueOf(isReadyToPayJson));
        if (isReadyToPayJson == null) {
            return;
        }
        IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString());
        if (request == null) {
            return;
        }

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        Task<Boolean> task = mPaymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(this,
                new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            setGooglePayAvailable(task.getResult());
                        } else {
                            Log.w("isReadyToPay failed", task.getException());
                        }
                    }
                });
    }

    /**
     * If isReadyToPay returned {@code true}, show the button and hide the "checking" text. Otherwise,
     * notify the user that Google Pay is not available. Please adjust to fit in with your current
     * user flow. You are not required to explicitly let the user know if isReadyToPay returns {@code
     * false}.
     *
     * @param available isReadyToPay API response.
     */
    private void setGooglePayAvailable(boolean available) {
        if (available) {
            mGooglePayButton.setVisibility(View.VISIBLE);
        } else {
            utility.show_alert(getString(R.string.error),getString(R.string.googlepay_status_unavailable));
        }
    }

    /**
     * Handle a resolved activity from the Google Pay payment sheet.
     *
     * @param requestCode Request code originally supplied to AutoResolveHelper in requestPayment().
     * @param resultCode Result code returned by the Google Pay API.
     * @param data Intent from the Google Pay API containing payment or error data.
     * @see <a href="https://developer.android.com/training/basics/intents/result">Getting a result
     *     from an Activity</a>
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // value passed in AutoResolveHelper
            case LOAD_PAYMENT_DATA_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        handlePaymentSuccess(paymentData);
                        break;
                    case Activity.RESULT_CANCELED:
                        // Nothing to here normally - the user simply cancelled without selecting a
                        // payment method.
                        break;
                    case AutoResolveHelper.RESULT_ERROR:
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        handleError(status.getStatusCode());
                        break;
                    default:
                        // Do nothing.
                }

                // Re-enables the Google Pay payment button.
                mGooglePayButton.setClickable(true);
                break;
        }
    }

    /**
     * PaymentData response object contains the payment information, as well as any additional
     * requested information, such as billing and shipping address.
     *
     * @param paymentData A response object returned by Google after a payer approves payment.
     * @see <a
     *     href="https://developers.google.com/pay/api/android/reference/object#PaymentData">Payment
     *     Data</a>
     */
    private void handlePaymentSuccess(PaymentData paymentData) {
        String paymentInformation = paymentData.toJson();

        // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
        if (paymentInformation == null) {
            return;
        }
        JSONObject paymentMethodData;

        try {
            paymentMethodData = new JSONObject(paymentInformation).getJSONObject("paymentMethodData");
            // If the gateway is set to "example", no payment information is returned - instead, the
            // token will only consist of "examplePaymentMethodToken".
            if (paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("type")
                    .equals("PAYMENT_GATEWAY")
                    && paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("token")
                    .equals("examplePaymentMethodToken")) {
                AlertDialog alertDialog =
                        new AlertDialog.Builder(this)
                                .setTitle("Warning")
                                .setMessage(
                                        "Gateway name set to \"example\" - please modify "
                                                + "Constants.java and replace it with your own gateway.")
                                .setPositiveButton("OK", null)
                                .create();
                alertDialog.show();
            }

            String billingName =
                    paymentMethodData.getJSONObject("info").getJSONObject("billingAddress").getString("name");
            Log.d("BillingName", billingName);
            Toast.makeText(this, getString(R.string.payments_show_name, billingName), Toast.LENGTH_LONG)
                    .show();

            // Logging token string.
            Log.d("GooglePaymentToken", paymentMethodData.getJSONObject("tokenizationData").getString("token"));
        } catch (JSONException e) {
            Log.e("handlePaymentSuccess", "Error: " + e.toString());
            return;
        }
    }

    /**
     * At this stage, the user has already seen a popup informing them an error occurred. Normally,
     * only logging is required.
     *
     * @param statusCode will hold the value of any constant from CommonStatusCode or one of the
     *     WalletConstants.ERROR_CODE_* constants.
     * @see <a
     *     href="https://developers.google.com/android/reference/com/google/android/gms/wallet/WalletConstants#constant-summary">
     *     Wallet Constants Library</a>
     */
    private void handleError(int statusCode) {
        Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode));
    }

    // This method is called when the Pay with Google button is clicked.
    public void requestPayment(View view) {
        // Disables the button to prevent multiple clicks.
        mGooglePayButton.setClickable(false);

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
        String price = 1000+"";

        // TransactionInfo transaction = PaymentsUtil.createTransaction(price);
        JSONObject paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest(price);
        if (paymentDataRequestJson == null) {
            return;
        }
        PaymentDataRequest request =
                PaymentDataRequest.fromJson(paymentDataRequestJson.toString());

        // Since loadPaymentData may show the UI asking the user to select a payment method, we use
        // AutoResolveHelper to wait for the user interacting with it. Once completed,
        // onActivityResult will be called with the result.
        if (request != null) {
            AutoResolveHelper.resolveTask(
                    mPaymentsClient.loadPaymentData(request), this, LOAD_PAYMENT_DATA_REQUEST_CODE);
        }
    }
}
