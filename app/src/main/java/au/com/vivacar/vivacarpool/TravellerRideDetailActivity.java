package au.com.vivacar.vivacarpool;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;

import au.com.vivacar.vivacarpool.config.Config;
import au.com.vivacar.vivacarpool.utils.DateTimeUtil;
import au.com.vivacar.vivacarpool.utils.WebServiceConfig;

public class TravellerRideDetailActivity extends AppCompatActivity {


    ProgressDialog prgDialog;
    Button bookRide, contactOwner;

    TextView postedBy, postedOn, from, to, fromDateTime, seats, price, payBy, ownerComments;
    TextView carModel, carNumber, carColor, ac, music, pets, luggage;
    LinearLayout stopOverLayout;
    FlexboxLayout flexLayout;

    String email="";
    String rideId= "";
    String seatsToBeBooked="1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traveller_ride_detail);

        rideId = getIntent().getStringExtra("rideid");
        System.out.println("Ride id = "+rideId);

        setTitle("Ride Detail - " + rideId);

        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        email = pref.getString("user_email",null);

        System.out.println("Logged in user id = "+ email);

        postedBy = (TextView) findViewById(R.id.traveller_det_postedby);
        postedOn = (TextView) findViewById(R.id.traveller_det_posteddate);
        from = (TextView) findViewById(R.id.traveller_det_from);
        fromDateTime = (TextView) findViewById(R.id.traveller_det_fromdate);
        to = (TextView) findViewById(R.id.traveller_det_to);
        seats = (TextView) findViewById(R.id.traveller_det_seats);
        price = (TextView) findViewById(R.id.traveller_det_price);
        payBy = (TextView) findViewById(R.id.traveller_det_payby);
        ownerComments = (TextView) findViewById(R.id.traveller_det_ownercomments);
        carModel = (TextView) findViewById(R.id.traveller_det_carmodel);
        carColor = (TextView) findViewById(R.id.traveller_det_carcolor);
        carNumber = (TextView) findViewById(R.id.traveller_det_carnumber);
        ac = (TextView) findViewById(R.id.traveller_det_ac);
        music = (TextView) findViewById(R.id.traveller_det_music);
        pets = (TextView) findViewById(R.id.traveller_det_pets);
        luggage = (TextView) findViewById(R.id.traveller_det_luggage);

        stopOverLayout = (LinearLayout) findViewById(R.id.traveller_det_stopover_layout);
        //flexLayout = (FlexboxLayout) findViewById(R.id.traveller_det_stopover_flexlayout);
        //flexLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_ROW);

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Fetching Ride Details...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        final RequestParams params = new RequestParams();
        params.put("rideid", rideId);

        getRideDetailWS(params);

        bookRide = (Button) findViewById(R.id.book_seat_button);

        contactOwner = (Button) findViewById(R.id.contact_owner_button);

        bookRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   dialogBookSeats();

            }
        });

    }

    private void dialogBookSeats() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.book_seat_dialog);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;


        ImageView minus = (ImageView) dialog.findViewById(R.id.img_decrease);
        ImageView plus = (ImageView) dialog.findViewById(R.id.img_increase);
        final TextView number = (TextView) dialog.findViewById(R.id.quantity);

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer seatsNumber = Integer.valueOf(number.getText().toString());
                seatsNumber --;
                if(seatsNumber==0){
                    Toast.makeText(getApplicationContext(),"Number of seats cannot be less than 1", Toast.LENGTH_LONG).show();
                }
                else{
                    number.setText(seatsNumber.toString());
                    seatsToBeBooked = seatsNumber.toString();
                }
            }
        });

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer seatsNumber = Integer.valueOf(number.getText().toString());
                seatsNumber ++;
                if(seatsNumber>Integer.valueOf((seats.getText().toString()).split(" ")[0])){
                    Toast.makeText(getApplicationContext(),"Number of seats cannot be more than the seats available", Toast.LENGTH_LONG).show();
                }
                else{
                    number.setText(seatsNumber.toString());
                    seatsToBeBooked = seatsNumber.toString();
                }
            }
        });


        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

        Date today = new Date();

        final String formattedDate = sdf.format(today);

        Button b = (Button) dialog.findViewById(R.id.bt_save1);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final RequestParams params = new RequestParams();
                params.put("bookedby", email);
                params.put("rideid", rideId);
                params.put("seats", seatsToBeBooked);
                params.put("bookedon", formattedDate);

                invokeWS(params);
                dialog.dismiss();
            }
        });


        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    public void invokeWS(RequestParams params){

        prgDialog.show();

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(WebServiceConfig.BOOK_SEATS,params ,new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String responseBody) {
                prgDialog.hide();
                try {

                    JSONObject obj = new JSONObject(responseBody);
                    System.out.println(obj);

                    if(obj.getBoolean("success")){
                        Toast.makeText(getApplicationContext(), "Seats reserved successfully!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), RideHistoryActivity.class);
                        startActivity(intent);

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

    public void getRideDetailWS(RequestParams params) {
        // Show Progress Dialog
        prgDialog.show();

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(WebServiceConfig.GET_RIDE_DETAIL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String responseBody) {
                prgDialog.hide();
                try {

                    JSONObject o = new JSONObject(responseBody);
                    System.out.println(o.toString());


                    if(new Boolean(o.getString("success"))) {
                        JSONArray arr = o.getJSONArray("rides");
                        JSONObject obj = arr.getJSONObject(0);

                        postedBy.setText(obj.getString("posted_by"));
                        postedOn.setText(obj.getString("posted_on"));
                        from.setText(obj.getString("from"));
                        to.setText(obj.getString("to"));
                        fromDateTime.setText(obj.getString("onward_date"));
                        seats.setText(obj.getString("seats")+" Seats");
                        price.setText("$" + obj.getString("price")+" / Seat");
                        payBy.setText("Pay " +obj.getString("pay_by"));
                        ownerComments.setText(obj.getString("owner_comments"));

                        carNumber.setText(obj.getString("car_number"));
                        carModel.setText(obj.getString("car_model"));
                        carColor.setText(obj.getString("car_color"));

                        ac.setText(obj.getString("ac"));
                        music.setText(obj.getString("pets"));
                        pets.setText(obj.getString("music"));
                        luggage.setText(obj.getString("luggage"));

                        String status = obj.getString("status");

                        if(!status.equalsIgnoreCase("Upcoming")){
                            bookRide.setVisibility(View.GONE);
                            contactOwner.setVisibility(View.GONE);
                        }
                       String stopOver = obj.getString("stop_over");

                        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        lparams.setMargins(5,5,5,5);

                        //FlexboxLayout flexboxLayout = (FlexboxLayout) findViewById(R.id.flexbox_layout);
                        //flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);

                       // View view = flexLayout.getChildAt(0);
                        /*FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams (
                                FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT);*/


                       // lp.setMargins(5,5,5,5);
                        //lp.order = -1;
                        //lp.flexGrow = 2;
                        //view.setLayoutParams(lp);

                        String[] stopOverArray = stopOver.split(",");
                        for(int i=0; i< stopOverArray.length;i++){
                            final TextView tv=new TextView(getApplicationContext());
                            tv.setLayoutParams(lparams);
                            tv.setText(stopOverArray[i]);
                            tv.setTextSize(20);
                            tv.setTextColor(Color.WHITE);
                            tv.setPaddingRelative(10,4,10,4);
                            tv.setBackgroundResource(R.drawable.textviewshape);

                            stopOverLayout.addView(tv);
                        }


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
