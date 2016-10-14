package go.upsseriallogger;


import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

//  Toast.makeText(context, "azazaza", Toast.LENGTH_SHORT).show();
public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = "myLogs";
    private  ArrayList<Item> devList = new ArrayList<>();
    private  ArrayList<Item> devListUsb = new ArrayList<>();
    private  ArrayList<Item> devListBT = new ArrayList<>();
    private TextView mProgressBarTitle;
    private ProgressBar mProgressBar;
    private BluetoothAdapter BA;
    private final static int REQUEST_ENABLE_BT = 1;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                 case UsbService.SET_DEVLIST_USB:
                 devlistUsb_recieve(intent);
                    break;
                case UsbService.CLEAR_DEVLIST_USB:
                    devlistUsb_clear();
                    break;
                case UsbService.SET_DEVLIST_BT:
                    devlistBT_recieve(intent);
                    break;
                case UsbService.CLEAR_DEVLIST_BT:
                   // devlistUsb_clear();
                    break;
                case UsbService.END_DISCOVERY:
                    hideProgressBar();
                    break;
            }
        }
    };

    private void devlistBT_recieve(Intent intent) { // проверено. получает список из сервиса

        devListBT.clear();
        devList.clear();
     //   Bundle bundle=new Bundle();
        Bundle bundle= intent.getExtras();

        if(bundle != null) {
            devListBT = (ArrayList<Item>) bundle.getSerializable("btdevlist");
            assert devListUsb != null;
            if (devListUsb.size()>0)
                for (int k = 0; k < devListUsb.size(); ++k) devList.add(devListUsb.get(k));
            assert devListBT != null;
            if (devListBT.size()>0)
                for (int k = 0; k < devListBT.size(); ++k) devList.add(devListBT.get(k));

            ListView list = (ListView) findViewById(R.id.deviceList);
            list.setAdapter(new MyAdapter(this, devList));

        }
    }

    private void devlistUsb_clear(){
        devListUsb.clear();
        devList.clear();

        if (devListBT.size()>0)
            for (int k = 0; k < devListBT.size(); ++k) devList.add(devListBT.get(k));

        ListView list = (ListView) findViewById(R.id.deviceList);
          MyAdapter adapter = new MyAdapter(this,devList);
            list.setAdapter(adapter);
              adapter.notifyDataSetChanged();
    }

    private void devlistUsb_recieve(Intent intent) { // проверено. получает список из сервиса

            devListUsb.clear();
            devList.clear();
          //  Bundle bundle=new Bundle();
        Bundle bundle= intent.getExtras();

                 if(bundle != null) {
                     devListUsb = (ArrayList<Item>) bundle.getSerializable("usbdevlist");

                     assert devListUsb != null;
                     if (devListUsb.size()>0)
                     for (int k = 0; k < devListUsb.size(); ++k) devList.add(devListUsb.get(k));
                     assert devListBT != null;
                     if (devListBT.size()>0)
                         for (int k = 0; k < devListBT.size(); ++k) devList.add(devListBT.get(k));

                     ListView list = (ListView) findViewById(R.id.deviceList);
                     list.setAdapter(new MyAdapter(this, devList));
                 }
       }


    private UsbService usbService;

   private final Handler mHandler = new Handler();
   /*     @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_REFRESH:
                   //readdevicelistBT();
                   // Toast.makeText(MainActivity.this, "azazaza", Toast.LENGTH_SHORT).show();
                    mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, REFRESH_TIMEOUT_MILLIS);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }

    };*/

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };


    private void startService(Class<?> service, ServiceConnection serviceConnection) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }


//====================================================================
private void setFilters() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(UsbService.SET_DEVLIST_USB);
    filter.addAction(UsbService.CLEAR_DEVLIST_USB);
    filter.addAction(UsbService.SET_DEVLIST_BT);
    filter.addAction(UsbService.CLEAR_DEVLIST_BT);
    filter.addAction(UsbService.END_DISCOVERY);

    registerReceiver(mUsbReceiver, filter);
}
    //================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
      //  mHandler = new MyHandler(this);


        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBarTitle = (TextView) findViewById(R.id.progressBarTitle);
        ListView mListView = (ListView) findViewById(R.id.deviceList);
        MyAdapter adapter = new MyAdapter (MainActivity.this, devList);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                      if (position >= 0) showConsoleActivity(position);
            }

        });


    }
    @Override
    protected void onResume() {
        super.onResume();
        Read_Comport_settings();
        setFilters();
        startService(UsbService.class, usbConnection);
          readdevicelistUSB();
        hideProgressBar();
        BA = BluetoothAdapter.getDefaultAdapter();

    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);

    }



    //================ главное меню формы===========================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings_comport:
                Intent intent = new Intent(MainActivity.this, Settings.class);
                startActivity(intent);
                return true;
            case R.id.action_exit:
                this.finish();
                return true;
               case R.id.action_readfile:
             openFileDialog();

               return true;

                   default:
                return super.onOptionsItemSelected(item);
        }
    }
