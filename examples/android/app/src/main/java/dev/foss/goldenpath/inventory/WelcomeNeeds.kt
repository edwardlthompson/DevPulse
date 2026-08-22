package dev.foss.goldenpath.inventory

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

enum class WelcomeNeed { Apps, Notifications, Install, Usage }

enum class WelcomeHome { Splash, Welcome, Inventory }

data class WelcomeRow(
    val need: WelcomeNeed,
    val required: Boolean,
    val granted: Boolean,
)

object WelcomeNeeds {
    fun notifyGranted(context: Context, sdkInt: Int = Build.VERSION.SDK_INT): Boolean =
        sdkInt < 33 || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

    fun installGranted(context: Context, sdkInt: Int = Build.VERSION.SDK_INT): Boolean =
        sdkInt < 26 || context.packageManager.canRequestPackageInstalls()

    fun rows(
        appsAccepted: Boolean,
        notifyGranted: Boolean,
        installGranted: Boolean,
        usageGranted: Boolean,
        sdkInt: Int = Build.VERSION.SDK_INT,
    ): List<WelcomeRow> = listOf(
        WelcomeRow(WelcomeNeed.Apps, required = true, granted = appsAccepted),
        WelcomeRow(WelcomeNeed.Notifications, required = sdkInt >= 33, granted = notifyGranted),
        WelcomeRow(WelcomeNeed.Install, required = true, granted = installGranted),
        WelcomeRow(WelcomeNeed.Usage, required = false, granted = usageGranted),
    )

    fun ready(rows: List<WelcomeRow>): Boolean = rows.filter { it.required }.all { it.granted }

    fun seen(welcomeSeen: Boolean, acknowledged: Boolean): Boolean = welcomeSeen || acknowledged

    fun home(loadedSeen: Boolean?, canScan: Boolean): WelcomeHome = when (loadedSeen) {
        null -> WelcomeHome.Splash
        false -> WelcomeHome.Welcome
        true -> if (canScan) WelcomeHome.Inventory else WelcomeHome.Splash
    }

    fun openInstallSettings(context: Context) {
        val uri = Uri.parse("package:${context.packageName}")
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, uri)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }
}
