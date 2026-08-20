package dev.foss.goldenpath.inventory

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.inventoryDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "inventory_preferences",
)

private val QUERY_ALL_PACKAGES_ACK = booleanPreferencesKey("query_all_packages_ack")
private val INCLUDE_SYSTEM_APPS = booleanPreferencesKey("include_system_apps")
private val USAGE_STATS_CONSENT = stringPreferencesKey("usage_stats_consent")
private val SORT_MODE = stringPreferencesKey("inventory_sort_mode")
private val STALE_ONLY = booleanPreferencesKey("inventory_stale_only")
private val UPDATES_ONLY = booleanPreferencesKey("inventory_updates_only")
private val GITHUB_ONLY = booleanPreferencesKey("inventory_github_only")
private val SOURCE_FILTERS = stringPreferencesKey("inventory_source_filters")
private val APTOIDE_LOOKUP = booleanPreferencesKey("aptoide_lookup_enabled")
private val PLAY_LOOKUP = booleanPreferencesKey("play_lookup_enabled")
private val FORGE_LOOKUP = booleanPreferencesKey("forge_lookup_enabled")
private val FORGE_LOOKUP_SEARCH_UNKNOWNS = booleanPreferencesKey("forge_lookup_search_unknowns")
private val SCAN_INTERVAL = stringPreferencesKey("scan_interval")
private val LAST_SCAN_AT = longPreferencesKey("last_scan_at_ms")

class InventoryPreferences(private val context: Context) {
    val queryAllPackagesAcknowledged: Flow<Boolean> = context.inventoryDataStore.data.map { prefs ->
        prefs[QUERY_ALL_PACKAGES_ACK] ?: false
    }

    val includeSystemApps: Flow<Boolean> = context.inventoryDataStore.data.map { prefs ->
        prefs[INCLUDE_SYSTEM_APPS] ?: false
    }

    val usageStatsConsent: Flow<UsageStatsConsent> = context.inventoryDataStore.data.map { prefs ->
        runCatching { UsageStatsConsent.valueOf(prefs[USAGE_STATS_CONSENT] ?: "") }
            .getOrDefault(UsageStatsConsent.NotOffered)
    }

    val sortMode: Flow<InventorySortMode> = context.inventoryDataStore.data.map { prefs ->
        runCatching { InventorySortMode.valueOf(prefs[SORT_MODE] ?: "") }
            .getOrDefault(InventorySortMode.Oldest)
    }

    val staleOnly: Flow<Boolean> = context.inventoryDataStore.data.map { prefs ->
        prefs[STALE_ONLY] ?: false
    }

    val updatesOnly: Flow<Boolean> = context.inventoryDataStore.data.map { prefs ->
        prefs[UPDATES_ONLY] ?: false
    }

    val sourceFilters: Flow<Set<RemoteReleasedSource>> = context.inventoryDataStore.data.map { prefs ->
        InventorySourceFilter.decode(prefs[SOURCE_FILTERS], prefs[GITHUB_ONLY] == true)
    }

    val aptoideLookupEnabled: Flow<Boolean> = context.inventoryDataStore.data.map { prefs ->
        prefs[APTOIDE_LOOKUP] ?: false
    }

    val playLookupEnabled: Flow<Boolean> = context.inventoryDataStore.data.map { prefs ->
        prefs[PLAY_LOOKUP] ?: true
    }

    val forgeLookupEnabled: Flow<Boolean> = context.inventoryDataStore.data.map { prefs ->
        prefs[FORGE_LOOKUP] ?: true
    }

    val forgeLookupSearchUnknowns: Flow<Boolean> = context.inventoryDataStore.data.map { prefs ->
        prefs[FORGE_LOOKUP_SEARCH_UNKNOWNS] ?: false
    }

    val scanInterval: Flow<ScanInterval> = context.inventoryDataStore.data.map { prefs ->
        runCatching { ScanInterval.valueOf(prefs[SCAN_INTERVAL] ?: "") }
            .getOrDefault(ScanInterval.OnDemand)
    }

    val lastScanAtMs: Flow<Long?> = context.inventoryDataStore.data.map { prefs ->
        prefs[LAST_SCAN_AT]?.takeIf { it > 0L }
    }

    suspend fun setQueryAllPackagesAcknowledged(value: Boolean) {
        context.inventoryDataStore.edit { prefs ->
            prefs[QUERY_ALL_PACKAGES_ACK] = value
        }
    }

    suspend fun setIncludeSystemApps(value: Boolean) {
        context.inventoryDataStore.edit { prefs ->
            prefs[INCLUDE_SYSTEM_APPS] = value
        }
    }

    suspend fun setUsageStatsConsent(value: UsageStatsConsent) {
        context.inventoryDataStore.edit { prefs ->
            prefs[USAGE_STATS_CONSENT] = value.name
        }
    }

    suspend fun setSortMode(value: InventorySortMode) {
        context.inventoryDataStore.edit { prefs -> prefs[SORT_MODE] = value.name }
    }

    suspend fun setStaleOnly(value: Boolean) {
        context.inventoryDataStore.edit { prefs -> prefs[STALE_ONLY] = value }
    }

    suspend fun setUpdatesOnly(value: Boolean) {
        context.inventoryDataStore.edit { prefs -> prefs[UPDATES_ONLY] = value }
    }

    suspend fun setSourceFilters(value: Set<RemoteReleasedSource>) {
        context.inventoryDataStore.edit { prefs ->
            prefs[SOURCE_FILTERS] = InventorySourceFilter.encode(value)
            prefs[GITHUB_ONLY] = RemoteReleasedSource.Forge in value
        }
    }

    suspend fun setAptoideLookupEnabled(value: Boolean) {
        context.inventoryDataStore.edit { prefs -> prefs[APTOIDE_LOOKUP] = value }
    }

    suspend fun setPlayLookupEnabled(value: Boolean) {
        context.inventoryDataStore.edit { prefs -> prefs[PLAY_LOOKUP] = value }
    }

    suspend fun setForgeLookupEnabled(value: Boolean) {
        context.inventoryDataStore.edit { prefs -> prefs[FORGE_LOOKUP] = value }
    }

    suspend fun setForgeLookupSearchUnknowns(value: Boolean) {
        context.inventoryDataStore.edit { prefs -> prefs[FORGE_LOOKUP_SEARCH_UNKNOWNS] = value }
    }

    suspend fun setScanInterval(value: ScanInterval) {
        context.inventoryDataStore.edit { prefs -> prefs[SCAN_INTERVAL] = value.name }
    }

    suspend fun setLastScanAtMs(value: Long) {
        context.inventoryDataStore.edit { prefs -> prefs[LAST_SCAN_AT] = value }
    }
}
