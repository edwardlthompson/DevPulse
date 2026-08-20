package dev.foss.goldenpath.index.fdroid

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

object FdroidIndexUnpack {
    const val MAX_JSON_BYTES = 96 * 1024 * 1024

    fun readBytes(input: InputStream, fromJar: Boolean, maxBytes: Int = MAX_JSON_BYTES): ByteArray {
        if (!fromJar) return readLimited(input, maxBytes)
        ZipInputStream(input).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: error("index-v1.json missing")
                if (entry.name.endsWith("index-v1.json")) return readLimited(zip, maxBytes)
            }
        }
    }

    fun readJson(input: InputStream, fromJar: Boolean, maxBytes: Int = MAX_JSON_BYTES): String =
        String(readBytes(input, fromJar, maxBytes), Charsets.UTF_8)

    fun isJarUrl(url: String): Boolean =
        url.endsWith(".jar", ignoreCase = true) || url.endsWith(".zip", ignoreCase = true)

    internal fun readLimited(input: InputStream, maxBytes: Int): ByteArray {
        val out = ByteArrayOutputStream(8_192)
        val buf = ByteArray(8_192)
        var total = 0
        while (true) {
            val n = input.read(buf)
            if (n < 0) break
            total += n
            if (total > maxBytes) error("fdroid index exceeds $maxBytes bytes")
            out.write(buf, 0, n)
        }
        return out.toByteArray()
    }
}
