package com.d4rk.cleaner.rambooster;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.d4rk.cleaner.R;
import com.d4rk.cleaner.rambooster.interfaces.CleanListener;
import com.d4rk.cleaner.rambooster.interfaces.ScanListener;
import com.d4rk.cleaner.rambooster.utils.ProcessInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
public class Home extends AppCompatActivity {
    private RAMBooster booster;
    private final String TAG = "Booster.Test";
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_ram_booster);
        btn = findViewById(R.id.cleanRAMButton);
        btn.setOnClickListener(v -> {
            booster = new RAMBooster(Home.this);
            booster.setDebug(true);
            booster.setScanListener(new ScanListener() {
                public void onStarted() {
                    Log.d(TAG, "Scan started");
                }
                public void onClick() {
                }
                public boolean onSupportNavigateUp() {
                    onBackPressed();
                    return true;
                }
                public void onFinished(long availableRam, long totalRam, List < ProcessInfo > appsToClean) {
                    Log.d(TAG, String.format(Locale.US, "Scan finished, available RAM: %dMB, total RAM: %dMB", availableRam, totalRam));
                    List < String > apps = new ArrayList < > ();
                    for (ProcessInfo info: appsToClean) {
                        apps.add(info.getProcessName());
                    }
                    Log.d(TAG, String.format(Locale.US, "Going to clean founded processes: %s", Arrays.toString(apps.toArray())));
                    booster.startClean();
                }
            });
            booster.setCleanListener(new CleanListener() {
                public void onStarted() {
                    Log.d(TAG, "Clean started");
                }
                public void onFinished(long availableRam, long totalRam) {
                    Log.d(TAG, String.format(Locale.US,
                            "Clean finished, available RAM: %dMB, total RAM: %dMB",
                            availableRam, totalRam));
                    booster = null;
                }
            });
            booster.startScan(true);
            Toast.makeText(getBaseContext(), "Clean finished, RAM freed!",  Toast.LENGTH_LONG).show();
        });
    }
    public void onBoost(View view) {
    }
}