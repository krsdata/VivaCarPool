package au.com.vivacar.vivacarpool.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import au.com.vivacar.vivacarpool.R;
import au.com.vivacar.vivacarpool.RideDetailActivity;

/**
 * Created by adarsh on 12/3/17.
 */

public class RideHistoryOwnerAdapter extends BaseAdapter implements ListAdapter {

    private final Activity activity;
    private final JSONArray jsonArray;

    public RideHistoryOwnerAdapter(Activity activity, JSONArray jsonArray) {
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
            convertView = activity.getLayoutInflater().inflate(R.layout.ride_history_owner_row, null);

        TextView from, to, fromDate,seats, status, seatsBooked;


      //  TextView text =(TextView)convertView.findViewById(R.id.list_title);

        MaterialRippleLayout rideLayout = (MaterialRippleLayout) convertView.findViewById(R.id.ride_list_click_layout);

        from = (TextView)convertView.findViewById(R.id.history_owner_from_location);
        to = (TextView)convertView.findViewById(R.id.history_owner_to_location);
        fromDate = (TextView)convertView.findViewById(R.id.history_owner_fromdate);
        seats = (TextView)convertView.findViewById(R.id.history_owner_seats);
        status = (TextView)convertView.findViewById(R.id.history_owner_status);
        seatsBooked = (TextView)convertView.findViewById(R.id.history_owner_seats_booked);



        final JSONObject json_data = getItem(position);
        if(null!=json_data ){
            try {

                Integer seatsBookedNumber = Integer.parseInt(json_data.getString("seats"))-Integer.parseInt(json_data.getString("seats_available"));
                System.out.println("Booked ====="+seatsBookedNumber);

                from.setText(json_data.getString("from"));
                to.setText(json_data.getString("to"));
                seats.setText(json_data.getString("seats"));
                status.setText(json_data.getString("status"));
                fromDate.setText(json_data.getString("date"));
                seatsBooked.setText(seatsBookedNumber.toString());

                rideLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(activity, RideDetailActivity.class);
                        try {
                            intent.putExtra("rideid",json_data.getString("rideid"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        activity.startActivity(intent);
                    }
                });


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        return convertView;
    }
}