//=======================================================
private void openFileDialog() {

        FileChooser filechooser = new FileChooser(this);
        filechooser.setFileListener(new FileChooser.FileSelectedListener() {
            @Override
            public void fileSelected(final File file) {
                String filename = file.getAbsolutePath();
                Log.d(LOG_TAG, "Открыть файл для чтения: " + filename);
                Intent intent2 = new Intent(MainActivity.this, Reader.class);
                intent2.putExtra("filename",filename);
                startActivity(intent2);
            }
        });
        filechooser.setExtension("txt");
        filechooser.showDialog();
        }



    private void Read_Comport_settings(){
        String downloadType;
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        downloadType = SP.getString("comport_bauderate","9600");
        Global_data.Gd_comport_baudrate=downloadType;
        downloadType = SP.getString("comport_databit","8");
        Global_data.Gd_comport_databit=downloadType;
        downloadType = SP.getString("comport_chet","NONE");
        Global_data.Gd_comport_chet=downloadType;
        downloadType = SP.getString("comport_stopbits","1");
        Global_data.Gd_comport_stopbits=downloadType;
        downloadType = SP.getString("comport_flowcontrol","OFF");
        Global_data.Gd_comport_flowcontrol=downloadType;
        downloadType = SP.getString("pref_directory_path","");
        Global_data.Gd_Directory_path=downloadType;

        Global_data.Gd_BAUDRATE=Integer.parseInt(Global_data.Gd_comport_baudrate);
        Global_data.Gd_DATABITS=Integer.parseInt(Global_data.Gd_comport_databit);


        switch(Global_data.Gd_comport_chet) {
            case "NONE":
                Global_data.Gd_PARITY=0;
                break;
            case "ODD":
                Global_data.Gd_STOPBITS=1;
                break;
            case  "EVEN":
                Global_data.Gd_STOPBITS=2;
                break;
            case  "MARK":
                Global_data.Gd_STOPBITS=3;
                break;
            case  "SPACE":
                Global_data.Gd_STOPBITS=4;
                break;
        }

        switch(Global_data.Gd_comport_stopbits) {
            case "1":
                Global_data.Gd_STOPBITS=1;
                break;
            case "1,5":
                Global_data.Gd_STOPBITS=3;
                break;
            case  "2":
                Global_data.Gd_STOPBITS=2;
                break;
        }
        switch(Global_data.Gd_comport_flowcontrol) {
            case "OFF":
                Global_data.Gd_FLOWCONTROL=0;
                break;
            case "RTS_CTS":
                Global_data.Gd_FLOWCONTROL=1;
                break;
            case  "DSR_DTR":
                Global_data.Gd_FLOWCONTROL=2;
                break;
            case  "XON_XOFF":
                Global_data.Gd_FLOWCONTROL=3;
                break;

        }


    }



    //========================================================================

    private  void readdevicelistUSB() {

        Intent intent = new Intent(UsbService.GET_ACTION_DEVlIST_USB);
              sendBroadcast(intent);
    }

    private  void readdevicelistBT() {

        Intent intent = new Intent(UsbService.GET_ACTION_DEVlIST_BT);
        sendBroadcast(intent);
    }

    //=====================================================================

    private void showConsoleActivity(Integer position) {

        Item item;
        item=devList.get(position);
        if (item.btconnection){
            Intent intent = new Intent(MainActivity.this, SerialConsoleActivity.class);
            startActivity(intent);
            Intent intent2 = new Intent(UsbService.STARTPORT_BT_ACTION);
            intent2.putExtra("mac",item.mac);
            sendBroadcast(intent2);
        }
        else {
            Intent intent = new Intent(MainActivity.this, SerialConsoleActivity.class);
            startActivity(intent);
            Intent intent2 = new Intent(UsbService.STARTPORT_ACTION);
            intent2.putExtra("position",item.absolute_index_fromSerialDevList);
            sendBroadcast(intent2);
        }
    }
//=============================================
    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBarTitle.setVisibility(View.VISIBLE);
        mProgressBarTitle.setText(R.string.refreshing);
    }

    private void hideProgressBar() {
        mProgressBar.setVisibility(View.INVISIBLE);
        mProgressBarTitle.setVisibility(View.INVISIBLE);
    }

//======================процедуры блюпупа

    public void btbutton_click (View V) {
        if (BA.isEnabled())
        if (BA.isDiscovering())BA.cancelDiscovery();
        else
        {
            showProgressBar();
            readdevicelistBT();
        }
        else
        {

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == REQUEST_ENABLE_BT){
            if(resultCode == RESULT_OK){
                showProgressBar();
                readdevicelistBT();
            }else{
                //bluetooth was not successfully turned on
            }
        }
    }

}

