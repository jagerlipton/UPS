package go.upsseriallogger;

/**
 * Created by 111 on 11.10.2016.
 */
/*
public class Bluetooth {
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
/*

    public void bt_on(){
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(),"Turned on",Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Already on", Toast.LENGTH_LONG).show();
        }
    }

    public void bt_off(){
        BA.disable();
        Toast.makeText(getApplicationContext(),"Turned off" ,Toast.LENGTH_LONG).show();
    }

    public  void bt_visible(){
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }

    public void bt_list_devices(Context context, ListView lv){
        pairedDevices = BA.getBondedDevices();
        ArrayList list = new ArrayList();

        for(BluetoothDevice bt : pairedDevices){
          list.add(bt.getName());
          list.add(bt.getAddress());}


        final ArrayAdapter adapter = new ArrayAdapter(context,android.R.layout.simple_list_item_1, list);
        lv.setAdapter(adapter);
    }
}

*/