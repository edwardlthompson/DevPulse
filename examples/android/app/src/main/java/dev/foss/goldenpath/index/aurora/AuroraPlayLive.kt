package dev.foss.goldenpath.index.aurora

import android.content.Context
import com.aurora.gplayapi.data.models.PlayFile
import com.aurora.gplayapi.helpers.AppDetailsHelper
import com.aurora.gplayapi.helpers.PurchaseHelper
import dev.foss.goldenpath.inventory.RefreshTrace

/** Live gplayapi purchase and Play-equivalent details. Fail-soft so Update can open Play Store. */
object AuroraPlayLive {
    fun files(context: Context): AuroraPlayFiles = AuroraPlayFiles { pkg ->
        purchase(context, pkg, refresh = false).ifEmpty { purchase(context, pkg, refresh = true) }
    }

    fun details(context: Context): AuroraPlayDetails = AuroraPlayDetails { names ->
        lookupMany(context, names)
    }

    private fun lookupMany(context: Context, names: List<String>): Map<String, AuroraPlayApp> {
        val wanted = names.map { it.trim() }.filter { it.isNotEmpty() }
        if (wanted.isEmpty()) return emptyMap()
        val now = System.currentTimeMillis()
        val auth = runCatching {
            val store = EncryptedAuroraAuthStore.create(context)
            AuroraAuth.loadOrRefresh(store, AuroraDeviceProps.json(context))
        }.getOrNull()
        if (auth == null) return wanted.associateWith { AuroraPlayApp(AuroraPlayStatus.Unknown) }
        return runCatching {
            val helper = AppDetailsHelper(auth).using(AuroraPlayHttp)
            fun page(names: List<String>): Map<String, AuroraPlayApp> =
                helper.getAppByPackageName(names).mapNotNull { app ->
                    val pkg = app.packageName.trim()
                    if (pkg.isEmpty()) null
                    else pkg to AuroraPlayLookup.fromFields(app.versionName, app.versionCode, app.updatedOn, now)
                }.toMap()
            page(wanted)
        }.getOrElse {
            RefreshTrace.line("aurora lookup fail ${it.javaClass.simpleName}: ${it.message}")
            wanted.associateWith { AuroraPlayApp(AuroraPlayStatus.Unknown) }
        }
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
                    if (url.isEmpty()) {
                        null
                    } else {
                        AuroraPlayFile(url, app.versionName, app.versionCode, file.type == PlayFile.Type.BASE)
                    }
                }
        }.getOrDefault(emptyList())
    }
}
