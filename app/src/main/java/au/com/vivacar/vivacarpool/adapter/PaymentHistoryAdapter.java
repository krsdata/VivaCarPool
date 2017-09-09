package au.com.vivacar.vivacarpool.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import au.com.vivacar.vivacarpool.R;

/**
 * Created by adarsh on 12/3/17.
 */

public class PaymentHistoryAdapter extends BaseAdapter implements ListAdapter {

    private final Activity activity;
    private final JSONArray jsonArray;

    ProgressDialog prgDialog;

    public PaymentHistoryAdapter(Activity activity, JSONArray jsonArray) {
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
            convertView = activity.getLayoutInflater().inflate(R.layout.payment_history_row, null);

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(activity);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);


        TextView description, amount, date, card, status;


        description = (TextView)convertView.findViewById(R.id.payment_history_description);
        amount = (TextView)convertView.findViewById(R.id.payment_history_amount);
        date = (TextView)convertView.findViewById(R.id.payment_history_date);
        card = (TextView)convertView.findViewById(R.id.payment_history_card);
        status = (TextView)convertView.findViewById(R.id.payment_history_status);


        final JSONObject json_data = getItem(position);
        if(null!=json_data ){
            try {

                description.setText(json_data.getString("description"));
                amount.setText(json_data.getString("amount"));
                date.setText(json_data.getString("date"));
                card.setText(json_data.getString("card"));
                status.setText(json_data.getString("status"));

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return convertView;
    }


}