package dev.foss.goldenpath.index.aurora

import android.content.Context
import com.aurora.gplayapi.data.models.PlayFile
import com.aurora.gplayapi.helpers.AppDetailsHelper
import com.aurora.gplayapi.helpers.PurchaseHelper

/** Live gplayapi purchase. Fail-soft to empty so Update can open Play Store. */
object AuroraPlayLive {
    fun files(context: Context): AuroraPlayFiles = AuroraPlayFiles { pkg ->
        purchase(context, pkg, refresh = false).ifEmpty { purchase(context, pkg, refresh = true) }
    }

    private fun purchase(context: Context, packageName: String, refresh: Boolean): List<AuroraPlayFile> {
        return runCatching {
            val store = EncryptedAuroraAuthStore.create(context)
            val props = AuroraDeviceProps.json(context)
            val auth = if (refresh) {
                AuroraAuth.refresh(store, props)
            } else {
                AuroraAuth.loadOrRefresh(store, props)
            } ?: return emptyList()
            val app = AppDetailsHelper(auth).using(AuroraPlayHttp).getAppByPackageName(packageName)
            if (app.packageName.isBlank() || app.versionCode <= 0) return emptyList()
            PurchaseHelper(auth).using(AuroraPlayHttp)
                .purchase(app.packageName, app.versionCode, app.offerType)
                .filter { it.type == PlayFile.Type.BASE || it.type == PlayFile.Type.SPLIT }
                .mapNotNull { file ->
                    val url = file.url.trim()
                    if (url.isEmpty()) null else AuroraPlayFile(url, app.versionName, app.versionCode)
                }
        }.getOrDefault(emptyList())
    }
}
