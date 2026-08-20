package dev.foss.goldenpath.index.apkmirror

import java.net.HttpURLConnection
import java.net.URL

object ApkMirrorHttpFetcher : ApkMirrorBatchFetcher {
    override fun fetch(packageNames: List<String>): Result<String> = runCatching {
        val names = packageNames.joinToString(",") { "\"${it.replace("\"", "")}\"" }
        val body = """{"pnames":[$names],"exclude":["alpha","beta"]}"""
        val conn = URL(ApkMirrorFetchPolicy.EXISTS_URL).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("User-Agent", ApkMirrorFetchPolicy.USER_AGENT)
            conn.setRequestProperty("Authorization", ApkMirrorFetchPolicy.AUTH)
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = ApkMirrorFetchPolicy.CONNECT_TIMEOUT_MS
            conn.readTimeout = ApkMirrorFetchPolicy.READ_TIMEOUT_MS
            conn.outputStream.bufferedWriter().use { it.write(body) }
            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                error("apkmirror ${conn.responseCode}")
            }
            conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }
}
