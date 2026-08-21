package dev.foss.goldenpath.index.aurora

import android.app.ActivityManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale
import java.util.Properties

/** Device property bag for Aurora's anonymous Play auth, same fields APKUpdater sends. */
object AuroraDeviceProps {
    fun json(context: Context): ByteArray {
        val app = context.applicationContext
        val config = app.resources.configuration
        val metrics = app.resources.displayMetrics
        val props = Properties()
        props["UserReadableName"] = "${Build.DEVICE}-default"
        props["Build.HARDWARE"] = Build.HARDWARE
        props["Build.RADIO"] = Build.getRadioVersion() ?: "unknown"
        props["Build.FINGERPRINT"] = Build.FINGERPRINT
        props["Build.BRAND"] = Build.BRAND
        props["Build.DEVICE"] = Build.DEVICE
        props["Build.VERSION.SDK_INT"] = "${Build.VERSION.SDK_INT}"
        props["Build.VERSION.RELEASE"] = Build.VERSION.RELEASE
        props["Build.MODEL"] = Build.MODEL
        props["Build.MANUFACTURER"] = Build.MANUFACTURER
        props["Build.PRODUCT"] = Build.PRODUCT
        props["Build.ID"] = Build.ID
        props["Build.BOOTLOADER"] = Build.BOOTLOADER
        props["TouchScreen"] = "${config.touchscreen}"
        props["Keyboard"] = "${config.keyboard}"
        props["Navigation"] = "${config.navigation}"
        props["ScreenLayout"] = "${config.screenLayout and 15}"
        props["HasHardKeyboard"] = "${config.keyboard == Configuration.KEYBOARD_QWERTY}"
        props["HasFiveWayNavigation"] = "${config.navigation == Configuration.NAVIGATIONHIDDEN_YES}"
        props["Screen.Density"] = "${metrics.densityDpi}"
        props["Screen.Width"] = "${metrics.widthPixels}"
        props["Screen.Height"] = "${metrics.heightPixels}"
        props["Platforms"] = Build.SUPPORTED_ABIS.joinToString(",")
        props["Features"] = features(app)
        props["Locales"] = app.assets.locales.filter { it.isNotEmpty() }.joinToString(",") { it.replace("-", "_") }
        props["SharedLibraries"] = app.packageManager.systemSharedLibraryNames?.joinToString(",").orEmpty()
        val gl = (app.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)
            ?.deviceConfigurationInfo?.reqGlEsVersion?.toString() ?: "0"
        props["GL.Version"] = gl
        props["GL.Extensions"] = ""
        props["Client"] = "android-google"
        props["GSF.version"] = "203615037"
        props["Vending.version"] = "82201710"
        props["Vending.versionString"] = "22.0.17-21 [0] [PR] 332555730"
        props["Roaming"] = "mobile-notroaming"
        props["TimeZone"] = "UTC-10"
        props["CellOperator"] = "310"
        props["SimOperator"] = "38"
        if (isHuawei()) stripHuawei(props)
        return com.google.gson.Gson().toJson(props).toByteArray(Charsets.UTF_8)
    }

    private fun features(context: Context): String =
        runCatching {
            context.packageManager.systemAvailableFeatures.mapNotNull { it.name }.joinToString(",")
        }.getOrDefault("")

    private fun isHuawei(): Boolean {
        val maker = Build.MANUFACTURER.lowercase(Locale.US)
        val hardware = Build.HARDWARE.lowercase(Locale.US)
        return maker.contains("huawei") || hardware.contains("kirin") || hardware.contains("hi3")
    }

    private fun stripHuawei(props: Properties) {
        props["Build.HARDWARE"] = "lynx"
        props["Build.BOOTLOADER"] = "lynx-1.0-9716681"
        props["Build.BRAND"] = "google"
        props["Build.DEVICE"] = "lynx"
        props["Build.MODEL"] = "Pixel 7a"
        props["Build.MANUFACTURER"] = "Google"
        props["Build.PRODUCT"] = "lynx"
        props["Build.ID"] = "TQ2A.230505.002"
    }
}
