package dev.foss.goldenpath.notify

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.StalenessDays
import dev.foss.goldenpath.query.ScanHistoryStore
import dev.foss.goldenpath.staleness.Badge
import dev.foss.goldenpath.staleness.Staleness
import java.io.File

object WidgetRedCount {
    fun fromBadges(badges: Map<String, String>): Int =
        badges.values.count { it.equals("Red", ignoreCase = true) }

    fun fromApps(apps: List<InstalledApp>, nowMs: Long): Int {
        val days = StalenessDays.of(apps, nowMs)
        return days.values.count { age -> age != null && Staleness.badgeForDays(age) == Badge.Red }
    }

    fun fromStore(dir: File): Int = runCatching { fromBadges(ScanHistoryStore(dir).loadBadges()) }.getOrDefault(0)

    fun refresh(context: Context) {
        runCatching {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, StaleCountWidget::class.java))
            if (ids.isEmpty()) return
            val intent = Intent(context, StaleCountWidget::class.java).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}
