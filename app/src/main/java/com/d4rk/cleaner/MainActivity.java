package com.d4rk.cleaner;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.d4rk.cleaner.clipboard.ClipboardActivity;
import com.d4rk.cleaner.compressor.CompressorActivity;
import com.d4rk.cleaner.rambooster.Home;
import com.google.android.material.navigation.NavigationView;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Objects;
public class MainActivity extends AppCompatActivity {
    private static final int NOTIFICATION_REMINDER_DAY = 3;
    final ConstraintSet constraintSet = new ConstraintSet();
    static boolean running = false;
    static SharedPreferences prefs;
    LinearLayout fileListView;
    ScrollView fileScrollView;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    ProgressBar scanPBar;
    TextView progressText;
    TextView statusText;
    final Context context = this;
    ConstraintLayout layout;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint({"RestrictedApi", "NonConstantResourceId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        WhitelistActivity.getWhiteList();
        fileListView = findViewById(R.id.fileListView);
        fileScrollView = findViewById(R.id.fileScrollView);
        scanPBar = findViewById(R.id.scanProgress);
        progressText = findViewById(R.id.ScanTextView);
        statusText = findViewById(R.id.statusTextView);
        layout = findViewById(R.id.main_layout);
        constraintSet.clone(layout);
        setUpToolbar();
        navigationView = findViewById(R.id.navigation_view);
        Intent notifyIntent = new Intent(this,NotificationReceiver.class);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getBroadcast
                (context, NOTIFICATION_REMINDER_DAY, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,  System.currentTimeMillis(),
                1000 * 60 * 60 * 24, pendingIntent);
        @SuppressLint("RestrictedApi")
        ShortcutInfoCompat shortcut = new ShortcutInfoCompat.Builder(context, "atm_shortcut")
                .setShortLabel(getString(R.string.atmegame))
                .setLongLabel(getString(R.string.long_shortcut_atmegame))
                .setIcon(IconCompat.createFromIcon(Icon.createWithResource(context, R.mipmap.ic_launch_atmegame)))
                .setIntent(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.atmegame.com/?utm_source=D4Cleaner&utm_medium=D4Cleaner")))
                .build();
        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId())
            {
                case R.id.nav_drawer_settings:
                    Intent intent = new Intent (MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    break;
                case R.id.nav_drawer_whitelist:
                    intent = new Intent (MainActivity.this, WhitelistActivity.class);
                    startActivity(intent);
                    break;
                case R.id.nav_drawer_clipboard_cleaner:
                    intent = new Intent (MainActivity.this, ClipboardActivity.class);
                    startActivity(intent);
                    break;
                case R.id.nav_drawer_image_compressor:
                    intent = new Intent (MainActivity.this, CompressorActivity.class);
                    startActivity(intent);
                    break;
                case R.id.nav_drawer_ram_booster:
                    intent = new Intent (MainActivity.this, Home.class);
                    startActivity(intent);
                    break;
                case R.id.nav_drawer_about:
                    intent = new Intent (MainActivity.this, AboutActivity.class);
                    startActivity(intent);
                    break;
                case R.id.nav_drawer_support:
                    Intent newIntent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("https://www.paypal.me/d4rkmichaeltutorials"));
                    startActivity(newIntent);
                    break;
                case  R.id.nav_drawer_share:{
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    String shareBody =  "https://play.google.com/store/apps/details?id=com.d4rk.cleaner";
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Try right now!");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    startActivity(Intent.createChooser(sharingIntent, "Share using..."));
                }
                break;
            }
            return false;
        });
    }
    public final void clean(View view) {
        requestWriteExternalPermission();
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
    public final void link(View view) {
        Intent openURL = new Intent(android.content.Intent.ACTION_VIEW);
        openURL.setData(Uri.parse("https://www.atmegame.com/?utm_source=D4Cleaner&utm_medium=D4Cleaner"));
        startActivity(openURL);
    }
    public void setUpToolbar() {
        drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
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
        runOnUiThread(()->findViewById(R.id.cleanButton).setEnabled(!running));
        reset();
        File path = Environment.getExternalStorageDirectory();
        FileScanner fs = new FileScanner(path, this);
        fs.setEmptyDir(prefs.getBoolean("empty", false));
        fs.setAutoWhite(prefs.getBoolean("auto_white", true));
        fs.setDelete(delete);
        fs.setCorpse(prefs.getBoolean("corpse", false));
        fs.setGUI(this);
        fs.setUpFilters(prefs.getBoolean("generic", true),
                prefs.getBoolean("aggressive", false),
                prefs.getBoolean("true_aggressive", false),
                prefs.getBoolean("apk", false));
        if (path.listFiles() == null) {
            TextView textView = printTextView(printTextView(), Color.RED);
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
        runOnUiThread(()->findViewById(R.id.cleanButton).setEnabled(!running));
        Looper.loop();
    }
    private String printTextView() {
        return null;
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
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE},
                    1);
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(this, "Permission needed!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
            if (!isAccessGranted()) {
                Intent intent;
                intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isAccessGranted() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode;
            mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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