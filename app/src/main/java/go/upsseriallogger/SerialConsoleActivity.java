package go.upsseriallogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserConfig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SerialConsoleActivity extends AppCompatActivity {

    private final ArrayList<String> logstrings = new ArrayList<>();
    private static final SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("dd_MM_yyyy", Locale.ROOT);
    private static final String LOG_EXTENTION = ".txt";
    private final String LOG_TAG = "myLogs";
    private static final int REQUEST_DIRECTORY = 0;


   //==========
   private void lines_add(String str){

       ListView list = (ListView) findViewById(R.id.listview3);
       //ArrayAdapter<String> adapter = new ArrayAdapter<>(SerialConsoleActivity.this, android.R.layout.simple_list_item_1, logstrings);
       ReaderAdapter adapter = new ReaderAdapter(this,logstrings);
       list.setAdapter(adapter);
       logstrings.add(str);
       adapter.notifyDataSetChanged();
   }


 //=============

 private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_LOG_LIST:
                    lines_add((intent.getExtras().getString("data")));
                                    break;
            }
        }
    };


    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_LOG_LIST);
        registerReceiver(mUsbReceiver, filter);
    }


//=====================================
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_console);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);

          }

    @Override
    protected void onResume() {
        super.onResume();
       setFilters();
    }


    @Override
    protected void onPause() {
        super.onPause();
        //close port
        Intent intent = new Intent(UsbService.STOPPORT_ACTION);
        sendBroadcast(intent);
      unregisterReceiver(mUsbReceiver);
        Intent intent2 = new Intent(UsbService.STOPPORT_BT_ACTION);
           sendBroadcast(intent2);
    }


   //=======меню-===========================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_console, menu);
        return true;
    }

    private void Start_chooser_dialog(String extra)
    {
        final Intent chooserIntent = new Intent(
               SerialConsoleActivity.this,
                DirectoryChooserActivity.class);

        final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                .newDirectoryName("Logs")
                .allowReadOnlyDirectory(true)
                .allowNewDirectoryNameModification(true)
                .build();
        chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config);
        Global_data.Gd_Intent_data=extra;
        startActivityForResult(chooserIntent, REQUEST_DIRECTORY);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings_directory_path:
                Start_chooser_dialog("settings_directory_path");
                return true;
            case R.id.action_saveas:
                Start_chooser_dialog("saveas");
                return true;
            case R.id.action_save:
                WriteFile_from_listview(Global_data.Gd_Directory_path);
                return true;
            case R.id.action_readfile:
                openFileDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
//=======================================================================
private void openFileDialog() {

        FileChooser filechooser = new FileChooser(this);
        filechooser.setFileListener(new FileChooser.FileSelectedListener() {
            @Override
            public void fileSelected(final File file) {
                String filename = file.getAbsolutePath();
                Log.d(LOG_TAG, "Открыть файл для чтения: " + filename);
                readfile(filename);
            }
        });
        filechooser.setExtension("txt");
        filechooser.showDialog();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        }

        if (requestCode == REQUEST_DIRECTORY) {
            Log.i(LOG_TAG, String.format("Return from DirChooser with result %d",  resultCode));
                 if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
                if (Global_data.Gd_Intent_data.equals("settings_directory_path")) {
                    Global_data.Gd_Directory_path=data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR);
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("pref_directory_path",data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR));
                    editor.apply();
                }
                if (Global_data.Gd_Intent_data.equals("saveas")) {
                    WriteFile_from_listview(data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR));
                }
            }
        }
    }
    //========================

    //==========================================
    private  void WriteFile_from_listview(String filepath) {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Log.d(LOG_TAG, "SD-карта не доступна: " + Environment.getExternalStorageState());
            return;
        }
        String dateString = DATA_FORMAT.format(new Date());
        String fileName = "Log_"+dateString;

        File sdFile = new File(filepath, fileName+LOG_EXTENTION);
        int i = 0;
        while (sdFile.exists()) {
            i += 1;
            sdFile = new File(filepath, fileName+"_"+(i)+LOG_EXTENTION);
        }

        try {

            BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));
              ListView list = (ListView) findViewById(R.id.listview3);
            //ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, logstrings);
            ReaderAdapter adapter = new ReaderAdapter(this,logstrings);
                list.setAdapter(adapter);
            Integer list_lines_count=adapter.getCount();

            for (int  k = 0; k < list_lines_count; ++k){
                String str=logstrings.get(k);
                bw.write(str);
                bw.newLine();
            }
            bw.close();
            Log.d(LOG_TAG, "Файл записан на SD: " + sdFile.getAbsolutePath());
            Toast.makeText(this, "Файл "+fileName+"_"+(i)+LOG_EXTENTION+" успешно записан", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //========================================================================

    private void readfile(String filename) {

        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Log.d(LOG_TAG, "SD-карта не доступна: " + Environment.getExternalStorageState());
            return;
        }

        File sdFile = new File(filename);
        try {

            BufferedReader br = new BufferedReader(new FileReader(sdFile));
            String str;


              ListView list = (ListView) findViewById(R.id.listview3);
            //ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, logstrings);
            ReaderAdapter adapter = new ReaderAdapter(this,logstrings);
              list.setAdapter(adapter);
            logstrings.clear();
            while ((str = br.readLine()) != null) {
                logstrings.add(str);
                adapter.notifyDataSetChanged();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





//=========обработчики кнопок==================
    public void ESCL_click (View v) {

        Intent intent = new Intent(UsbService.ESCL_ACTION);
          sendBroadcast(intent);
    }

    public void ESCH_click (View v) {

        Intent intent = new Intent(UsbService.ESCH_ACTION);
        sendBroadcast(intent);
    }

    public void ESCB_click (View V) {

        Intent intent = new Intent(UsbService.ESCB_ACTION);
        sendBroadcast(intent);
    }

    public void ESCN_click (View V) {

        Intent intent = new Intent(UsbService.ESCN_ACTION);
        sendBroadcast(intent);
    }

    public void clearlist_click (View V) {
        logstrings.clear();
        ListView list = (ListView) findViewById(R.id.listview3);
       // ArrayAdapter<String> adapter = new ArrayAdapter<>(SerialConsoleActivity.this, android.R.layout.simple_list_item_1, logstrings);
        ReaderAdapter adapter = new ReaderAdapter(this,logstrings);
        list.setAdapter(adapter);

        adapter.notifyDataSetChanged();
    }

    //===================
}
