package dev.foss.goldenpath.inventory

import android.content.Context
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.foss.goldenpath.ui.theme.ThemeMode
import dev.foss.goldenpath.ui.theme.ThemePreferences
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

object SettingsPersistence {
    private const val BACKUP_FILE = "settings_backup.json"

    val userFiles = listOf(
        "pasted_repos.tsv",
        "direct_apks.tsv",
        "github_app_opts.tsv",
        "stale_snooze.tsv",
        "ignored_updates.tsv",
        "applied_updates.txt",
        "develop_next.tsv",
    )

    suspend fun backup(context: Context) = withContext(Dispatchers.IO) {
        runCatching {
            val json = exportJson(context)
            backupFiles(context).forEach { file ->
                file.parentFile?.mkdirs()
                file.writeText(json)
            }
        }
    }

    suspend fun restoreIfEmpty(context: Context) = withContext(Dispatchers.IO) {
        runCatching {
            val welcomePrefs = WelcomePrefs(context)
            val seen = welcomePrefs.seen.first()
            if (!seen) {
                val backup = backupFiles(context).firstOrNull { it.isFile && it.length() > 0 }
                if (backup != null) {
                    importJson(context, backup.readText())
                }
            }
        }
    }

    suspend fun exportJson(context: Context): String {
        val prefs = InventoryPreferences(context)
        val welcome = WelcomePrefs(context)
        val theme = ThemePreferences(context)
        val root = JsonObject()

        val settings = JsonObject().apply {
            addProperty("welcomeSeen", welcome.seen.first())
            addProperty("queryAllPackagesAck", prefs.queryAllPackagesAcknowledged.first())
            addProperty("includeSystemApps", prefs.includeSystemApps.first())
            addProperty("usageStatsConsent", prefs.usageStatsConsent.first().name)
            addProperty("sortMode", prefs.sortMode.first().name)
            addProperty("staleOnly", prefs.staleOnly.first())
            addProperty("updatesOnly", prefs.updatesOnly.first())
            addProperty("aptoideLookup", prefs.aptoideLookupEnabled.first())
            addProperty("apkMirrorLookup", prefs.apkMirrorLookupEnabled.first())
            addProperty("apkPureLookup", prefs.apkPureLookupEnabled.first())
            addProperty("playLookup", prefs.playLookupEnabled.first())
            addProperty("auroraPlay", prefs.auroraPlayEnabled.first())
            addProperty("forgeLookup", prefs.forgeLookupEnabled.first())
            addProperty("forgeLookupSearchUnknowns", prefs.forgeLookupSearchUnknowns.first())
            addProperty("scanInterval", prefs.scanInterval.first().name)
            addProperty("installMethod", prefs.installMethod.first().name)
            addProperty("updatePrefetch", prefs.updatePrefetchEnabled.first())
            addProperty("themeMode", theme.themeMode.first().name)
        }
        root.add("settings", settings)

        val filesObj = JsonObject()
        userFiles.forEach { name ->
            val file = File(context.filesDir, name)
            if (file.isFile) {
                filesObj.addProperty(name, file.readText())
            }
        }
        root.add("files", filesObj)

        return root.toString()
    }

    suspend fun importJson(context: Context, raw: String) {
        val root = runCatching { JsonParser.parseString(raw).asJsonObject }.getOrNull() ?: return
        val settings = root.getAsJsonObject("settings")
        if (settings != null) {
            val prefs = InventoryPreferences(context)
            val welcome = WelcomePrefs(context)
            val theme = ThemePreferences(context)

            if (settings.has("welcomeSeen") && settings.get("welcomeSeen").asBoolean) welcome.markSeen()
            if (settings.has("queryAllPackagesAck")) prefs.setQueryAllPackagesAcknowledged(settings.get("queryAllPackagesAck").asBoolean)
            if (settings.has("includeSystemApps")) prefs.setIncludeSystemApps(settings.get("includeSystemApps").asBoolean)
            if (settings.has("usageStatsConsent")) runCatching { prefs.setUsageStatsConsent(UsageStatsConsent.valueOf(settings.get("usageStatsConsent").asString)) }
            if (settings.has("sortMode")) runCatching { prefs.setSortMode(InventorySortMode.valueOf(settings.get("sortMode").asString)) }
            if (settings.has("staleOnly")) prefs.setStaleOnly(settings.get("staleOnly").asBoolean)
            if (settings.has("updatesOnly")) prefs.setUpdatesOnly(settings.get("updatesOnly").asBoolean)
            if (settings.has("aptoideLookup")) prefs.setAptoideLookupEnabled(settings.get("aptoideLookup").asBoolean)
            if (settings.has("apkMirrorLookup")) prefs.setApkMirrorLookupEnabled(settings.get("apkMirrorLookup").asBoolean)
            if (settings.has("apkPureLookup")) prefs.setApkPureLookupEnabled(settings.get("apkPureLookup").asBoolean)
            if (settings.has("playLookup")) prefs.setPlayLookupEnabled(settings.get("playLookup").asBoolean)
            if (settings.has("auroraPlay")) prefs.setAuroraPlayEnabled(settings.get("auroraPlay").asBoolean)
            if (settings.has("forgeLookup")) prefs.setForgeLookupEnabled(settings.get("forgeLookup").asBoolean)
            if (settings.has("forgeLookupSearchUnknowns")) prefs.setForgeLookupSearchUnknowns(settings.get("forgeLookupSearchUnknowns").asBoolean)
            if (settings.has("scanInterval")) runCatching { prefs.setScanInterval(ScanInterval.valueOf(settings.get("scanInterval").asString)) }
            if (settings.has("installMethod")) runCatching { prefs.setInstallMethod(InstallMethod.valueOf(settings.get("installMethod").asString)) }
            if (settings.has("updatePrefetch")) prefs.setUpdatePrefetchEnabled(settings.get("updatePrefetch").asBoolean)
            if (settings.has("themeMode")) runCatching { theme.setThemeMode(ThemeMode.valueOf(settings.get("themeMode").asString)) }
        }

        val filesObj = root.getAsJsonObject("files")
        if (filesObj != null) {
            userFiles.forEach { name ->
                if (filesObj.has(name)) {
                    val content = filesObj.get(name).asString
                    File(context.filesDir, name).writeText(content)
                }
            }
            IgnoredUpdates.hydrate(context.filesDir)
            AppliedUpdates.hydrate(context.filesDir)
        }
    }

    private fun backupFiles(context: Context): List<File> = listOfNotNull(
        File(context.filesDir, BACKUP_FILE),
        context.getExternalFilesDir("backup")?.let { File(it, BACKUP_FILE) },
    )
}
