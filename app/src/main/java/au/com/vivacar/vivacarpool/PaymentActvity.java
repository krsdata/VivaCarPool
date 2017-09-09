package au.com.vivacar.vivacarpool;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.exception.AuthenticationException;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

import org.json.JSONException;
import org.json.JSONObject;

import au.com.vivacar.vivacarpool.config.Config;
import au.com.vivacar.vivacarpool.utils.WebServiceConfig;

public class PaymentActvity extends AppCompatActivity {

    CardInputWidget card1;
    ProgressDialog prgDialog;
    String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        card1 = (CardInputWidget)findViewById(R.id.card_input_widget);
        Button pay = (Button)findViewById(R.id.pay_button);

        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        userEmail = pref.getString("user_email",null);


        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Updating Card details to your account");
        // Set Cancelable as False
        prgDialog.setCancelable(false);


        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Card card2 = card1.getCard();
                if (card2 == null) {
                    // Do not continue token creation.
                }
                else{

                    if(card2.validateNumber()){
                        if(card2.validateExpiryDate()){
                            if(card2.validateCVC()){
                                if(card2.validateCard()){
                                    prgDialog.show();


                                    Stripe stripe = null;
                                    try {
                                        stripe = new Stripe(getApplicationContext(), "pk_test_PNhaItlFTR2C0eZ2t1r1q9RW");
                                    } catch (AuthenticationException e) {
                                        e.printStackTrace();
                                    }
                                    stripe.createToken(
                                            card2,
                                            new TokenCallback() {
                                                public void onSuccess(Token token) {
                                                    prgDialog.hide();

                                                    final RequestParams params = new RequestParams();
                                                    params.put("token", token.getId());
                                                    params.put("email", userEmail);

                                                    invokeWS(params);

                                                }
                                                public void onError(Exception error) {
                                                    // Show localized error message
                                                    error.printStackTrace();
                                                }
                                            }
                                    );
                                }
                                else{
                                    Toast.makeText(getApplicationContext(),"This is not a valid card" , Toast.LENGTH_SHORT).show();
                                }
                            }
                            else{
                                Toast.makeText(getApplicationContext(),"This cvc number is not valid" , Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"This expiry date is not valid" , Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"This is not a valid card number" , Toast.LENGTH_SHORT).show();
                    }

                }

            }
        });
    }

    public void invokeWS(RequestParams params){

        prgDialog.show();

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(WebServiceConfig.UPDATE_TOKEN,params ,new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String responseBody) {
                prgDialog.hide();
                try {

                    JSONObject obj = new JSONObject(responseBody);
                    System.out.println(obj);
                    if(obj.getBoolean("success")){
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);
                        finish();

                    }
                    else{

                    }



                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
            }

            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {

                // Hide Progress Dialog
                prgDialog.hide();
                // When Http response code is '404'
                if(statusCode == 404){
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if(statusCode == 500){
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else{
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }


        });
    }
}
