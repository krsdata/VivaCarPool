package au.com.vivacar.vivacarpool;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import au.com.vivacar.vivacarpool.adapter.RideListAdapter;
import au.com.vivacar.vivacarpool.config.Config;
import au.com.vivacar.vivacarpool.utils.WebServiceConfig;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RideDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RideDetailFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String rideId;
    private String mParam2;

    private Button action, editRide;
    final Context context = getActivity();
    //private EditText seats;

    TextView postedBy, postedOn, from, to, fromDateTime, seats, price, payBy, ownerComments;
    TextView carModel, carNumber, carColor, ac, music, pets, luggage;
    String email="";
    LinearLayout stopOverLayout;
    String status ="";

    ProgressDialog prgDialog;

    public RideDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RideDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RideDetailFragment newInstance(String param1, String param2) {
        RideDetailFragment fragment = new RideDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            rideId = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Toast.makeText(getActivity(),"val - "+rideId,Toast.LENGTH_SHORT).show();

        View rideDetaiFragmentView =  inflater.inflate(R.layout.fragment_ride_detail, container, false);

        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        email = pref.getString("user_email",null);

        postedBy = (TextView) rideDetaiFragmentView.findViewById(R.id.owner_ride_detail_name);
        postedOn = (TextView) rideDetaiFragmentView.findViewById(R.id.owner_ride_detail_posted_on);
        from = (TextView) rideDetaiFragmentView.findViewById(R.id.owner_ride_detail_from);
        fromDateTime = (TextView) rideDetaiFragmentView.findViewById(R.id.owner_ride_detail_date);
        to = (TextView) rideDetaiFragmentView.findViewById(R.id.owner_ride_detail_to);
        seats = (TextView) rideDetaiFragmentView.findViewById(R.id.owner_ride_detail_seats);
        price = (TextView) rideDetaiFragmentView.findViewById(R.id.owner_ride_detail_price);
        payBy = (TextView) rideDetaiFragmentView.findViewById(R.id.owner_ride_detail_payby);
        ownerComments = (TextView) rideDetaiFragmentView.findViewById(R.id.owner_ride_detail_owner_comments);
        carModel = (TextView) rideDetaiFragmentView.findViewById(R.id.owner_ride_detail_car_model);
        carColor = (TextView) rideDetaiFragmentView.findViewById(R.id.owner_ride_detail_car_type);
        carNumber = (TextView) rideDetaiFragmentView.findViewById(R.id.owner_ride_detail_car_num);
        ac = (TextView) rideDetaiFragmentView.findViewById(R.id.owner_ride_detail_ac);
        music = (TextView) rideDetaiFragmentView.findViewById(R.id.owner_ride_detail_music);
        pets = (TextView) rideDetaiFragmentView.findViewById(R.id.owner_ride_detail_pets);
        luggage = (TextView) rideDetaiFragmentView.findViewById(R.id.owner_ride_detail_luggage);

        stopOverLayout = (LinearLayout)rideDetaiFragmentView.findViewById(R.id.owner_det_stopover_layout);

        action = (Button) rideDetaiFragmentView.findViewById(R.id.ride_det_action_button);
        editRide = (Button) rideDetaiFragmentView.findViewById(R.id.edit_ride_button);

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(getActivity());
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        final RequestParams params = new RequestParams();
        params.put("rideid", rideId);
        getRideDetailWS(params);

        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final Dialog dialog = new Dialog(getActivity());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
                dialog.setContentView(R.layout.owner_ride_action_dialog);

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;


                Button cancelRide = (Button) dialog.findViewById(R.id.owner_ride_action_cancel_ride);

                Button startRide = (Button) dialog.findViewById(R.id.owner_ride_action_start_ride);

                Button endRide = (Button) dialog.findViewById(R.id.owner_ride_end_ride);

                if(!status.contains("Cancelled") || !status.contains("Started") || !status.contains("Completed")){
                    cancelRide.setEnabled(false);
                    cancelRide.setBackgroundColor(getActivity().getResources().getColor(R.color.grey_bg));
                }

                if(!status.contains("Upcoming")){
                    startRide.setEnabled(true);
                    startRide.setBackgroundColor(getActivity().getResources().getColor(R.color.grey_bg));
                }

                if(!status.contains("Started") ){
                    endRide.setEnabled(false);
                    endRide.setBackgroundColor(getActivity().getResources().getColor(R.color.grey_bg));
                }

                cancelRide.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Cancel Ride")
                                .setMessage("Are you sure you want to cancel this ride?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Instantiate Progress Dialog object
                                        prgDialog = new ProgressDialog(getActivity());
                                        // Set Progress Dialog Text
                                        prgDialog.setMessage("Please wait...");
                                        // Set Cancelable as False
                                        prgDialog.setCancelable(false);

                                        cancelRidelWS(params);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // do nothing
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();

                        dialog.dismiss();
                    }
                });

                startRide.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        new AlertDialog.Builder(getActivity())
                                .setTitle("Start Ride")
                                .setMessage("Are you sure you want to start the ride?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Instantiate Progress Dialog object
                                        prgDialog = new ProgressDialog(getActivity());
                                        // Set Progress Dialog Text
                                        prgDialog.setMessage("Please wait...");
                                        // Set Cancelable as False
                                        prgDialog.setCancelable(false);

                                        startRideWS(params);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // do nothing
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();

                        dialog.dismiss();
                    }
                });

                endRide.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        new AlertDialog.Builder(getActivity())
                                .setTitle("End Ride")
                                .setMessage("Are you sure you want to end this ride?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Instantiate Progress Dialog object
                                        prgDialog = new ProgressDialog(getActivity());
                                        // Set Progress Dialog Text
                                        prgDialog.setMessage("Please wait...");
                                        // Set Cancelable as False
                                        prgDialog.setCancelable(false);

                                        endRideWS(params);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // do nothing
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();

                        dialog.dismiss();
                    }
                });


                dialog.show();
                dialog.getWindow().setAttributes(lp);
            }
        });

        editRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity().getApplicationContext(),"edit ride clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity().getApplicationContext(), EditRideActivity.class);
                intent.putExtra("rideid", rideId);
                startActivity(intent);
            }
        });


        return rideDetaiFragmentView;
    }

    public void getRideDetailWS(RequestParams params) {
        // Show Progress Dialog
        prgDialog.show();

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(WebServiceConfig.RIDE_DETAIL, params, new AsyncHttpResponseHandler() {
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

                        String stopOver = obj.getString("stop_over");

                        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        lparams.setMargins(5,5,5,5);


                        String[] stopOverArray = stopOver.split(",");
                        for(int i=0; i< stopOverArray.length;i++){
                            final TextView tv=new TextView(getActivity().getApplicationContext());
                            tv.setLayoutParams(lparams);
                            tv.setText(stopOverArray[i]);
                            tv.setTextSize(20);
                            tv.setTextColor(Color.WHITE);
                            tv.setPaddingRelative(10,4,10,4);
                            tv.setBackgroundResource(R.drawable.textviewshape);

                            stopOverLayout.addView(tv);
                        }

                        status = obj.getString("status");
                        if(!status.contains("Cancelled") || !status.contains("Completed")){
                            editRide.setVisibility(View.GONE);
                            action.setVisibility(View.GONE);
                            if(status.contains("Started")){
                                editRide.setEnabled(false);
                            }
                        }

                    }



                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getActivity(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getActivity().getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getActivity().getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getActivity().getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }

            }


        });

    }

    public void cancelRidelWS(RequestParams params) {
        // Show Progress Dialog
        prgDialog.show();

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(WebServiceConfig.CANCEL_RIDE, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String responseBody) {
                prgDialog.hide();
                try {

                    JSONObject o = new JSONObject(responseBody);
                    System.out.println(o.getString("message"));
                    Toast.makeText(getActivity().getApplicationContext(),o.getString("message"),Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getActivity().getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getActivity().getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getActivity().getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getActivity().getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }

            }


        });

    }

    public void startRideWS(RequestParams params) {
        // Show Progress Dialog
        prgDialog.show();

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(WebServiceConfig.START_RIDE, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String responseBody) {
                prgDialog.hide();
                try {

                    JSONObject o = new JSONObject(responseBody);
                    System.out.println(o.getString("message"));
                    Toast.makeText(getActivity().getApplicationContext(),o.getString("message"),Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getActivity().getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getActivity().getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getActivity().getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getActivity().getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }

            }


        });

    }

    public void endRideWS(RequestParams params) {
        // Show Progress Dialog
        prgDialog.show();

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(WebServiceConfig.END_RIDE, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String responseBody) {
                prgDialog.hide();
                try {

                    JSONObject o = new JSONObject(responseBody);
                    System.out.println(o.getString("message"));
                    Toast.makeText(getActivity().getApplicationContext(),o.getString("message"),Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getActivity().getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getActivity().getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getActivity().getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getActivity().getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }

            }


        });

    }

}

