package au.com.vivacar.vivacarpool;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by adarsh on 18/2/17.
 */

public class RecyclerViewHolder extends RecyclerView.ViewHolder {

    TextView tv1,tv2;
    ImageView imageView;
    LinearLayout tripLayout;

    public RecyclerViewHolder(View itemView) {
        super(itemView);

        tv1= (TextView) itemView.findViewById(R.id.list_title);
        tripLayout = (LinearLayout) itemView.findViewById(R.id.item_layout);

        //tv2= (TextView) itemView.findViewById(R.id.list_desc);
       // imageView= (ImageView) itemView.findViewById(R.id.list_avatar);

    }
}