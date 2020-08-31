package apps.envision.mychurch.ui.activities;

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

public class PaystackActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private UserData userDetails;
    private Utility utility;
    private AppCompatEditText amount,card_no,expiry,code,email,name,reason;
    private int pos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paystack);

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
        card_no = findViewById(R.id.card_no);
        expiry = findViewById(R.id.expiry);
        code = findViewById(R.id.code);
        email = findViewById(R.id.email);
        name = findViewById(R.id.name);
        reason = findViewById(R.id.reason);


        expiry.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub

                if(expiry.getText().length()==2 && pos!=3)
                {   expiry.setText(expiry.getText().toString()+"/");
                    expiry.setSelection(3);
                }

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub
                pos=expiry.getText().length();
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub

            }
        });

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
        String _amount = amount.getText().toString().trim();
        String _card_no = card_no.getText().toString().trim();
        String _expiry = expiry.getText().toString().trim();
        String _code = code.getText().toString().trim();
        String _email = email.getText().toString().trim();
        String _name = name.getText().toString().trim();
        String _reason = reason.getText().toString().trim();

        if(_amount.equalsIgnoreCase("") || _card_no.equalsIgnoreCase("") || _expiry.equalsIgnoreCase("")
        || _code.equalsIgnoreCase("")  || _reason.equalsIgnoreCase("") || _email.equalsIgnoreCase("") ||  _name.equalsIgnoreCase("")){
            utility.show_alert("Error",getString(R.string.fill_all_fields_hint));
            return;
        }

        int amount_ = Integer.parseInt(_amount);

        if(amount_ < 100){
            Toast.makeText(App.getContext(), R.string.invalid_amount_hint,Toast.LENGTH_LONG).show();
            return;
        }

        final String[] tokens = _expiry.split("/");

        if(tokens.length<2){
            utility.show_alert("Error",getString(R.string.enter_a_valid_expiry_date));
            return;
        }

        Card card = new Card(_card_no, Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), _code);
        if (card.isValid()) {
            Charge charge = new Charge();
            charge.setAmount(amount_ * 100);
            charge.setEmail(_email);
            charge.setCard(card); //sets the card to charge

            PaystackSdk.chargeCard(PaystackActivity.this, charge, new Paystack.TransactionCallback() {
                @Override
                public void onSuccess(Transaction transaction) {
                    // This is called only after transaction is deemed successful.
                    // Retrieve the transaction, and send its reference to your server
                    // for verification.
                    //utility.show_alert("Success",transaction.getReference());
                    send_payment_to_server(transaction.getReference(), _email, _name, _reason, amount_);
                }

                @Override
                public void beforeValidate(Transaction transaction) {
                    // This is called only before requesting OTP.
                    // Save reference so you may send to server. If
                    // error occurs with OTP, you should still verify on server.
                }

                @Override
                public void onError(Throwable error, Transaction transaction) {
                    //handle error here
                    utility.show_alert("Error",error.getMessage());
                }

            });
        } else {
            utility.show_alert("Error",getString(R.string.invalid_card_hint));
        }
    }

    private void send_payment_to_server(String trans_ref, String email, String name, String reason, int amount){
        utility.show_loader();
        NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("email", email);
            jsonData.put("name", name);
            jsonData.put("amount", amount);
            jsonData.put("reason", reason);
            jsonData.put("method", "paystack");
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
