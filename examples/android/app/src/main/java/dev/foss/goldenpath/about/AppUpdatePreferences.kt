package dev.foss.goldenpath.about

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

private val Context.appUpdateDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_update_preferences",
)

private val CHECK_INTERVAL = stringPreferencesKey("check_interval")
private val LAST_CHECKED = longPreferencesKey("last_checked")
private val INSTALLED_FORMAT = stringPreferencesKey("installed_artifact_format")
private val PENDING_RESTART = booleanPreferencesKey("pending_restart")

class AppUpdatePreferences(private val context: Context) {
    val checkInterval: Flow<String> = context.appUpdateDataStore.data.map { prefs ->
        prefs[CHECK_INTERVAL] ?: "off"
    }

    val lastChecked: Flow<Long?> = context.appUpdateDataStore.data.map { prefs ->
        prefs[LAST_CHECKED]
    }

    val installedFormat: Flow<String?> = context.appUpdateDataStore.data.map { prefs ->
        prefs[INSTALLED_FORMAT]
    }

    val pendingRestart: Flow<Boolean> = context.appUpdateDataStore.data.map { prefs ->
        prefs[PENDING_RESTART] ?: false
    }

    suspend fun ensureInstalledFormat(): String {
        var format = "apk"
        context.appUpdateDataStore.edit { prefs ->
            if (prefs[INSTALLED_FORMAT] == null) {
                format = ArtifactFormatDetector.detectAndroidFormat()
                prefs[INSTALLED_FORMAT] = format
            } else {
                format = prefs[INSTALLED_FORMAT] ?: format
            }
        }
        return format
    }

    suspend fun setCheckInterval(interval: String) {
        context.appUpdateDataStore.edit { prefs ->
            prefs[CHECK_INTERVAL] = interval
        }
    }

    suspend fun setLastChecked(epochMs: Long) {
        context.appUpdateDataStore.edit { prefs ->
            prefs[LAST_CHECKED] = epochMs
        }
    }

    suspend fun setPendingRestart(value: Boolean) {
        context.appUpdateDataStore.edit { prefs ->
            prefs[PENDING_RESTART] = value
        }
    }

    suspend fun clearPendingRestart() {
        setPendingRestart(false)
    }
}
