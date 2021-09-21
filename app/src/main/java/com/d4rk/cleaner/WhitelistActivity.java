package com.d4rk.cleaner;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import java.util.LinkedList;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.util.Arrays;
import java.util.List;
public class WhitelistActivity extends AppCompatActivity {
    ListView listView;
    BaseAdapter adapter;
    private static LinkedList<String> whiteList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whitelist);
        listView = findViewById(R.id.whitelistView);
        adapter = new ArrayAdapter < > (this, R.layout.fragment_whitelist_custom_textview, getWhiteList());
        listView.setAdapter(adapter);
    }
    public final void emptyWhitelist(View view) {
        new AlertDialog.Builder(WhitelistActivity.this, R.style.MyAlertDialogTheme)
                .setTitle(R.string.whitelist_empty)
                .setMessage(R.string.whitelist_empty_description)
                .setPositiveButton(R.string.whitelist_clear, (dialog, whichButton) -> {
                    whiteList.clear();
                    MainActivity.prefs.edit().putString("whiteList", whiteList.toString()).apply();
                    refreshListView();
                })
                .setNegativeButton(R.string.whitelist_cancel_button, (dialog, whichButton) -> {}).show();
    }
    public void addRecommended(View view) {
        String externalDir = this.getExternalFilesDir(null).getPath();
        externalDir = externalDir.substring(0, externalDir.indexOf("Android"));
        if (!whiteList.contains(new File(externalDir, "Music").getPath())) {
            whiteList.add(new File(externalDir, "Music").getPath());
            whiteList.add(new File(externalDir, "Podcasts").getPath());
            whiteList.add(new File(externalDir, "Ringtones").getPath());
            whiteList.add(new File(externalDir, "Alarms").getPath());
            whiteList.add(new File(externalDir, "Notifications").getPath());
            whiteList.add(new File(externalDir, "Pictures").getPath());
            whiteList.add(new File(externalDir, "WhatsApp").getPath());
            whiteList.add(new File(externalDir, "GBWhatsApp").getPath());
            whiteList.add(new File(externalDir, "Movies").getPath());
            whiteList.add(new File(externalDir, "Download").getPath());
            whiteList.add(new File(externalDir, "DCIM").getPath());
            whiteList.add(new File(externalDir, "Documents").getPath());
            MainActivity.prefs.edit().putString("whiteList", whiteList.toString()).apply();
            refreshListView();
        } else
            Toast.makeText(this, R.string.whitelist_already_added,
                    Toast.LENGTH_LONG).show();
    }
    public final void addToWhiteList(View view) {
        final EditText input = new EditText(WhitelistActivity.this);
        new AlertDialog.Builder(WhitelistActivity.this, R.style.MyAlertDialogTheme)
                .setTitle(R.string.whitelist_add)
                .setMessage(R.string.whitelist_add_description)
                .setView(input)
                .setPositiveButton(R.string.whitelist_add_button, (dialog, whichButton) -> {
                    whiteList.add(String.valueOf(input.getText()));
                    MainActivity.prefs.edit().putString("whiteList", whiteList.toString()).apply();
                    refreshListView();
                })
                .setNegativeButton(R.string.whitelist_cancel_button, (dialog, whichButton) -> {}).show();
    }
    public void refreshListView() {
        runOnUiThread(() -> {
            adapter.notifyDataSetChanged();
            listView.invalidateViews();
            listView.refreshDrawableState();
        });
    }
    public static synchronized List < String > getWhiteList() {
        if (whiteList == null) {
            String whiteListString = MainActivity.prefs.getString("whiteList","No whitelist");
            String[] whitelistStrings = whiteListString.split(", ");
            whiteList = new LinkedList<>(Arrays.asList(whitelistStrings));
            whiteList.remove("[");
            whiteList.remove("]");
        }
        return whiteList;
    }
}