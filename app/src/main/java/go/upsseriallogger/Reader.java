package go.upsseriallogger;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Reader extends AppCompatActivity {
    private final String LOG_TAG = "myLogs";
    private  final ArrayList<String> logstrings = new ArrayList<>();

    //=============================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
    }
    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String filename = intent.getStringExtra("filename");
        readfile(filename);
    }

   //=============================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reader, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
                case R.id.action_readfile:
                openFileDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
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


            ListView list = (ListView) findViewById(R.id.listview4);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, logstrings);
            list.setAdapter(adapter);
            logstrings.clear();
            TextView status = (TextView) findViewById(R.id.textView);
            status.setText(filename);
            while ((str = br.readLine()) != null) {
                logstrings.add(str);
                adapter.notifyDataSetChanged();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
