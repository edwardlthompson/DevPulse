package dev.foss.goldenpath.notify

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import dev.foss.goldenpath.MainActivity
import dev.foss.goldenpath.R
import java.io.File

class StaleCountWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val count = runCatching {
            WidgetRedCount.fromStore(File(context.filesDir, "scan-history"))
        }.getOrDefault(0)
        val tap = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(dev.foss.goldenpath.inventory.RefreshLaunch.EXTRA_UPDATES_ONLY, true),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        ids.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_stale)
            views.setTextViewText(R.id.widget_red_count, count.toString())
            views.setOnClickPendingIntent(R.id.widget_red_count, tap)
            manager.updateAppWidget(id, views)
        }
    }
}
