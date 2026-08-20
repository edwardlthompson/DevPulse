package dev.foss.goldenpath.index.fdroid

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.fdroidRepoDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "fdroid_repo_preferences",
)

private val CUSTOM_URL = stringPreferencesKey("custom_index_url")

class FdroidRepoPreferences(private val context: Context) {
    fun repoEnabled(repoId: String): Flow<Boolean> {
        val key = booleanPreferencesKey("repo_enabled_$repoId")
        val defaultOn = repoId == "official"
        return context.fdroidRepoDataStore.data.map { prefs -> prefs[key] ?: defaultOn }
    }

    val customIndexUrl: Flow<String> = context.fdroidRepoDataStore.data.map { prefs ->
        prefs[CUSTOM_URL] ?: ""
    }

    suspend fun setRepoEnabled(repoId: String, enabled: Boolean) {
        val key = booleanPreferencesKey("repo_enabled_$repoId")
        context.fdroidRepoDataStore.edit { prefs -> prefs[key] = enabled }
    }

    suspend fun setCustomIndexUrl(url: String) {
        context.fdroidRepoDataStore.edit { prefs -> prefs[CUSTOM_URL] = url.trim() }
    }
}
