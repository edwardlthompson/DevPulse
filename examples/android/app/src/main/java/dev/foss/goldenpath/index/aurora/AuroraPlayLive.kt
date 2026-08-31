package dev.foss.goldenpath.index.aurora

import android.content.Context
import com.aurora.gplayapi.data.models.PlayFile
import com.aurora.gplayapi.helpers.AppDetailsHelper
import com.aurora.gplayapi.helpers.PurchaseHelper
import dev.foss.goldenpath.inventory.AuroraPlayWhy
import dev.foss.goldenpath.inventory.InstallWhy
import dev.foss.goldenpath.inventory.RefreshTrace
import java.util.concurrent.ConcurrentHashMap

/** Live gplayapi purchase and Play-equivalent details. Fail-soft so Update can open Play Store. */
object AuroraPlayLive {
    private val lastWhy = ConcurrentHashMap<String, InstallWhy>()

    @Volatile
    private var sessionHeld = false

    fun why(packageName: String): InstallWhy =
        lastWhy[packageName.trim()] ?: InstallWhy.NoFile

    fun clearWhy() {
        lastWhy.clear()
    }

    fun holdSession() {
        sessionHeld = true
        lastWhy.clear()
    }

    fun releaseSession() {
        sessionHeld = false
    }

    internal fun retryAfterEmpty(why: InstallWhy): Boolean =
        why != InstallWhy.PlayPurchase && !sessionHeld

    internal fun markWhy(packageName: String, why: InstallWhy) {
        val pkg = packageName.trim()
        if (pkg.isNotEmpty()) lastWhy[pkg] = why
    }

    fun files(context: Context): AuroraPlayFiles = AuroraPlayFiles { pkg ->
        note(pkg, InstallWhy.NoFile)
        val first = purchase(context, pkg, refresh = false)
        if (first.isNotEmpty() || !retryAfterEmpty(why(pkg))) return@AuroraPlayFiles first
        purchase(context, pkg, refresh = true)
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
                    else {
                        if (pkg.contains("ingress", ignoreCase = true) || pkg.contains("niantic", ignoreCase = true) || pkg.contains("temu", ignoreCase = true) || pkg.contains("mapgenie", ignoreCase = true) || pkg.contains("komoot", ignoreCase = true) || pkg.contains("tachyon", ignoreCase = true)) {
                            RefreshTrace.line("aurora target: pkg=$pkg vName=${app.versionName} vCode=${app.versionCode} updatedOn=${app.updatedOn}")
                        }
                        pkg to AuroraPlayLookup.fromFields(app.versionName, app.versionCode, app.updatedOn, now)
                    }
                }.toMap()
            page(wanted)
        }.getOrElse {
            RefreshTrace.line("aurora lookup fail ${it.javaClass.simpleName}: ${it.message}")
            wanted.associateWith { AuroraPlayApp(AuroraPlayStatus.Unknown) }
        }
    }

    private fun purchase(context: Context, packageName: String, refresh: Boolean): List<AuroraPlayFile> {
        val pkg = packageName.trim()
        return runCatching {
            val store = EncryptedAuroraAuthStore.create(context)
            val props = AuroraDeviceProps.json(context)
            val auth = if (refresh) {
                AuroraAuth.refresh(store, props)
            } else {
                AuroraAuth.loadOrRefresh(store, props)
            }
            if (auth == null) {
                RefreshTrace.line("aurora $pkg no auth")
                return emptyList()
            }
            val app = AppDetailsHelper(auth).using(AuroraPlayHttp).getAppByPackageName(pkg)
            if (app.packageName.isBlank() || app.versionCode <= 0) {
                RefreshTrace.line("aurora $pkg no details")
                return emptyList()
            }
            val bought = PurchaseHelper(auth).using(AuroraPlayHttp)
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
            if (bought.isEmpty()) RefreshTrace.line("aurora $pkg empty files")
            bought
        }.onFailure {
            val mapped = AuroraPlayWhy.of(it)
            note(pkg, mapped)
            RefreshTrace.line("aurora $pkg ${it.javaClass.simpleName}: ${it.message}")
        }.getOrDefault(emptyList())
    }

    private fun note(packageName: String, why: InstallWhy) {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return
        lastWhy[pkg] = why
    }
}
