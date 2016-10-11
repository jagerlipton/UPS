package go.upsseriallogger;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ReaderAdapter extends BaseAdapter {
private Context context;
    private ArrayList<String> data = new ArrayList<>();

public ReaderAdapter (Context context, ArrayList<String> arr)
        {
                  if (arr != null) {
                data = arr;
            }
            this.context = context;
        }
    @Override
    public int getCount() {
        return data.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }


@Override
public View getView(int position, View convertView, ViewGroup parent)
        {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.rowlayout, parent, false);

        TextView textView = (TextView) view.findViewById(R.id.colors);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.llColors);
  // on the future
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                textView.setTextColor(getActivity().getResources().getColor(android.R.color.white, getActivity().getTheme()));
//            }else {
//                textView.setTextColor(getActivity().getResources().getColor(android.R.color.white));
//            }

        textView.setTextColor(context.getResources().getColor(R.color.reader_font_color));
        textView.setText(data.get(position));
        String s = data.get(position);


            if (s.contains("STATUS"))
        {linearLayout.setBackgroundResource(R.color.STATUS);
        }
        else if (s.contains("CMD"))
        {linearLayout.setBackgroundResource(R.color.CMD);
        }
        else if ((s.contains("NOTICE"))||(s.contains("Notice")))
        {linearLayout.setBackgroundResource(R.color.NOTICE);
        }
        else if ((s.contains("ALARM"))||(s.contains("Alarm")))
        {linearLayout.setBackgroundResource(R.color.ALARM);
        }
        else if (s.contains("Event"))
        {linearLayout.setBackgroundResource(R.color.EVENT);
        }

        else { linearLayout.setBackgroundResource(android.R.color.background_light);

            }
         return view;
                }
        }

