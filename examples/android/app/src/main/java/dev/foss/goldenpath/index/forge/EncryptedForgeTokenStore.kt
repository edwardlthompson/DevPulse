package dev.foss.goldenpath.index.forge

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class EncryptedForgeTokenStore(private val prefs: SharedPreferences) : ForgeTokenStore {
    override fun getToken(): String? = prefs.getString(KEY, null)?.trim()?.ifEmpty { null }

    override fun setToken(token: String?) {
        val trimmed = token?.trim().orEmpty()
        prefs.edit().apply {
            if (trimmed.isEmpty()) remove(KEY) else putString(KEY, trimmed)
            apply()
        }
    }

    companion object {
        private const val FILE = "forge_token_encrypted"
        private const val KEY = "github_token"

        fun create(context: Context): EncryptedForgeTokenStore? = runCatching {
            val alias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedForgeTokenStore(
                EncryptedSharedPreferences.create(
                    FILE,
                    alias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
                ),
            )
        }.getOrNull()

        fun wrap(context: Context): ForgeTokenStore {
            val plaintext = DataStoreForgeTokenStore(context)
            val secure = create(context) ?: return plaintext
            return MigratingForgeTokenStore(secure, plaintext)
        }
    }
}
