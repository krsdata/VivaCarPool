package au.com.vivacar.vivacarpool;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import au.com.vivacar.vivacarpool.adapter.RideListAdapter;
import au.com.vivacar.vivacarpool.utils.WebServiceConfig;

public class TestActivity extends AppCompatActivity {

    ListView rideList;
    ProgressDialog prgDialog;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        rideList = (ListView) findViewById(R.id.lstText);

                final RequestParams params = new RequestParams();

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

                /*Intent intent = new Intent(getApplicationContext(), RideListActivity.class);
                startActivity(intent);*/
        params.put("from", "Carnegie");
        // Put Http parameter password with value of Password Edit Value control
        params.put("to", "Murrumbeena");
        // Invoke RESTful Web Service with Http parameters




        invokeWS(params);

    }

    public void invokeWS(RequestParams params) {
        // Show Progress Dialog
        prgDialog.show();

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(WebServiceConfig.SEARCH_RIDES, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String responseBody) {
                prgDialog.hide();
                try {

                    JSONObject o = new JSONObject(responseBody);
                    JSONArray arr = o.getJSONArray("rides");

                    Log.i("abc", "Hello" + arr.toString());
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        Toast.makeText(getApplicationContext(), obj.getString("luggage"), Toast.LENGTH_LONG).show();

                    }

                    RideListAdapter adapter = new RideListAdapter(TestActivity.this, arr);//jArray is your json array

                    //Set the above adapter as the adapter of choice for our list
                    rideList.setAdapter(adapter);


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
