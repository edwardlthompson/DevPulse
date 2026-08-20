package dev.foss.goldenpath.inventory

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val QUERY_ALL_PACKAGES_ACK = booleanPreferencesKey("query_all_packages_ack")
private val INCLUDE_SYSTEM_APPS = booleanPreferencesKey("include_system_apps")
private val USAGE_STATS_CONSENT = stringPreferencesKey("usage_stats_consent")
private val SORT_MODE = stringPreferencesKey("inventory_sort_mode")
private val STALE_ONLY = booleanPreferencesKey("inventory_stale_only")
private val UPDATES_ONLY = booleanPreferencesKey("inventory_updates_only")
private val GITHUB_ONLY = booleanPreferencesKey("inventory_github_only")
private val SOURCE_FILTERS = stringPreferencesKey("inventory_source_filters")
private val APTOIDE_LOOKUP = booleanPreferencesKey("aptoide_lookup_enabled")
private val APKMIRROR_LOOKUP = booleanPreferencesKey("apkmirror_lookup_enabled")
private val APKPURE_LOOKUP = booleanPreferencesKey("apkpure_lookup_enabled")
private val PLAY_LOOKUP = booleanPreferencesKey("play_lookup_enabled")
private val FORGE_LOOKUP = booleanPreferencesKey("forge_lookup_enabled")
private val FORGE_LOOKUP_SEARCH_UNKNOWNS = booleanPreferencesKey("forge_lookup_search_unknowns")
private val SCAN_INTERVAL = stringPreferencesKey("scan_interval")
private val LAST_SCAN_AT = longPreferencesKey("last_scan_at_ms")

class InventoryPreferences(private val context: Context) {
    private val store get() = context.inventoryDataStore

    val queryAllPackagesAcknowledged: Flow<Boolean> = store.booleanPref(QUERY_ALL_PACKAGES_ACK, false)
    val includeSystemApps: Flow<Boolean> = store.booleanPref(INCLUDE_SYSTEM_APPS, false)
    val staleOnly: Flow<Boolean> = store.booleanPref(STALE_ONLY, false)
    val updatesOnly: Flow<Boolean> = store.booleanPref(UPDATES_ONLY, false)
    val aptoideLookupEnabled: Flow<Boolean> = store.booleanPref(APTOIDE_LOOKUP, false)
    val apkMirrorLookupEnabled: Flow<Boolean> = store.booleanPref(APKMIRROR_LOOKUP, false)
    val apkPureLookupEnabled: Flow<Boolean> = store.booleanPref(APKPURE_LOOKUP, false)
    val playLookupEnabled: Flow<Boolean> = store.booleanPref(PLAY_LOOKUP, true)
    val forgeLookupEnabled: Flow<Boolean> = store.booleanPref(FORGE_LOOKUP, true)
    val forgeLookupSearchUnknowns: Flow<Boolean> = store.booleanPref(FORGE_LOOKUP_SEARCH_UNKNOWNS, false)

    val usageStatsConsent: Flow<UsageStatsConsent> = store.data.map { prefs ->
        runCatching { UsageStatsConsent.valueOf(prefs[USAGE_STATS_CONSENT] ?: "") }
            .getOrDefault(UsageStatsConsent.NotOffered)
    }

    val sortMode: Flow<InventorySortMode> = store.data.map { prefs ->
        runCatching { InventorySortMode.valueOf(prefs[SORT_MODE] ?: "") }
            .getOrDefault(InventorySortMode.Oldest)
    }

    val sourceFilters: Flow<Set<RemoteReleasedSource>> = store.data.map { prefs ->
        InventorySourceFilter.decode(prefs[SOURCE_FILTERS], prefs[GITHUB_ONLY] == true)
    }

    val scanInterval: Flow<ScanInterval> = store.data.map { prefs ->
        runCatching { ScanInterval.valueOf(prefs[SCAN_INTERVAL] ?: "") }
            .getOrDefault(ScanInterval.OnDemand)
    }

    val lastScanAtMs: Flow<Long?> = store.data.map { prefs ->
        prefs[LAST_SCAN_AT]?.takeIf { it > 0L }
    }

    suspend fun setQueryAllPackagesAcknowledged(value: Boolean) {
        store.writeBoolean(QUERY_ALL_PACKAGES_ACK, value)
    }

    suspend fun setIncludeSystemApps(value: Boolean) {
        store.writeBoolean(INCLUDE_SYSTEM_APPS, value)
    }

    suspend fun setStaleOnly(value: Boolean) {
        store.writeBoolean(STALE_ONLY, value)
    }

    suspend fun setUpdatesOnly(value: Boolean) {
        store.writeBoolean(UPDATES_ONLY, value)
    }

    suspend fun setAptoideLookupEnabled(value: Boolean) {
        store.writeBoolean(APTOIDE_LOOKUP, value)
    }

    suspend fun setApkMirrorLookupEnabled(value: Boolean) {
        store.writeBoolean(APKMIRROR_LOOKUP, value)
    }

    suspend fun setApkPureLookupEnabled(value: Boolean) {
        store.writeBoolean(APKPURE_LOOKUP, value)
    }

    suspend fun setPlayLookupEnabled(value: Boolean) {
        store.writeBoolean(PLAY_LOOKUP, value)
    }

    suspend fun setForgeLookupEnabled(value: Boolean) {
        store.writeBoolean(FORGE_LOOKUP, value)
    }

    suspend fun setForgeLookupSearchUnknowns(value: Boolean) {
        store.writeBoolean(FORGE_LOOKUP_SEARCH_UNKNOWNS, value)
    }

    suspend fun setUsageStatsConsent(value: UsageStatsConsent) {
        store.edit { prefs -> prefs[USAGE_STATS_CONSENT] = value.name }
    }

    suspend fun setSortMode(value: InventorySortMode) {
        store.edit { prefs -> prefs[SORT_MODE] = value.name }
    }

    suspend fun setSourceFilters(value: Set<RemoteReleasedSource>) {
        store.edit { prefs ->
            prefs[SOURCE_FILTERS] = InventorySourceFilter.encode(value)
            prefs[GITHUB_ONLY] = RemoteReleasedSource.Forge in value
        }
    }

    suspend fun setScanInterval(value: ScanInterval) {
        store.edit { prefs -> prefs[SCAN_INTERVAL] = value.name }
    }

    suspend fun setLastScanAtMs(value: Long) {
        store.edit { prefs -> prefs[LAST_SCAN_AT] = value }
    }
}
