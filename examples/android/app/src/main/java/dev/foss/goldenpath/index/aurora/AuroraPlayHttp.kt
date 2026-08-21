package dev.foss.goldenpath.index.aurora

import com.aurora.gplayapi.data.models.PlayResponse
import com.aurora.gplayapi.network.IHttpClient
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** HttpURLConnection adapter for gplayapi. Never logs bodies or tokens. */
object AuroraPlayHttp : IHttpClient {
    private const val MAX_BYTES = 2_000_000
    private val codeState = MutableStateFlow(0)
    override val responseCode: StateFlow<Int> = codeState.asStateFlow()

    override fun post(url: String, headers: Map<String, String>, body: ByteArray): PlayResponse =
        request("POST", url, headers + ("Content-Type" to "application/x-protobuf"), body)

    override fun post(url: String, headers: Map<String, String>, params: Map<String, String>): PlayResponse =
        request("POST", buildUrl(url, params), headers, ByteArray(0))

    override fun get(url: String, headers: Map<String, String>): PlayResponse =
        request("GET", url, headers, null)

    override fun get(url: String, headers: Map<String, String>, params: Map<String, String>): PlayResponse =
        request("GET", buildUrl(url, params), headers, null)

    override fun get(url: String, headers: Map<String, String>, paramString: String): PlayResponse =
        request("GET", url + paramString, headers, null)

    override fun getAuth(url: String): PlayResponse =
        request("GET", url, mapOf("User-Agent" to AuroraAuth.USER_AGENT), null)

    override fun postAuth(url: String, body: ByteArray): PlayResponse =
        request(
            "POST",
            url,
            mapOf("User-Agent" to AuroraAuth.USER_AGENT, "Content-Type" to "application/json"),
            body,
        )

    private fun request(
        method: String,
        url: String,
        headers: Map<String, String>,
        body: ByteArray?,
    ): PlayResponse {
        val conn = URL(url).openConnection() as HttpURLConnection
        return try {
            conn.instanceFollowRedirects = true
            conn.useCaches = false
            conn.requestMethod = method
            conn.connectTimeout = 25_000
            conn.readTimeout = 25_000
            headers.forEach { (key, value) -> conn.setRequestProperty(key, value) }
            if (body != null) {
                conn.doOutput = true
                conn.outputStream.use { it.write(body) }
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val bytes = readCapped(stream)
            codeState.value = code
            PlayResponse(
                id = "",
                responseBytes = bytes,
                errorBytes = ByteArray(0),
                errorString = if (code in 200..299) "" else conn.responseMessage.orEmpty(),
                isSuccessful = code in 200..299,
                code = code,
            )
        } finally {
            conn.disconnect()
        }
    }

    private fun buildUrl(url: String, params: Map<String, String>): String {
        if (params.isEmpty()) return url
        val query = params.entries.joinToString("&") { (key, value) ->
            val enc = URLEncoder.encode(value, Charsets.UTF_8.name())
            "$key=$enc"
        }
        return if (url.contains('?')) "$url&$query" else "$url?$query"
    }

    private fun readCapped(stream: java.io.InputStream?): ByteArray {
        if (stream == null) return ByteArray(0)
        return stream.use { input ->
            val out = java.io.ByteArrayOutputStream()
            val buf = ByteArray(16_384)
            var total = 0
            while (total < MAX_BYTES) {
                val n = input.read(buf, 0, minOf(buf.size, MAX_BYTES - total))
                if (n < 0) break
                out.write(buf, 0, n)
                total += n
            }
            out.toByteArray()
        }
    }
}
