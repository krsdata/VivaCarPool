package au.com.vivacar.vivacarpool;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import au.com.vivacar.vivacarpool.adapter.RideListAdapter;
import au.com.vivacar.vivacarpool.utils.WebServiceConfig;

public class RideListActivity extends AppCompatActivity {

    ListView rideList;
    ProgressDialog prgDialog;
    LinearLayout rideListLayout, noRideLayout;

    //RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_list);

        setTitle("Search Rides");

       /* recyclerView= (RecyclerView) findViewById(R.id.my_recycler_view);

        RecyclerAdapter adapter=new RecyclerAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));*/

        String from = getIntent().getStringExtra("from");
        String to = getIntent().getStringExtra("to");
        String journeyDate = getIntent().getStringExtra("journeydate");
        String leavingAfter = getIntent().getStringExtra("leavingafter");

        rideList = (ListView) findViewById(R.id.rides_list);

        rideListLayout = (LinearLayout) findViewById(R.id.rides_list_layout);
        noRideLayout = (LinearLayout) findViewById(R.id.rides_list_empty_layout);

        noRideLayout.setVisibility(View.INVISIBLE);

        final RequestParams params = new RequestParams();

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);


        System.out.println("Date == " + journeyDate);
        params.put("from", from);
        params.put("to", to);
        /*if(journeyDate.equalsIgnoreCase(" ") || journeyDate == null || journeyDate.equalsIgnoreCase("")){
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            Date today = new Date();

            String formattedDate = sdf.format(today);
            System.out.println("In if == " + formattedDate);
            params.put("date",formattedDate);
        }
        else{
            System.out.println("In Else - " + journeyDate);
            params.put("date",journeyDate);
        }*/
        params.put("date",journeyDate);
        if(leavingAfter==null || leavingAfter.isEmpty()) {
            params.put("time", "00:00");
        }else{
            params.put("time",leavingAfter);
        }
        invokeWS(params);


    }

    public void invokeWS(RequestParams params) {
        // Show Progress Dialog
        prgDialog.show();

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(WebServiceConfig.SEARCH_RIDES, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String responseBody) {
                prgDialog.dismiss();
                try {

                    JSONObject o = new JSONObject(responseBody);
                    System.out.println("Resp -- " + o.toString());

                    if(new Boolean(o.getString("success"))){
                        JSONArray arr = o.getJSONArray("rides");
                        if(arr.length()>0) {
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = arr.getJSONObject(i);
                            }

                            RideListAdapter adapter = new RideListAdapter(RideListActivity.this, arr);
                            rideList.setAdapter(adapter);
                        }
                    }
                    else{
                        noRideLayout.setVisibility(View.VISIBLE);
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
