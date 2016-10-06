package go.upsseriallogger;


import android.app.Activity;
import android.app.Dialog;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

public class FileChooser  extends AppCompatActivity {
    private static final String PARENT_DIR = "..";

    private final Activity activity;
    private final ListView list;
    private final Dialog dialog;

    private File currentPath;
    private String extension = null;

    public void setExtension(String extension) {
        this.extension = (extension == null) ? null :
                extension.toLowerCase();
    }

    public interface FileSelectedListener {
        void fileSelected(File file);
    }
    public void setFileListener(FileSelectedListener fileListener) {
        this.fileListener = fileListener;
    }
    private FileSelectedListener fileListener;

    public FileChooser(Activity activity) {
          activity.setTheme(R.style.DialogTheme);
        this.activity = activity;
           dialog = new Dialog(activity);

           list = new ListView(activity);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                String fileChosen = (String) list.getItemAtPosition(which);
                File chosenFile = getChosenFile(fileChosen);
                if (chosenFile.isDirectory()) {
                    refresh(chosenFile);
                } else {
                    if (fileListener != null) {
                        fileListener.fileSelected(chosenFile);
                    }
                    dialog.dismiss();
                }
            }
        });

        dialog.setContentView(list);

        assert  dialog.getWindow() != null;
        dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        refresh(Environment.getExternalStorageDirectory());
    }

    public void showDialog() {
        dialog.show();
    }


    private void refresh(File path) {
        this.currentPath = path;
        if (path.exists()) {
            File[] dirs = path.listFiles(new FileFilter() {
                @Override public boolean accept(File file) {
                    return (file.isDirectory() && file.canRead());
                }
            });
            File[] files = path.listFiles(new FileFilter() {
                @Override public boolean accept(File file) {
                    return !file.isDirectory() && file.canRead() && (extension == null || file.getName().toLowerCase().endsWith(extension));
                }
            });


            int i = 0;
            String[] fileList;
            if (path.getParentFile() == null) {
                fileList = new String[dirs.length + files.length];
            } else {
                fileList = new String[dirs.length + files.length + 1];
                fileList[i++] = PARENT_DIR;
            }
            Arrays.sort(dirs);
            Arrays.sort(files);
            for (File dir : dirs) { fileList[i++] = dir.getName(); }
            for (File file : files ) { fileList[i++] = file.getName(); }





            dialog.setTitle(currentPath.getPath());
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, fileList)
            {

                @NonNull
                @Override public View getView(int pos, View view, @NonNull ViewGroup parent) {
                view = super.getView(pos, view, parent);
                ((TextView) view).setSingleLine(true);
                return view;
            }
            };
             list.setAdapter(adapter);

        }
    }


    private File getChosenFile(String fileChosen) {
        if (fileChosen.equals(PARENT_DIR)) {
            return currentPath.getParentFile();
        } else {
            return new File(currentPath, fileChosen);
        }
    }
}