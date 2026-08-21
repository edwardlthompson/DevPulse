package dev.foss.goldenpath.index.aurora

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class EncryptedAuroraAuthStore(private val prefs: SharedPreferences) {
    fun getJson(): String? = prefs.getString(KEY, null)?.trim()?.ifEmpty { null }

    fun setJson(json: String?) {
        val trimmed = json?.trim().orEmpty()
        prefs.edit().apply {
            if (trimmed.isEmpty()) remove(KEY) else putString(KEY, trimmed)
            apply()
        }
    }

    companion object {
        private const val FILE = "aurora_auth_encrypted"
        private const val KEY = "auth_json"

        fun create(context: Context): EncryptedAuroraAuthStore? = runCatching {
            val alias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedAuroraAuthStore(
                EncryptedSharedPreferences.create(
                    FILE,
                    alias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
                ),
            )
        }.getOrNull()
    }
}
