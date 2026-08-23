package dev.foss.goldenpath.notify

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dev.foss.goldenpath.MainActivity
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.UpdateInventory

object UpdatesNotify {
    const val NOTICE_ID = 82

    fun count(apps: List<InstalledApp>): Int = UpdateInventory.withUpdates(apps).size

    fun post(context: Context, apps: List<InstalledApp>) {
        val n = count(apps)
        if (n <= 0) return
        StaleNotifier(context).ensureChannel()
        val launch = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending = PendingIntent.getActivity(
            context,
            2,
            launch,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        context.getSystemService(NotificationManager::class.java).notify(
            NOTICE_ID,
            NotificationCompat.Builder(context, StaleNotifyCopy.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_brand_mark)
                .setContentTitle(context.getString(R.string.inventory_update_available))
                .setContentText(context.getString(R.string.update_all, n))
                .setContentIntent(pending)
                .setAutoCancel(true)
                .build(),
        )
    }
}
