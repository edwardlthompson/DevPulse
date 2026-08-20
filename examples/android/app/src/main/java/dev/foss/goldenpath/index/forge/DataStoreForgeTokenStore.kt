package dev.foss.goldenpath.index.forge

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.forgeTokenDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "forge_token_preferences",
)

private val TOKEN = stringPreferencesKey("github_token")

class DataStoreForgeTokenStore(private val context: Context) : ForgeTokenStore {
    override fun getToken(): String? = runBlocking {
        context.forgeTokenDataStore.data.first()[TOKEN]?.ifBlank { null }
    }

    override fun setToken(token: String?) {
        runBlocking {
            context.forgeTokenDataStore.edit { prefs ->
                val trimmed = token?.trim().orEmpty()
                if (trimmed.isEmpty()) prefs.remove(TOKEN) else prefs[TOKEN] = trimmed
            }
        }
    }
}
