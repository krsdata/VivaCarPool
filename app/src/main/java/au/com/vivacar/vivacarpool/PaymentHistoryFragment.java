package au.com.vivacar.vivacarpool;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import au.com.vivacar.vivacarpool.adapter.PaymentHistoryAdapter;
import au.com.vivacar.vivacarpool.adapter.RideHistoryTravellerAdapter;
import au.com.vivacar.vivacarpool.utils.WebServiceConfig;


public class PaymentHistoryFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ProgressDialog prgDialog;
    LinearLayout rideListLayout, noRideLayout;
    ListView rideHistoryTraveller;

    public PaymentHistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RideHistoryAsTravellerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PaymentHistoryFragment newInstance(String param1, String param2) {
        PaymentHistoryFragment fragment = new PaymentHistoryFragment();
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rideHistoryView =  inflater.inflate(R.layout.fragment_payment_history, container, false);

        rideHistoryTraveller = (ListView) rideHistoryView.findViewById(R.id.payment_history_list);

        rideListLayout = (LinearLayout) rideHistoryView.findViewById(R.id.payment_history_list_layout);
        noRideLayout = (LinearLayout) rideHistoryView.findViewById(R.id.payment_history_empty_text);

        noRideLayout.setVisibility(View.INVISIBLE);
        final RequestParams params = new RequestParams();

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(getActivity());
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        System.out.println("Email ==== " + mParam1);

        params.put("email", "raji@gmail.com");

        invokeWS(params);

        return rideHistoryView;

    }


    public void invokeWS(RequestParams params) {
        // Show Progress Dialog
        prgDialog.show();

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(WebServiceConfig.GET_PAYMENT_HISTORY, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String responseBody) {
                prgDialog.dismiss();
                try {

                    JSONObject o = new JSONObject(responseBody);
                    JSONArray arr = o.getJSONArray("charges");

                    if(arr.length()>0) {


                        PaymentHistoryAdapter adapter = new PaymentHistoryAdapter(getActivity(), arr);//jArray is your json array

                        //Set the above adapter as the adapter of choice for our list
                        rideHistoryTraveller.setAdapter(adapter);
                    }
                    else{
                        noRideLayout.setVisibility(View.VISIBLE);
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
                prgDialog.dismiss();
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getActivity(), "Requested resource not found", Toast.LENGTH_LONG).show();
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
