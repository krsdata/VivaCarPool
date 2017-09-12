package au.com.vivacar.vivacarpool.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import au.com.vivacar.vivacarpool.R;
import au.com.vivacar.vivacarpool.RideDetailActivity;
import au.com.vivacar.vivacarpool.RideListActivity;
import au.com.vivacar.vivacarpool.TravellerRideDetailActivity;
import au.com.vivacar.vivacarpool.config.Config;
import au.com.vivacar.vivacarpool.utils.DateTimeUtil;

/**
 * Created by adarsh on 12/3/17.
 */

public class RideListAdapter extends BaseAdapter implements ListAdapter {

    private final Activity activity;
    private final JSONArray jsonArray;

    public RideListAdapter (Activity activity, JSONArray jsonArray) {
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
            convertView = activity.getLayoutInflater().inflate(R.layout.ride_list_row, null);

        SharedPreferences pref = activity.getSharedPreferences(Config.SHARED_PREF, 0);
        final String userEmail = pref.getString("user_email",null);


        TextView from, to, departureDateTime, seats, price, postedByName, postedOn;
        ImageView ac, music, pets;
        com.balysv.materialripple.MaterialRippleLayout acLayout, musicLayout, petsLayout;

        MaterialRippleLayout rideLayout = (MaterialRippleLayout) convertView.findViewById(R.id.ride_list_click_layout);

        from = (TextView)convertView.findViewById(R.id.ride_list_from_location);
        to = (TextView)convertView.findViewById(R.id.ride_list_to_location);
        departureDateTime = (TextView)convertView.findViewById(R.id.ride_list_from_date_time1);
        seats = (TextView)convertView.findViewById(R.id.ride_list_seats);
        price = (TextView)convertView.findViewById(R.id.ride_list_amount);
        postedByName = (TextView)convertView.findViewById(R.id.ride_list_posted_by);
        postedOn = (TextView)convertView.findViewById(R.id.ride_list_posted_date);

        ac = (ImageView) convertView.findViewById(R.id.bt_ac);
        music = (ImageView) convertView.findViewById(R.id.bt_music);
        pets = (ImageView) convertView.findViewById(R.id.bt_pets);

        acLayout = (MaterialRippleLayout) convertView.findViewById(R.id.layout_ac);
        musicLayout = (MaterialRippleLayout) convertView.findViewById(R.id.layout_music);
        petsLayout = (MaterialRippleLayout) convertView.findViewById(R.id.layout_pets);

        final JSONObject json_data = getItem(position);
        if(null!=json_data ){

            try {
                from.setText(json_data.getString("from"));
                to.setText(json_data.getString("to"));
                departureDateTime.setText(json_data.getString("onward_date"));
                seats.setText(json_data.getString("seats") + " Seats");
                price.setText("$ " +json_data.getString("price") + " / Seat");
                postedByName.setText(json_data.getString("posted_by"));
                postedOn.setText(json_data.getString("posted_on"));

                if(json_data.getString("ac").equalsIgnoreCase("Not Available")){
                    acLayout.setVisibility(View.GONE);
                }
                if(json_data.getString("music").equalsIgnoreCase("Not Available")){
                    musicLayout.setVisibility(View.GONE);
                }
                if(json_data.getString("pets").equalsIgnoreCase("Not Available")){
                    petsLayout.setVisibility(View.GONE);
                }
                final String posted_by_email = json_data.getString("posted_by_email");

                rideLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(userEmail.equalsIgnoreCase(posted_by_email)){
                            Intent intent = new Intent(activity, RideDetailActivity.class);
                            try {
                                intent.putExtra("rideid",json_data.getString("rideid"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            activity.startActivity(intent);
                        }
                        else{
                            Intent intent = new Intent(activity, TravellerRideDetailActivity.class);
                            try {
                                intent.putExtra("rideid",json_data.getString("rideid"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            activity.startActivity(intent);
                        }

                    }
                });


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        return convertView;
    }
}