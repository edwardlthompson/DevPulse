package dev.foss.goldenpath.index.fdroid

data class FdroidVersionRecord(
    val versionName: String,
    val versionCode: Long? = null,
    val apkName: String? = null,
    val sha256: String? = null,
    val sizeBytes: Long? = null,
    val addedMs: Long? = null,
    val minSdk: Int? = null,
)

object FdroidPackageVersions {
    private val pkgKey = Regex(""""([a-zA-Z][a-zA-Z0-9_]*(?:\.[a-zA-Z0-9_]+)+)"\s*:\s*\[""")
    private val token = Regex(""""versionName"\s*:\s*"([^"]+)"|"versionCode"\s*:\s*(-?\d+)""")

    fun allFor(raw: ByteArray, packageName: String): List<FdroidVersionRecord> {
        val start = FdroidIndexBytes.indexOf(raw, "\"packages\"")
        if (start < 0 || packageName.isEmpty()) return emptyList()
        val needle = "\"$packageName\""
        val pkgAt = FdroidIndexBytes.indexOf(raw, needle, start)
        if (pkgAt < 0) return emptyList()
        val colon = FdroidIndexBytes.indexOfByte(raw, ':'.code.toByte(), pkgAt + needle.length)
        if (colon < 0) return emptyList()
        val bracket = FdroidIndexBytes.indexOfByte(raw, '['.code.toByte(), colon)
        if (bracket < 0) return emptyList()
        val from = bracket + 1
        val to = endOfArray(raw, from)
        val body = FdroidIndexBytes.utf8(raw, from, to)
        return allIn(body)
    }

    private val vNameRegex = Regex(""""versionName"\s*:\s*"([^"]+)"""")
    private val vCodeRegex = Regex(""""versionCode"\s*:\s*(-?\d+)""")
    private val aNameRegex = Regex(""""apkName"\s*:\s*"([^"]+)"""")
    private val hashRegex = Regex(""""hash"\s*:\s*"([0-9a-fA-F]{64})"""")
    private val sizeRegex = Regex(""""size"\s*:\s*(\d+)""")
    private val addedRegex = Regex(""""added"\s*:\s*(\d+)""")
    private val minSdkRegex = Regex(""""minSdkVersion"\s*:\s*(\d+)""")

    fun allIn(body: String): List<FdroidVersionRecord> {
        val results = mutableListOf<FdroidVersionRecord>()
        val objects = splitObjects(body)
        for (chunk in objects) {
            val vName = vNameRegex.find(chunk)?.groupValues?.get(1)
            val vCode = vCodeRegex.find(chunk)?.groupValues?.get(1)?.toLongOrNull()
            val aName = aNameRegex.find(chunk)?.groupValues?.get(1)
            val hash = hashRegex.find(chunk)?.groupValues?.get(1)?.lowercase()
            val size = sizeRegex.find(chunk)?.groupValues?.get(1)?.toLongOrNull()
            val added = addedRegex.find(chunk)?.groupValues?.get(1)?.toLongOrNull()
            val minSdk = minSdkRegex.find(chunk)?.groupValues?.get(1)?.toIntOrNull()
            if (!vName.isNullOrBlank() || !aName.isNullOrBlank()) {
                results += FdroidVersionRecord(
                    versionName = vName ?: aName.orEmpty(),
                    versionCode = vCode,
                    apkName = aName,
                    sha256 = hash,
                    sizeBytes = size,
                    addedMs = added,
                    minSdk = minSdk,
                )
            }
        }
        return results
    }

    private fun splitObjects(body: String): List<String> {
        val list = mutableListOf<String>()
        var depth = 0
        var inStr = false
        var escape = false
        var start = -1
        for (i in body.indices) {
            val c = body[i]
            when {
                escape -> escape = false
                inStr && c == '\\' -> escape = true
                c == '"' -> inStr = !inStr
                !inStr && c == '{' -> {
                    if (depth == 0) start = i
                    depth++
                }
                !inStr && c == '}' -> {
                    depth--
                    if (depth == 0 && start >= 0) {
                        list += body.substring(start, i + 1)
                        start = -1
                    }
                }
            }
        }
        return list
    }

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
