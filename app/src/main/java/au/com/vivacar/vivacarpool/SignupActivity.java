package au.com.vivacar.vivacarpool;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import au.com.vivacar.vivacarpool.config.Config;
import au.com.vivacar.vivacarpool.utils.WebServiceConfig;

public class SignupActivity extends AppCompatActivity {

    private EditText name, email, password, mobile;

    private Button signUpButton;

    ProgressDialog prgDialog;

    private LoginButton fbSignUpButton;
    CallbackManager callbackManager;

    AccessTokenTracker accessTokenTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        setTitle("Register to VivaCarPool");


        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);

        final String firebaseId = pref.getString("regId", null);
        System.out.println("Firebase == " + firebaseId);

        name = (EditText) findViewById(R.id.signup_name);
        email = (EditText) findViewById(R.id.signup_email);
        mobile = (EditText) findViewById(R.id.signup_mobile);
        password = (EditText) findViewById(R.id.signup_password);

        signUpButton = (Button) findViewById(R.id.email_sign_up_button);

        final RequestParams params = new RequestParams();

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

                Date today = new Date();

                String formattedDate = sdf.format(today);
                params.put("name", name.getText().toString());
                params.put("email", email.getText().toString());
                params.put("mobile", mobile.getText().toString());
                params.put("registered_on", formattedDate);
                params.put("activated_on", formattedDate);
                params.put("last_login", formattedDate);
                params.put("status", "A");
                params.put("is_email_verified", "false");
                params.put("profile_pic", "test.jpg");
                params.put("dl_pic", "dl.jpg");
                params.put("password", password.getText().toString());

                params.put("firebaseid", firebaseId);

                attemptRegister(params);


            }
        });

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Creating Viva CarPool Account...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);


        fbSignUpButton = (LoginButton) findViewById(R.id.signup_fb_button);
        fbSignUpButton.setReadPermissions("email");
        // If using in a fragment
        callbackManager = CallbackManager.Factory.create();

        fbSignUpButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {


                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {


                                try {
                                    String email = object.getString("email");
                                    String id = object.getString("id");
                                    String name = object.getString("name");

                                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

                                    Date today = new Date();

                                    String formattedDate = sdf.format(today);

                                    params.put("name", name);
                                    params.put("email", email);
                                    params.put("mobile", "");
                                    params.put("registered_on", formattedDate);
                                    params.put("activated_on", formattedDate);
                                    params.put("last_login", formattedDate);
                                    params.put("status", "A");
                                    params.put("is_email_verified", "true");
                                    params.put("profile_pic", "test.jpg");
                                    params.put("dl_pic", "dl.jpg");
                                    params.put("password", "");
                                    params.put("firebaseid", firebaseId);

                                    SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("user_email", email);
                                    editor.commit();

                                    invokeWS(params);

                                    //String imageurl = "https://graph.facebook.com/" + id + "/picture?type=large";

                                    //  Picasso.with(LoginActivity.this).load(imageurl).into(iv_profile_pic);

                                  /*   Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                   intent.putExtra("name",name);
                                    intent.putExtra("imageurl",imageurl);
                                    startActivity(intent);*/

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });


                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();


                accessTokenTracker = new AccessTokenTracker() {
                    @Override
                    protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,

                                                               AccessToken currentAccessToken) {

                        //updateWithToken(currentAccessToken);
                       /* if (currentAccessToken == null) {
                            //tv_profile_name.setText("");
                            //iv_profile_pic.setImageResource(R.drawable.maleicon);
                        }*/
                    }
                };
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void attemptRegister(RequestParams params) {


        // Reset errors.
        name.setError(null);
        email.setError(null);
        password.setError(null);
        mobile.setError(null);

        // Store values at the time of the login attempt.
        String emailVal = email.getText().toString();
        String passwordVal = password.getText().toString();
        String nameVal = name.getText().toString();
        String mobileVal = mobile.getText().toString();


        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(nameVal)) {
            name.setError("Name should not be empty");
            focusView = name;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(passwordVal) || !isPasswordValid(passwordVal)) {
            password.setError(getString(R.string.error_invalid_password));
            focusView = password;
            cancel = true;
        }else if (TextUtils.isEmpty(emailVal)) {
            email.setError(getString(R.string.error_field_required));
            focusView = email;
            cancel = true;
        } else if (!isEmailValid(emailVal)) {
            email.setError(getString(R.string.error_invalid_email));
            focusView = email;
            cancel = true;
        }else if (TextUtils.isEmpty(mobileVal) || !isMobileValid(mobileVal)) {
            mobile.setError("Not a Valid Mobile Number");
            focusView = mobile;
            cancel = true;
        }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

            invokeWS(params);

        }
    }

    private boolean isEmailValid(String emailVal) {
        //TODO: Replace this with your own logic
        return emailVal.contains("@") && emailVal.contains(".");
    }

    private boolean isPasswordValid(String passwordVal) {
        //TODO: Replace this with your own logic
        return passwordVal.length() > 4;
    }
    private boolean isMobileValid(String mobile) {
        //TODO: Replace this with your own logic
        return mobile.length() > 9;
    }

    public void invokeWS(RequestParams params){

        prgDialog.show();

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(WebServiceConfig.SIGNUP,params ,new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String responseBody) {
                prgDialog.hide();
                try {

                    JSONObject obj = new JSONObject(responseBody);
                    System.out.println(obj);

                    if(obj.getBoolean("success")){
                        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("user_email", email.getText().toString());
                        editor.commit();


                        Toast.makeText(getApplicationContext(), "You are successfully logged in!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), PaymentActvity.class);
                        startActivity(intent);

                        finish();
                    }

                    else{

                        Toast.makeText(getApplicationContext(), obj.getString("msg"), Toast.LENGTH_LONG).show();
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
