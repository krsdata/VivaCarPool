package au.com.vivacar.vivacarpool.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import au.com.vivacar.vivacarpool.R;

/**
 * Created by adarsh on 12/3/17.
 */

public class CardListAdapter extends BaseAdapter implements ListAdapter {

    private final Activity activity;
    private final JSONArray jsonArray;

    ProgressDialog prgDialog;

    public CardListAdapter(Activity activity, JSONArray jsonArray) {
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
            convertView = activity.getLayoutInflater().inflate(R.layout.card_list_row, null);

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(activity);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);


        TextView cardNumber, expiry;


        cardNumber = (TextView)convertView.findViewById(R.id.card_number);
        expiry = (TextView)convertView.findViewById(R.id.card_expiry);


        final JSONObject json_data = getItem(position);
        if(null!=json_data ){
            try {

                cardNumber.setText(json_data.getString("number"));
                expiry.setText("Expires on " + json_data.getString("expiry"));

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return convertView;
    }


}