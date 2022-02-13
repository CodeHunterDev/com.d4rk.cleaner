package com.d4rk.cleaner.clipboard.widget
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.d4rk.cleaner.clipboard.ACTION_CONTENT
import com.d4rk.cleaner.clipboard.IntentActivity
import com.d4rk.cleaner.R
import com.d4rk.cleaner.clipboard.pendingActivityIntent
class ContentWidget : AppWidgetProvider() {
    companion object {
        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_content)
            val pi = context.pendingActivityIntent(
                IntentActivity.activityIntent(context, ACTION_CONTENT)
            )
            views.setOnClickPendingIntent(R.id.layout, pi)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}