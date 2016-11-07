package go.upsseriallogger;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class IPAdressSettings extends AppCompatActivity {
    private ArrayList<String> liststrings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipadress_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar4);
        setSupportActionBar(toolbar);
        ListView list = (ListView) findViewById(R.id.listview5);
        registerForContextMenu(list);

    }

    @Override
    protected void onResume() {
        super.onResume();
        readiplist();
    }


    @Override
    protected void onPause() {
        super.onPause();
        writeiplist();
    }

        @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu_ipadress, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.deleteip:
            deleteIP(info.position); // метод, выполняющий действие при редактировании пункта меню
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    private boolean validate(String ip) {
        if (TextUtils.isEmpty(ip)) {
            return false;
        }
        final Pattern pattern = Patterns.IP_ADDRESS;
        return pattern.matcher(ip).matches();

    }

    protected void AddIP_click(View view){

        EditText edit = (EditText) findViewById(R.id.editText3);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, liststrings);
        ListView list = (ListView) findViewById(R.id.listview5);
        list.setAdapter(adapter);
        if (validate(edit.getText().toString())){

            liststrings.add(edit.getText().toString());
            adapter.notifyDataSetChanged();
            edit.setText("");
        }



    }

  private void deleteIP(Integer position){
      ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, liststrings);
      ListView list = (ListView) findViewById(R.id.listview5);
      list.setAdapter(adapter);
      adapter.remove(adapter.getItem(position));
      adapter.notifyDataSetChanged();

  }




    private void writeiplist() {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = SP.edit();
        Set<String> set = new HashSet<>();
        set.addAll(liststrings);
        editor.putStringSet("DATE_LIST", set);
        editor.apply();

    }

    private void readiplist() {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        Set<String> set = SP.getStringSet("DATE_LIST", new HashSet<String>());
        assert set != null;
        liststrings.addAll(set);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, liststrings);
        ListView list = (ListView) findViewById(R.id.listview5);
        list.setAdapter(adapter);
    }


}
