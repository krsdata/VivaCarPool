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
import au.com.vivacar.vivacarpool.TravellerRideDetailActivity;
import au.com.vivacar.vivacarpool.config.Config;

/**
 * Created by adarsh on 12/3/17.
 */

public class NotificationsListAdapter extends BaseAdapter implements ListAdapter {

    private final Activity activity;
    private final JSONArray jsonArray;

    public NotificationsListAdapter(Activity activity, JSONArray jsonArray) {
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
            convertView = activity.getLayoutInflater().inflate(R.layout.notifications_list_row, null);

        TextView action, message, date;

        action = (TextView)convertView.findViewById(R.id.notification_action);
        message = (TextView)convertView.findViewById(R.id.notification_message);
        date = (TextView)convertView.findViewById(R.id.notification_date);

        final JSONObject json_data = getItem(position);
        if(null!=json_data ){

            try {
                action.setText(json_data.getString("action") + " - " +json_data.getString("rideid") );
                message.setText(json_data.getString("message"));
                date.setText(json_data.getString("date"));

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        return convertView;
    }
}