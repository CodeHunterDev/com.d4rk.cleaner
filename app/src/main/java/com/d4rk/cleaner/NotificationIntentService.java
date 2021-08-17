package com.d4rk.cleaner;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import androidx.core.app.NotificationManagerCompat;
public class NotificationIntentService extends IntentService {
    private static final int NOTIFICATION_ID = 3;
    public NotificationIntentService() {
        super("NotificationIntentService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("D4rK Cleaner!!!");
        builder.setContentText("You didn't cleaned your phone for some time. Try now!");
        builder.setSmallIcon(R.drawable.ic_splash_screen);
        Intent notifyIntent = new Intent(this, MainActivity.class);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //to be able to launch your activity from the notification
        builder.setContentIntent(pendingIntent);
        Notification notificationCompat = builder.build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(NOTIFICATION_ID, notificationCompat);
    }
}
