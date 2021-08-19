package com.d4rk.cleaner;
import static android.app.NotificationChannel.DEFAULT_CHANNEL_ID;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import androidx.core.app.NotificationCompat;
public class NotificationUtils extends ContextWrapper {
    private static final CharSequence TIMELINE_CHANNEL_NAME = "Timeline notification";
    private NotificationManager _notificationManager;

    public NotificationUtils(Context base) {
        super(base);
        createChannel();
    }
    public NotificationCompat.Builder setNotification(String title, String body) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_splash_screen)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        }
        return null;
    }
    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(DEFAULT_CHANNEL_ID, TIMELINE_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            getManager().createNotificationChannel(channel);
        }
    }
    public NotificationManager getManager() {
        if (_notificationManager == null) {
            _notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return _notificationManager;
    }
}