package dev.foss.goldenpath.about

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object ApkDownloadHelper {
    suspend fun download(url: String, destination: File): Boolean = withContext(Dispatchers.IO) {
        try {
            destination.parentFile?.mkdirs()
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 15_000
            conn.readTimeout = 60_000
            if (conn.responseCode != HttpURLConnection.HTTP_OK) return@withContext false
            conn.inputStream.use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            destination.length() > 0L
        } catch (_: Exception) {
            false
        }
    }
}
