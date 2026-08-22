package dev.foss.goldenpath.notify

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import dev.foss.goldenpath.MainActivity
import dev.foss.goldenpath.R

class RefreshNotifier(private val context: Context) {
    private val manager = context.getSystemService(NotificationManager::class.java)

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            RefreshNotifyCopy.CHANNEL_ID,
            context.getString(R.string.refresh_notify_channel),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        channel.description = context.getString(R.string.refresh_notify_channel)
        manager.createNotificationChannel(channel)
    }

    fun progress(done: Int, total: Int, location: String = ""): Notification {
        val text = when {
            total <= 0 -> context.getString(R.string.inventory_refreshing)
            else -> context.getString(R.string.inventory_refresh_progress, done, total)
        }
        val indeterminate = total <= 0
        return base(context.getString(R.string.refresh_notify_title), text, ongoing = true)
            .setOnlyAlertOnce(true)
            .setProgress(total.coerceAtLeast(1), done.coerceAtLeast(0), indeterminate)
            .build()
    }

    fun postProgress(done: Int, total: Int, location: String = "") {
        manager.notify(RefreshNotifyCopy.PROGRESS_ID, progress(done, total, location))
    }

    fun postDone(lookedUp: Int) {
        manager.cancel(RefreshNotifyCopy.PROGRESS_ID)
        val notification = base(
            context.getString(R.string.refresh_notify_done_title),
            context.getString(R.string.refresh_notify_done_text, lookedUp),
            ongoing = false,
        ).setAutoCancel(true).build()
        manager.notify(RefreshNotifyCopy.DONE_ID, notification)
    }

    private fun base(title: String, text: String, ongoing: Boolean): NotificationCompat.Builder {
        val launch = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending = PendingIntent.getActivity(
            context,
            0,
            launch,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(context, RefreshNotifyCopy.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_brand_mark)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pending)
            .setOngoing(ongoing)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
    }
}
