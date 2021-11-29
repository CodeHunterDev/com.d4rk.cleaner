package com.d4rk.cleaner
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import com.d4rk.cleaner.MainActivity.Companion.convertSize
class ScheduledService : JobIntentService() {
    public override fun onHandleWork(i: Intent) {
        try {
            val path = Environment.getExternalStorageDirectory()
            val prefs = PreferenceManager.getDefaultSharedPreferences(
                applicationContext
            )
            val fs = FileScanner(path, applicationContext)
                .setEmptyDir(prefs.getBoolean("empty", false))
                .setAutoWhite(prefs.getBoolean("auto_white", true))
                .setDelete(true)
                .setCorpse(prefs.getBoolean("corpse", false))
                .setGUI(null)
                .setContext(applicationContext)
                .setUpFilters(
                    prefs.getBoolean("generic", true),
                    prefs.getBoolean("aggressive", false),
                    prefs.getBoolean("true_aggressive", false),
                    prefs.getBoolean("apk", false)
                )
            val kilobytesTotal = fs.startScan()
            val title =
                applicationContext.getString(R.string.service_notif_title) + " " + convertSize(
                    kilobytesTotal
                )
            makeStatusNotification(title, applicationContext)
        } catch (e: Exception) {
            makeStatusNotification(e.toString(), applicationContext)
        }
    }
    companion object {
        private const val UNIQUE_JOB_ID = 1337
        @JvmStatic
        fun enqueueWork(context: Context) {
            enqueueWork(
                context.applicationContext, ScheduledService::class.java, UNIQUE_JOB_ID,
                Intent(context, ScheduledService::class.java)
            )
        }
        fun makeStatusNotification(message: String?, context: Context) {
            val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence = context.getString(R.string.settings_notification_name)
            val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION = context.getString(R.string.settings_notification_sum)
            val NOTIFICATION_TITLE: CharSequence = context.getString(R.string.service_channel_name)
            val CHANNEL_ID = "VERBOSE_NOTIFICATION"
            val NOTIFICATION_ID = 1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, VERBOSE_NOTIFICATION_CHANNEL_NAME, importance)
                channel.description = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
                val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_app)
                .setContentTitle(NOTIFICATION_TITLE)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVibrate(LongArray(0))
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        }
    }
}