package dev.foss.goldenpath.index.fdroid

data class FdroidApkHint(
    val apkName: String,
    val sha256: String? = null,
    val nativeCodes: Set<String> = emptySet(),
)

object FdroidApkFiles {
    private val apkName = Regex(""""apkName"\s*:\s*"([^"]+)"""")
    private val versionCode = Regex(""""versionCode"\s*:\s*(-?\d+)""")
    private val sha256 = Regex(""""hash"\s*:\s*"([0-9a-fA-F]{64})"""")
    private val nativecode = Regex(""""nativecode"\s*:\s*\[([^\]]*)\]""")

    fun namesFor(raw: ByteArray, wanted: Set<String>): Map<String, FdroidApkHint> {
        val start = FdroidIndexBytes.indexOf(raw, "\"packages\"")
        if (start < 0) return emptyMap()
        return namesIn(FdroidIndexBytes.utf8(raw, start, raw.size), wanted)
    }

    fun namesIn(packagesJson: String, wanted: Set<String> = emptySet()): Map<String, FdroidApkHint> {
        val keys = Regex(""""([a-zA-Z][a-zA-Z0-9_]*(?:\.[a-zA-Z0-9_]+)+)"\s*:\s*\[""")
            .findAll(packagesJson).toList()
        val result = HashMap<String, FdroidApkHint>()
        keys.forEachIndexed { index, match ->
            val pkg = match.groupValues[1]
            if (wanted.isNotEmpty() && pkg !in wanted) return@forEachIndexed
            val from = match.range.last + 1
            val to = keys.getOrNull(index + 1)?.range?.first ?: packagesJson.length
            highestApk(packagesJson.substring(from, to.coerceAtMost(packagesJson.length)))?.let {
                result[pkg] = it
            }
        }
        return result
    }

    private fun highestApk(body: String): FdroidApkHint? {
        var bestCode = Long.MIN_VALUE
        var best: FdroidApkHint? = null
        val names = apkName.findAll(body).toList()
        val codes = versionCode.findAll(body).toList()
        names.forEach { nameMatch ->
            val code = codes.lastOrNull { it.range.first < nameMatch.range.first }?.groupValues?.get(1)?.toLongOrNull()
                ?: codes.firstOrNull { it.range.first > nameMatch.range.first }?.groupValues?.get(1)?.toLongOrNull()
                ?: return@forEach
            if (code >= bestCode) {
                bestCode = code
                best = hintAround(body, nameMatch)
            }
        }
        return best
    }

    private fun hintAround(body: String, nameMatch: MatchResult): FdroidApkHint {
        val from = body.lastIndexOf('{', nameMatch.range.first).coerceAtLeast(0)
        val close = body.indexOf('}', nameMatch.range.last)
        val to = if (close < 0) body.length else close + 1
        val window = body.substring(from, to)
        val natives = nativecode.find(window)?.groupValues?.get(1)
            ?.split(',')
            ?.map { it.trim().trim('"') }
            ?.filter { it.isNotEmpty() }
            ?.toSet()
            .orEmpty()
        return FdroidApkHint(
            apkName = nameMatch.groupValues[1],
            sha256 = sha256.find(window)?.groupValues?.get(1)?.lowercase(),
            nativeCodes = natives,
        )
    }
}
