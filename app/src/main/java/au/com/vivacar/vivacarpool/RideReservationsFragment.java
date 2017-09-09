package au.com.vivacar.vivacarpool;


import android.app.ProgressDialog;
import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.vision.text.Line;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;

import au.com.vivacar.vivacarpool.adapter.BookingsListAdapter;
import au.com.vivacar.vivacarpool.adapter.RideListAdapter;
import au.com.vivacar.vivacarpool.utils.WebServiceConfig;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RideReservationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RideReservationsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    ListView bookingsList;
    ProgressDialog prgDialog;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String rideId;

    LinearLayout noBookingsLayout, bookingsListLayout;

    public RideReservationsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RideReservationsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RideReservationsFragment newInstance(String param1, String param2) {
        RideReservationsFragment fragment = new RideReservationsFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rideReservationsFragement =  inflater.inflate(R.layout.fragment_ride_reservations, container, false);
        bookingsList = (ListView) rideReservationsFragement.findViewById(R.id.bookings_list);

        bookingsListLayout = (LinearLayout) rideReservationsFragement.findViewById(R.id.bookings_list_layout);
        noBookingsLayout = (LinearLayout) rideReservationsFragement.findViewById(R.id.bookings_text);

        noBookingsLayout.setVisibility(View.INVISIBLE);

        Toast.makeText(getActivity(),"val 111- "+rideId,Toast.LENGTH_SHORT).show();

        final RequestParams params = new RequestParams();

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(getActivity());
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);


        params.put("rideid", rideId);

        invokeWS(params);

        return rideReservationsFragement;


    }

    public void invokeWS(RequestParams params) {
        // Show Progress Dialog
        prgDialog.show();

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(WebServiceConfig.GET_BOOKINGS_FOR_RIDE, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String responseBody) {
                prgDialog.hide();
                try {


                    JSONObject o = new JSONObject(responseBody);
                    System.out.println("Reservations == " +o.toString());

                    JSONArray arr = o.getJSONArray("rides");

                    if(arr.length() >0 ) {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                        }

                        BookingsListAdapter adapter = new BookingsListAdapter(getActivity(), arr);//jArray is your json array

                        //Set the above adapter as the adapter of choice for our list
                        bookingsList.setAdapter(adapter);
                    }
                    else{
                        bookingsListLayout.setVisibility(View.INVISIBLE);
                        noBookingsLayout.setVisibility(View.VISIBLE);
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
                    Toast.makeText(getActivity(), "Requested resource not found in reservations", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getActivity(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getActivity(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }

            }


        });

    }
}
