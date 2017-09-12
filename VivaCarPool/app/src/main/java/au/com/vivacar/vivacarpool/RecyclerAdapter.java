package au.com.vivacar.vivacarpool;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;



/**
 * Created by adarsh on 18/2/17.
 */

public class RecyclerAdapter extends  RecyclerView.Adapter<RecyclerViewHolder> {

    String [] name={"Mona"};
    Context context;
    LayoutInflater inflater;
    public RecyclerAdapter(Context context) {
        this.context=context;
        inflater=LayoutInflater.from(context);
    }
    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v=inflater.inflate(R.layout.item_list, parent, false);

        RecyclerViewHolder viewHolder=new RecyclerViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {

        holder.tv1.setText(name[position]);
       // holder.imageView.setOnClickListener(clickListener);
       // holder.imageView.setTag(holder);
        holder.tripLayout.setOnClickListener(clickListener);
    }

    View.OnClickListener clickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            /*RecyclerViewHolder vholder = (RecyclerViewHolder) v.getTag();
            int position = vholder.getPosition();*/

            //Toast.makeText(context,"This is position ",Toast.LENGTH_LONG ).show();
            Intent intent = new Intent(v.getContext(), RideDetailActivity.class);
            v.getContext().startActivity(intent);

        }
    };



    @Override
    public int getItemCount() {
        return name.length;
    }
}