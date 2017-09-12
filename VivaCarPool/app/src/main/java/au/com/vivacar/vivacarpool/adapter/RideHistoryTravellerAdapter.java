package au.com.vivacar.vivacarpool.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Rating;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import angtrim.com.fivestarslibrary.FiveStarsDialog;
import angtrim.com.fivestarslibrary.NegativeReviewListener;
import angtrim.com.fivestarslibrary.ReviewListener;
import au.com.vivacar.vivacarpool.R;
import au.com.vivacar.vivacarpool.RideDetailActivity;
import au.com.vivacar.vivacarpool.utils.WebServiceConfig;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * Created by adarsh on 12/3/17.
 */

public class RideHistoryTravellerAdapter extends BaseAdapter implements ListAdapter {

    private final Activity activity;
    private final JSONArray jsonArray;

    ProgressDialog prgDialog;

    public RideHistoryTravellerAdapter(Activity activity, JSONArray jsonArray) {
        assert activity != null;
        assert jsonArray != null;

        this.jsonArray = jsonArray;
        this.activity = activity;
    }


    @Override public int getCount() {
        if(null==jsonArray)
            return 0;
        else
            return jsonArray.length();
    }

    @Override public JSONObject getItem(int position) {
        if(null==jsonArray) return null;
        else
            return jsonArray.optJSONObject(position);
    }

    @Override public long getItemId(int position) {
        JSONObject jsonObject = getItem(position);

        return jsonObject.optLong("id");
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = activity.getLayoutInflater().inflate(R.layout.ride_history_traveller_row, null);

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(activity);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);


        TextView from, to, fromDate, bookedOn, seats, status, rideId;

        final Button action, chat;



        //  TextView text =(TextView)convertView.findViewById(R.id.list_title);

       // MaterialRippleLayout rideLayout = (MaterialRippleLayout) convertView.findViewById(R.id.ride_list_click_layout_traveller);

        from = (TextView)convertView.findViewById(R.id.ride_history_from_location);
        to = (TextView)convertView.findViewById(R.id.ride_history_to_location);
        fromDate = (TextView)convertView.findViewById(R.id.ride_history_from_date_time);
        bookedOn = (TextView)convertView.findViewById(R.id.history_booked_date);
        seats = (TextView)convertView.findViewById(R.id.history_booked_seats);
        status = (TextView)convertView.findViewById(R.id.history_ride_status);
        rideId = (TextView)convertView.findViewById(R.id.history_ride_id);

        action = (Button) convertView.findViewById(R.id.history_ride_action);
        chat = (Button)convertView.findViewById(R.id.history_ride_chat);

        final JSONObject json_data = getItem(position);
        if(null!=json_data ){
            try {

                final String rideStatus = json_data.getString("status");

                from.setText(json_data.getString("from"));
                to.setText(json_data.getString("to"));
                bookedOn.setText(json_data.getString("booked_on"));
                seats.setText(json_data.getString("seats"));
                status.setText(rideStatus);
                fromDate.setText(json_data.getString("date"));
                rideId.setText(json_data.getString("rideid"));

                final String payBy = json_data.getString("payby").toString();
                final String paymentStatus = json_data.getString("paymentstatus").toString();
                final String review = json_data.getString("review").toString();
                final String price = json_data.getString("price").toString();

                /*if(json_data.getString("status").contains("Cancelled")){
                    action.setVisibility(View.GONE);
                    chat.setVisibility(View.GONE);
                }*/

                /*action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Toast.makeText(activity,"Hello",Toast.LENGTH_SHORT).show();
                       System.out.println("Cancel clicked");

                        final RequestParams params = new RequestParams();
                        try {
                            params.put("bookingid", json_data.getString("bookingid"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        invokeWS(params);
                    }
                }); */

                action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Dialog dialog = new Dialog(activity);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
                        dialog.setContentView(R.layout.traveller_action_dialog);

                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                        lp.copyFrom(dialog.getWindow().getAttributes());
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;


                        Button cancelBooking = (Button) dialog.findViewById(R.id.traveller_action_cancel_booking);

                        Button makePayment = (Button) dialog.findViewById(R.id.traveller_action_make_payment);

                        Button provideReview = (Button) dialog.findViewById(R.id.traveller_action_review);

                        if(rideStatus.contains("Cancelled") || rideStatus.contains("Started") || rideStatus.contains("Completed")){
                            cancelBooking.setEnabled(false);
                            cancelBooking.setBackgroundColor(activity.getResources().getColor(R.color.grey_bg));
                        }
                        cancelBooking.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final RequestParams params = new RequestParams();

                                try {
                                    params.put("bookingid", json_data.getString("bookingid"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                // Instantiate Progress Dialog object
                                prgDialog = new ProgressDialog(activity);
                                // Set Progress Dialog Text
                                prgDialog.setMessage("Please wait...");
                                // Set Cancelable as False
                                prgDialog.setCancelable(false);

                                cancelBookingWS(params);
                                dialog.dismiss();
                            }
                        });

                        if(payBy.contains("CASH")){
                            makePayment.setEnabled(false);
                            makePayment.setBackgroundColor(activity.getResources().getColor(R.color.grey_bg));
                        }
                        else{
                            if(!paymentStatus.contains("Pending")){
                                makePayment.setEnabled(false);
                                makePayment.setBackgroundColor(activity.getResources().getColor(R.color.grey_bg));
                            }
                        }
                        makePayment.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final RequestParams params = new RequestParams();

                                try {
                                    params.put("bookingid", json_data.getString("bookingid"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                cancelBookingWS(params);
                                dialog.dismiss();
                            }
                        });

                        if(!review.contains("Pending")){
                            provideReview.setEnabled(false);
                            provideReview.setBackgroundColor(activity.getResources().getColor(R.color.grey_bg));
                        }
                        provideReview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                final Dialog dialog1 = new Dialog(activity);
                                dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
                                dialog1.setContentView(R.layout.traveller_action_review_dialog);

                                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                                lp.copyFrom(dialog.getWindow().getAttributes());
                                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                                final RatingBar rb = (RatingBar) dialog1.findViewById(R.id.ratingbar);

                                //final MaterialRatingBar rb1 = (MaterialRatingBar) dialog1.findViewById(R.id.ratingbar1);

                                Button submitRating = (Button) dialog1.findViewById(R.id.traveller_action_submit_rating);

                                dialog1.show();
                                dialog1.getWindow().setAttributes(lp);

                                submitRating.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        System.out.println("rb = " + rb.getRating());
                                        dialog1.dismiss();
                                    }
                                });




                                final RequestParams params = new RequestParams();

                                /*try {
                                    params.put("bookingid", json_data.getString("bookingid"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                cancelBookingWS(params);*/
                                dialog.dismiss();
                            }
                        });


                        dialog.show();
                        dialog.getWindow().setAttributes(lp);
                    }
                }

                );


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


        return convertView;
    }


    public void cancelBookingWS(RequestParams params) {
        prgDialog.show();

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(WebServiceConfig.CANCEL_BOOKING, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String responseBody) {
                prgDialog.dismiss();
                try {

                    JSONObject o = new JSONObject(responseBody);
                    //JSONArray arr = o.getJSONArray("rides");

                    System.out.println("arr =" +o.toString());


                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(activity, "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(activity, "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(activity, "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(activity, "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }

            }


        });

    }
}