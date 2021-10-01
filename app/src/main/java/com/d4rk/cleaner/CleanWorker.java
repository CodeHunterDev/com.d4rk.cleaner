package com.d4rk.cleaner;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.File;
public class CleanWorker extends Worker {
    private static final String CHANNEL_ID = "cleanworker";
    private boolean isRunning = false;
    public CleanWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    @NonNull
    @Override
    public Result doWork() {
        if (MainActivity.prefs.getBoolean("dailyclean",false) && !isRunning) scan();
        return Result.success();
    }
    private synchronized void scan() {
        isRunning = true;
        File path = Environment.getExternalStorageDirectory();
        FileScanner fs = new FileScanner(path,getApplicationContext())
                .setEmptyDir(MainActivity.prefs.getBoolean("empty", false))
                .setAutoWhite(MainActivity.prefs.getBoolean("auto_white", true))
                .setDelete(true)
                .setCorpse(MainActivity.prefs.getBoolean("corpse", false))
                .setGUI(null)
                .setContext(getApplicationContext())
                .setUpFilters(
                        MainActivity.prefs.getBoolean("generic", true),
                        MainActivity.prefs.getBoolean("aggressive", false),
                        MainActivity.prefs.getBoolean("true_aggressive", false),
                        MainActivity.prefs.getBoolean("apk", false));
        long kilobytesTotal = fs.startScan();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String title = "Cleaned:"+" "+MainActivity.convertSize(kilobytesTotal);
            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,"name", NotificationManager.IMPORTANCE_HIGH);
            PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),1,intent,0);
            Notification notification=new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentIntent(pendingIntent)
                    .setChannelId(CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_app)
                    .build();
            NotificationManager notificationManager=(NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
            notificationManager.notify(1,notification);
        }
        isRunning = false;
    }
}