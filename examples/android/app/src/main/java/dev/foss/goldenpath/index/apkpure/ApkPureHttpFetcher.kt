package dev.foss.goldenpath.index.apkpure

import android.os.Build
import dev.foss.goldenpath.index.forge.RetryAfter
import dev.foss.goldenpath.inventory.HostRetry
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

object ApkPureHttpFetcher : ApkPureBatchFetcher {
    override fun fetch(packageNames: List<String>): Result<String> = runCatching {
        val androidId = Random.nextLong().toString(16)
        val apps = packageNames.joinToString(",") { pkg ->
            """{"package_name":"${pkg.replace("\"", "")}","version_code":0,"is_system":false}"""
        }
        val header = """{"device_info":{"abis":["arm64-v8a","armeabi-v7a"],"android_id":"$androidId","os_ver":"${Build.VERSION.SDK_INT}","os_ver_name":"${Build.VERSION.RELEASE}","platform":1,"screen_height":1920,"screen_width":1080}}"""
        val body = """{"app_info_for_update":[$apps],"android_id":"$androidId","application_id":"${ApkPureLink.STORE_PACKAGE}","cached_size":-1}"""
        val conn = URL(ApkPureFetchPolicy.UPDATE_URL).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("User-Agent", ApkPureFetchPolicy.USER_AGENT)
            conn.setRequestProperty("content-type", "application/json")
            conn.setRequestProperty("ual-access-businessid", "projecta")
            conn.setRequestProperty("ual-access-projecta", header)
            conn.connectTimeout = ApkPureFetchPolicy.CONNECT_TIMEOUT_MS
            conn.readTimeout = ApkPureFetchPolicy.READ_TIMEOUT_MS
            conn.outputStream.bufferedWriter().use { it.write(body) }
            val code = conn.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                HostRetry.note("apkpure", code, RetryAfter.seconds(conn.getHeaderField("Retry-After")))
                error("apkpure $code")
            }
            conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }
}
