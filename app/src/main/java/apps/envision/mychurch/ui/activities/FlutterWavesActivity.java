package apps.envision.mychurch.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.pojo.UserData;
import apps.envision.mychurch.utils.Utility;
import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;
import co.paystack.android.Transaction;
import co.paystack.android.model.Card;
import co.paystack.android.model.Charge;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlutterWavesActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private UserData userDetails;
    private Utility utility;
    private AppCompatEditText amount,email,name,reason,phone;
    private String _name="", _email = "", _reason="",  _amount = "", reference = "";
    private int pos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flutterwave);

        userDetails = PreferenceSettings.getUserData();
        utility = new Utility(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        init_views();

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

        RavePayManager ravePayManager = new RavePayManager(FlutterWavesActivity.this);
        ravePayManager.setAmount(amount_);

        switch (Utility.getCurrentCountryCode().toUpperCase()){
            case "KE":
                ravePayManager.setCountry("KE");
                ravePayManager.setCurrency("KES");
                break;
            case "GH":
                ravePayManager.setCountry("GH");
                ravePayManager.setCurrency("GHS");
                break;
            case "ZA":
                ravePayManager.setCountry("ZA");
                ravePayManager.setCurrency("ZAR");
                break;
            case "TZ":
                ravePayManager.setCountry("TZ");
                ravePayManager.setCurrency("TZS");
                break;
            default:
                ravePayManager.setCountry("NG");
                ravePayManager.setCurrency("NGN");
                break;
        }

        reference = RandomStringUtils.randomAlphanumeric(12).toUpperCase();
        ravePayManager.setEmail(_email)
                .setfName(_name)
                //.setlName("lName")
                .setNarration(_reason)
                .setPublicKey(getString(R.string.flutterwave_public_key))
                .setEncryptionKey(getString(R.string.flutterwave_encryption_key))
                .setTxRef(reference)
                .setPhoneNumber(_phone)
                .acceptAccountPayments(true)
                    .acceptCardPayments(true)
                    .acceptMpesaPayments(true)
                    .acceptAchPayments(true)
                    .acceptGHMobileMoneyPayments(true)
                    .acceptUgMobileMoneyPayments(true)
                    .acceptZmMobileMoneyPayments(true)
                    .acceptRwfMobileMoneyPayments(true)
                    .acceptSaBankPayments(true)
                    .acceptUkPayments(true)
                    .acceptBankTransferPayments(true)
                    .acceptUssdPayments(true)
                    .acceptBarterPayments(true)
                    .acceptFrancMobileMoneyPayments(true)
                    .allowSaveCardFeature(true)
                    .onStagingEnv(true)
                .isPreAuth(false)
                .shouldDisplayFee(true)
                    .showStagingLabel(true)
                    .initialize();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*
         *  We advise you to do a further verification of transaction's details on your server to be
         *  sure everything checks out before providing service or goods.
         */
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            String message = data.getStringExtra("response");
            if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                //Toast.makeText(this, "SUCCESS " + message, Toast.LENGTH_SHORT).show();
                send_payment_to_server();
            }
            else if (resultCode == RavePayActivity.RESULT_ERROR) {
                Toast.makeText(this, "ERROR " + message, Toast.LENGTH_SHORT).show();
            }
            else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                Toast.makeText(this, "CANCELLED " + message, Toast.LENGTH_SHORT).show();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
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
}
