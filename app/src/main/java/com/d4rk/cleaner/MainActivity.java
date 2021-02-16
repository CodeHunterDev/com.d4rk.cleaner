package com.d4rk.cleaner;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.transition.TransitionManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import com.fxn.stash.Stash;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Objects;
@SuppressWarnings({"unused", "ConstantConditions"})
public class MainActivity extends AppCompatActivity {
    final ConstraintSet constraintSet = new ConstraintSet();
    static boolean running = false;
    SharedPreferences prefs;
    LinearLayout fileListView;
    ScrollView fileScrollView;
    ProgressBar scanPBar;
    TextView progressText;
    TextView statusText;
    ConstraintLayout layout;
    @SuppressLint("LogConditional")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar);
        View view =getSupportActionBar().getCustomView();
        Stash.init(getApplicationContext());
        fileListView = findViewById(R.id.fileListView);
        fileScrollView = findViewById(R.id.fileScrollView);
        scanPBar = findViewById(R.id.scanProgress);
        progressText = findViewById(R.id.ScanTextView);
        statusText = findViewById(R.id.statusTextView);
        layout = findViewById(R.id.main_layout);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        constraintSet.clone(layout);
        requestWriteExternalPermission();
        Window w = getWindow();
        if (!isAccessGranted()) {
            Intent intent = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            }
            startActivity(intent);
        }
    }
    public final void settings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    public final void clean(View view) {
        if (!running) {
            if (!prefs.getBoolean("one_click", false))
                new AlertDialog.Builder(this,R.style.MyAlertDialogTheme)
                        .setTitle(R.string.main_select_task)
                        .setMessage(R.string.main_select_task_description)
                        .setPositiveButton(R.string.main_clean, (dialog, whichButton) -> new Thread(()-> scan(true)).start())
                        .setNegativeButton(R.string.main_analyze, (dialog, whichButton) -> new Thread(()-> scan(false)).start()).show();
            else new Thread(()-> scan(true)).start();
        }
    }
    public static void setWindowFlag(Activity activity, final int bits, boolean on) {

        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        }
        {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }
    public void animateBtn() {
        TransitionManager.beginDelayedTransition(layout);
        constraintSet.clear(R.id.cleanButton,ConstraintSet.TOP);
        constraintSet.clear(R.id.statusTextView,ConstraintSet.BOTTOM);
        constraintSet.setMargin(R.id.statusTextView,ConstraintSet.TOP,50);
        constraintSet.applyTo(layout);
    }
    @SuppressLint("SetTextI18n")
    private void scan(boolean delete) {
        Looper.prepare();
        running = true;
        reset();
        File path = Environment.getExternalStorageDirectory();
        FileScanner fs = new FileScanner(path);
        fs.setEmptyDir(prefs.getBoolean("empty", false));
        fs.setAutoWhite(prefs.getBoolean("auto_white", true));
        fs.setDelete(delete);
        fs.setCorpse(prefs.getBoolean("corpse", false));
        fs.setGUI(this);
        fs.setUpFilters(prefs.getBoolean("generic", true),
                prefs.getBoolean("aggressive", false),
                prefs.getBoolean("apk", false));
        if (path.listFiles() == null) { // is this needed? yes.
            TextView textView = printTextView("Scan failed.", Color.RED);
            runOnUiThread(() -> fileListView.addView(textView));
        }
        runOnUiThread(() -> {
            animateBtn();
            statusText.setText(getString(R.string.main_status_running));
        });
        long kilobytesTotal = fs.startScan();
        runOnUiThread(() -> {
            scanPBar.setProgress(scanPBar.getMax());
            progressText.setText("100%");
        });
        runOnUiThread(() -> {
            if (delete) {
                statusText.setText(getString(R.string.main_freed) + " " + convertSize(kilobytesTotal));
            } else {
                statusText.setText(getString(R.string.main_found) + " " + convertSize(kilobytesTotal));
            }
        });
        fileScrollView.post(() -> fileScrollView.fullScroll(ScrollView.FOCUS_DOWN));
        running = false;
        Looper.loop();
    }
    private synchronized TextView printTextView(String text, int color) {
        TextView textView = new TextView(MainActivity.this);
        textView.setTextColor(color);
        textView.setText(text);
        textView.setPadding(3,3,3,3);
        return textView;
    }
    private String convertSize(long length) {
        final DecimalFormat format = new DecimalFormat("#.##");
        final long MiB = 1024 * 1024;
        final long KiB = 1024;
        if (length > MiB) {
            return format.format(length / MiB) + " MB";
        }
        if (length > KiB) {
            return format.format(length / KiB) + " KB";
        }
        return format.format(length) + " B";
    }
    synchronized TextView displayPath(File file) {
        TextView textView = printTextView(file.getAbsolutePath(), getResources().getColor(R.color.colorAccent));
        runOnUiThread(() -> fileListView.addView(textView));
        fileScrollView.post(() -> fileScrollView.fullScroll(ScrollView.FOCUS_DOWN));
        return textView;
    }
    private synchronized void reset() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        runOnUiThread(() -> {
            fileListView.removeAllViews();
            scanPBar.setProgress(0);
            scanPBar.setMax(1);
        });
    }
    public synchronized void requestWriteExternalPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE,},
                    2);
        }
    }
    private boolean isAccessGranted() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                            applicationInfo.uid, applicationInfo.packageName);
                }
            }
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 &&
                grantResults.length > 0 &&
                grantResults[0] != PackageManager.PERMISSION_GRANTED)
            prompt();
    }
    public final void prompt() {
        Intent intent = new Intent(this, PromptActivity.class);
        startActivity(intent);
    }
}