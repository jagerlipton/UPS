package go.upsseriallogger;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MyAdapter extends BaseAdapter {

    private ArrayList<Item> data = new ArrayList<>();
    private Context context;

    public MyAdapter(Context context, ArrayList<Item> arr) {
        if (arr != null) {
            data = arr;
        }
        this.context = context;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return data.size();
    }

    @Override
    public Object getItem(int num) {
        // TODO Auto-generated method stub
        return data.get(num);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int i, View someView, ViewGroup arg2) {

        LayoutInflater inflater = LayoutInflater.from(context);
            if (someView == null) {
            someView = inflater.inflate(R.layout.list_view_item, arg2, false);
        }
        ImageView imageView = (ImageView) someView.findViewById(R.id.img);
        TextView header = (TextView) someView.findViewById(R.id.item_headerText);
        TextView subHeader = (TextView) someView.findViewById(R.id.item_subHeaderText);
        header.setText(data.get(i).header);
        subHeader.setText(data.get(i).subheader);
       if (data.get(i).btconnection) imageView.setImageResource( R.drawable.bt);
       else imageView.setImageResource( R.drawable.usb);
        return someView;
    }

}