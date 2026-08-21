package dev.foss.goldenpath.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import dev.foss.goldenpath.MainActivity
import dev.foss.goldenpath.R

object StaleNotifyCopy {
    const val CHANNEL_ID = "stale_crossing"
    const val NOTICE_ID = 81
}

class StaleNotifier(private val context: Context) {
    private val manager = context.getSystemService(NotificationManager::class.java)

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        manager.createNotificationChannel(
            NotificationChannel(
                StaleNotifyCopy.CHANNEL_ID,
                context.getString(R.string.stale_notify_channel),
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
    }

    fun postCrossings(hits: List<Pair<Int, List<String>>>) {
        if (hits.isEmpty()) return
        ensureChannel()
        val text = hits.joinToString(" · ") { (days, pkgs) ->
            context.getString(R.string.stale_notify_text, pkgs.size, days)
        }
        val launch = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending = PendingIntent.getActivity(
            context,
            1,
            launch,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        manager.notify(
            StaleNotifyCopy.NOTICE_ID,
            NotificationCompat.Builder(context, StaleNotifyCopy.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_brand_mark)
                .setContentTitle(context.getString(R.string.stale_notify_title))
                .setContentText(text)
                .setContentIntent(pending)
                .setAutoCancel(true)
                .build(),
        )
    }
}
