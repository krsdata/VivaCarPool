package au.com.vivacar.vivacarpool.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import au.com.vivacar.vivacarpool.R;
import au.com.vivacar.vivacarpool.utils.CircleTransform;
import au.com.vivacar.vivacarpool.utils.WebServiceConfig;

/**
 * Created by adarsh on 12/3/17.
 */

public class BookingsListAdapter extends BaseAdapter implements ListAdapter {

    private final Activity activity;
    private final JSONArray jsonArray;
    ProgressDialog prgDialog;

    public BookingsListAdapter(Activity activity, JSONArray jsonArray) {
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
            convertView = activity.getLayoutInflater().inflate(R.layout.bookings_for_ride_row, null);

        TextView bookedBy, bookedOn, seats;
        ImageView userProfilePic;
        Button action, chat;

      //  TextView text =(TextView)convertView.findViewById(R.id.list_title);

      //  MaterialRippleLayout rideLayout = (MaterialRippleLayout) convertView.findViewById(R.id.ride_list_click_layout);

        bookedBy = (TextView)convertView.findViewById(R.id.bookings_booked_by);
        bookedOn = (TextView)convertView.findViewById(R.id.bookings_date);
        seats = (TextView)convertView.findViewById(R.id.bookings_seats);
        userProfilePic = (ImageView) convertView.findViewById(R.id.bookings_profile_pic);

        action = (Button) convertView.findViewById(R.id.bookings_action);
        chat = (Button)convertView.findViewById(R.id.bookings_chat);

        final JSONObject json_data = getItem(position);
        if(null!=json_data ){
            String jj= null;
            try {

                final String bookingStatus = json_data.getString("status").toString();
                bookedBy.setText(json_data.getString("posted_by"));
                bookedOn.setText(json_data.getString("booked_on"));
                seats.setText(json_data.getString("seats"));

                Glide.with(activity).load(json_data.getString("booked_user_pic"))
                        .crossFade()
                        .thumbnail(0.5f)
                        .bitmapTransform(new CircleTransform(activity))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(userProfilePic);


                action.setOnClickListener(new View.OnClickListener() {
                                              @Override
                                              public void onClick(View v) {
                                                  final Dialog dialog = new Dialog(activity);
                                                  dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
                                                  dialog.setContentView(R.layout.owner_bookings_action_dialog);

                                                  WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                                                  lp.copyFrom(dialog.getWindow().getAttributes());
                                                  lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                                                  lp.height = WindowManager.LayoutParams.WRAP_CONTENT;


                                                  Button rejectBooking = (Button) dialog.findViewById(R.id.owner_bookings_action_reject_booking);

                                                  Button confirmBooking = (Button) dialog.findViewById(R.id.owner_bookings_action_confirm_booking);

                                                  //Button provideReview = (Button) dialog.findViewById(R.id.traveller_action_review);

                                                  if(bookingStatus.contains("Cancelled") || bookingStatus.contains("Started") || bookingStatus.contains("Completed") || bookingStatus.contains("Rejected")){
                                                      rejectBooking.setEnabled(false);
                                                      rejectBooking.setBackgroundColor(activity.getResources().getColor(R.color.grey_bg));
                                                  }

                                                  if(!bookingStatus.contains("Waiting")){
                                                      confirmBooking.setEnabled(false);
                                                      confirmBooking.setBackgroundColor(activity.getResources().getColor(R.color.grey_bg));
                                                  }

                                                  rejectBooking.setOnClickListener(new View.OnClickListener() {
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
                                                          rejectBookingWS(params);
                                                          dialog.dismiss();
                                                      }
                                                  });

                                                  confirmBooking.setOnClickListener(new View.OnClickListener() {
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
                                                          confirmBookingWS(params);
                                                          dialog.dismiss();
                                                      }
                                                  });

                                                  /*provideReview.setOnClickListener(new View.OnClickListener() {
                                                      @Override
                                                      public void onClick(View view) {
                                                          final RequestParams params = new RequestParams();

                                                          try {
                                                              params.put("bookingid", json_data.getString("bookingid"));
                                                          } catch (JSONException e) {
                                                              e.printStackTrace();
                                                          }
                                                          //cancelBookingWS(params);
                                                          dialog.dismiss();
                                                      }
                                                  });*/


                                                  dialog.show();
                                                  dialog.getWindow().setAttributes(lp);
                                              }
                                          }

                );


               /* rideLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(activity, RideDetailActivity.class);
                        try {
                            intent.putExtra("rideid",json_data.getString("_id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        activity.startActivity(intent);
                    }
                });*/


            } catch (JSONException e) {
                e.printStackTrace();
            }
           // text.setText(jj);


        }

        return convertView;
    }

    public void rejectBookingWS(RequestParams params) {
        prgDialog.show();

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(WebServiceConfig.REJECT_BOOKING, params, new AsyncHttpResponseHandler() {
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

    public void confirmBookingWS(RequestParams params) {
        prgDialog.show();

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(WebServiceConfig.CONFIRM_BOOKING, params, new AsyncHttpResponseHandler() {
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