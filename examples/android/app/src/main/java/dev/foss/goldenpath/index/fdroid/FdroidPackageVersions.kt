package dev.foss.goldenpath.index.fdroid

object FdroidPackageVersions {
    private val pkgKey = Regex(""""([a-zA-Z][a-zA-Z0-9_]*(?:\.[a-zA-Z0-9_]+)+)"\s*:\s*\[""")
    private val token = Regex(""""versionName"\s*:\s*"([^"]+)"|"versionCode"\s*:\s*(-?\d+)""")

    fun highestByPackage(raw: String): Map<String, String> {
        val start = raw.indexOf("\"packages\"")
        if (start < 0) return emptyMap()
        val keys = pkgKey.findAll(raw, start).toList()
        if (keys.isEmpty()) return emptyMap()
        val result = HashMap<String, String>(keys.size)
        keys.forEachIndexed { index, match ->
            val from = match.range.last + 1
            val to = keys.getOrNull(index + 1)?.range?.first ?: raw.length
            highestIn(raw.substring(from, to.coerceAtMost(raw.length)))?.let {
                result[match.groupValues[1]] = it
            }
        }
        return result
    }

    fun highestFor(raw: ByteArray, wanted: Set<String>): Map<String, String> {
        val start = FdroidIndexBytes.indexOf(raw, "\"packages\"")
        if (start < 0 || wanted.isEmpty()) return emptyMap()
        val result = HashMap<String, String>(wanted.size)
        var i = start
        val quote = '"'.code.toByte()
        val colon = ':'.code.toByte()
        val bracket = '['.code.toByte()
        while (i < raw.size && result.size < wanted.size) {
            if (raw[i] != quote) {
                i++
                continue
            }
            val close = FdroidIndexBytes.indexOfByte(raw, quote, i + 1)
            if (close < 0) break
            val name = FdroidIndexBytes.utf8(raw, i + 1, close)
            i = close + 1
            while (i < raw.size && raw[i].toInt().toChar().isWhitespace()) i++
            if (i >= raw.size || raw[i] != colon) continue
            i++
            while (i < raw.size && raw[i].toInt().toChar().isWhitespace()) i++
            if (i >= raw.size || raw[i] != bracket) continue
            val from = i + 1
            val to = endOfArray(raw, from)
            i = to
            if (name in wanted) {
                highestIn(FdroidIndexBytes.utf8(raw, from, to))?.let { result[name] = it }
            }
        }
        return result
    }

    private fun highestIn(body: String): String? {
        var pendingName: String? = null
        var pendingCode: Long? = null
        var bestCode = Long.MIN_VALUE
        var bestName: String? = null
        token.findAll(body).forEach { match ->
            val name = match.groupValues[1].ifEmpty { null }
            val code = match.groupValues[2].toLongOrNull()
            if (name != null) pendingName = name else pendingCode = code
            val pairName = pendingName
            val pairCode = pendingCode
            if (pairName != null && pairCode != null) {
                if (pairCode >= bestCode) {
                    bestCode = pairCode
                    bestName = pairName
                }
                pendingName = null
                pendingCode = null
            }
        }
        return bestName
    }

    private fun endOfArray(raw: ByteArray, from: Int): Int {
        var depth = 1
        var inStr = false
        var escape = false
        var i = from
        while (i < raw.size && depth > 0) {
            val c = raw[i].toInt().toChar()
            when {
                escape -> escape = false
                inStr && c == '\\' -> escape = true
                c == '"' -> inStr = !inStr
                !inStr && c == '[' -> depth++
                !inStr && c == ']' -> depth--
            }
            i++
        }
        return i
    }
}
