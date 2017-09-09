package au.com.vivacar.vivacarpool;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import au.com.vivacar.vivacarpool.adapter.NotificationsListAdapter;
import au.com.vivacar.vivacarpool.adapter.RideListAdapter;
import au.com.vivacar.vivacarpool.config.Config;
import au.com.vivacar.vivacarpool.utils.WebServiceConfig;


public class NotificationActivity extends AppCompatActivity {

    ProgressDialog prgDialog;
    ListView notificationsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_with_image_and_text);

        setTitle("Notifications");

        SharedPreferences pref = getSharedPreferences(Config.SHARED_PREF, 0);
        final String userEmail = pref.getString("user_email",null);

        notificationsList = (ListView) findViewById(R.id.list_notifications);

        final RequestParams params = new RequestParams();

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        params.put("email", userEmail);
        getNotificationsWS(params);

    }

    public void getNotificationsWS(RequestParams params) {
        // Show Progress Dialog
        prgDialog.show();

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(WebServiceConfig.GET_NOTIFICATIONS, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String responseBody) {
                prgDialog.dismiss();
                try {

                    JSONObject o = new JSONObject(responseBody);
                    System.out.println("Resp -- " + o.toString());

                    if(new Boolean(o.getString("success"))){
                        JSONArray arr = o.getJSONArray("notifications");
                        if(arr.length()>0) {
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = arr.getJSONObject(i);
                            }

                            NotificationsListAdapter adapter = new NotificationsListAdapter(NotificationActivity.this, arr);
                            notificationsList.setAdapter(adapter);
                        }
                    }
                    else{
                       // noRideLayout.setVisibility(View.VISIBLE);
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
                prgDialog.dismiss();
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }

            }


        });

    }
}
