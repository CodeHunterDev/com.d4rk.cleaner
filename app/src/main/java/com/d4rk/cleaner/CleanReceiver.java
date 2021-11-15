package com.d4rk.cleaner;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
public class CleanReceiver extends BroadcastReceiver {
    private static final int PERIOD = 86400000;
    private static final int INITIAL_DELAY = 3600000;
    @Override
    public void onReceive(Context ctxt, Intent i) {
        if (i.getAction() == null) {
            ScheduledService.enqueueWork(ctxt);
        } else {
            scheduleAlarm(ctxt);
        }
    }
    public static void scheduleAlarm(Context ctxt) {
        AlarmManager mgr =
                (AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(ctxt, CleanReceiver.class);
        PendingIntent pendingintent;
        pendingintent = PendingIntent.getBroadcast(ctxt, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        mgr.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + INITIAL_DELAY,
                PERIOD, pendingintent);
    }
    public static void cancelAlarm(Context ctxt) {
        AlarmManager mgr =
                (AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(ctxt, CleanReceiver.class);
        PendingIntent pendingintent;
        pendingintent = PendingIntent.getBroadcast(ctxt, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        mgr.cancel(pendingintent);
    }
}