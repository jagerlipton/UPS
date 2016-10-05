package go.upsseriallogger;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Set;

import android.util.Log;
import android.widget.Toast;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;


public class MainActivity extends AppCompatActivity {
       final String LOG_TAG = "myLogs";

    final ArrayList<String> logstrings = new ArrayList<String>();
    private final String TAG = MainActivity.class.getSimpleName();

    private UsbManager mUsbManager;
    private ListView mListView;




    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                 case UsbService.BROADCAST_ACTION_DEVlIST:
                 devlist_recieve(intent);
                    break;
            }
        }
    };

    public void devlist_recieve(Intent intent) { // проверено. получает список из сервиса
        if (intent.getExtras().getBoolean("clearlist")) {
            logstrings.clear();

            ListView list = (ListView) findViewById(R.id.deviceList);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, logstrings);
            list.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } else {
            logstrings.clear();
            ArrayList<String> logstrings = intent.getExtras().getStringArrayList("arraylist");
            ListView list = (ListView) findViewById(R.id.deviceList);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, logstrings);
            list.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }


    private UsbService usbService;
    private MyHandler mHandler;


  private static class MyHandler extends Handler {
       private final WeakReference<MainActivity> mActivity;

       public MyHandler(MainActivity activity) {
          mActivity = new WeakReference<>(activity);
       }

      /*  @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    mActivity.get().display.append(data);
                    break;

            }
        }*/
 }

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


    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }


//====================================================================
private void setFilters() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(UsbService.BROADCAST_ACTION_DEVlIST);
    registerReceiver(mUsbReceiver, filter);
}
    //================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new MyHandler(this);


        mListView = (ListView) findViewById(R.id.deviceList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, logstrings);
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
        startService(UsbService.class, usbConnection, null);
        readdevicelist();
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
    public void openFileDialog() {

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



    public void Read_Comport_settings(){
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

    void readdevicelist() {

        Intent intent = new Intent(UsbService.GET_ACTION_DEVlIST);
              sendBroadcast(intent);
    }
    //=====================================================================

    void showConsoleActivity(Integer position) {

        Intent intent = new Intent(MainActivity.this, SerialConsoleActivity.class);
        intent.putExtra("position",position);
        startActivity(intent);

        Intent intent2 = new Intent(UsbService.STARTPORT_ACTION);
        intent2.putExtra("position",position);
        sendBroadcast(intent2);

        Toast.makeText(this, "position="+ Integer.toString(position), Toast.LENGTH_SHORT).show();

    }


}

