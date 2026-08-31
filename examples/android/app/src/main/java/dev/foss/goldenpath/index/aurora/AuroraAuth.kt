package dev.foss.goldenpath.index.aurora

import com.aurora.gplayapi.data.models.AuthData
import com.google.gson.Gson
import com.google.gson.JsonParser

object AuroraAuth {
    const val AUTH_URL = "https://auroraoss.com/api/auth"
    const val USER_AGENT = "com.aurora.store-4.8.4-76"

    fun emailOf(json: String): String? {
        val text = json.trim()
        if (text.isEmpty()) return null
        return runCatching {
            JsonParser.parseString(text).asJsonObject.get("email")?.asString?.trim()?.ifEmpty { null }
        }.getOrNull()
    }

    fun parse(json: String): AuthData? {
        if (emailOf(json) == null) return null
        return runCatching { Gson().fromJson(json, AuthData::class.java) }.getOrNull()
            ?.takeIf { it.email.isNotBlank() }
    }

    fun loadOrRefresh(store: EncryptedAuroraAuthStore?, propsJson: ByteArray): AuthData? {
        store?.getJson()?.let { parse(it) }?.let { return it }
        return refresh(store, propsJson)
    }

    fun refresh(store: EncryptedAuroraAuthStore?, propsJson: ByteArray): AuthData? {
        val response = runCatching {
            val postResp = if (propsJson.isNotEmpty()) {
                runCatching { AuroraPlayHttp.postAuth(AUTH_URL, propsJson) }.getOrNull()
            } else null
            if (postResp != null && postResp.isSuccessful) postResp else AuroraPlayHttp.getAuth(AUTH_URL)
        }.getOrNull() ?: return null
        if (!response.isSuccessful) return null
        val json = response.responseBytes.toString(Charsets.UTF_8)
        val auth = parse(json) ?: return null
        store?.setJson(json)
        return auth
    }
}
