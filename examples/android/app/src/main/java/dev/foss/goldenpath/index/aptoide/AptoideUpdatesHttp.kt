package dev.foss.goldenpath.index.aptoide

import java.net.HttpURLConnection
import java.net.URL

object AptoideUpdatesHttp : AptoideUpdatesFetcher {
    override fun fetch(apks: List<AptoideApkRef>): Result<String> = runCatching {
        val items = apks.mapNotNull { ref ->
            val sig = ref.signature?.trim().orEmpty()
            val pkg = ref.packageName.trim()
            if (sig.isEmpty() || pkg.isEmpty()) return@mapNotNull null
            """{"package":"${esc(pkg)}","vercode":0,"signature":"${esc(sig)}"}"""
        }
        if (items.isEmpty()) return@runCatching """{"info":{"status":"OK"},"list":[]}"""
        val body = """{"apks_data":[${items.joinToString(",")}]}"""
        val conn = URL(AptoideFetchPolicy.UPDATES_URL).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("User-Agent", AptoideFetchPolicy.USER_AGENT)
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = AptoideFetchPolicy.CONNECT_TIMEOUT_MS
            conn.readTimeout = AptoideFetchPolicy.READ_TIMEOUT_MS
            conn.outputStream.bufferedWriter().use { it.write(body) }
            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                error("aptoide-updates ${conn.responseCode}")
            }
            conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    private fun esc(value: String): String = value.replace("\\", "").replace("\"", "")
}
