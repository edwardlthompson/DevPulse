package dev.foss.goldenpath.inventory

object VersionCompare {
    private val artifactVersion = Regex(
        """(?<![0-9])v?(\d+\.\d+(?:\.\d+){0,3}[a-z]?)(?![0-9a-z])""",
        RegexOption.IGNORE_CASE,
    )

    fun isNewer(
        remoteName: String?,
        installedName: String?,
        installedCode: Long = 0,
        remoteCode: Long? = null,
    ): Boolean {
        if (remoteCode != null && remoteCode > 0 && installedCode > 0 && remoteCode > installedCode) {
            return true
        }
        val remote = remoteName?.trim().orEmpty()
        val installed = installedName?.trim().orEmpty()
        if (remote.isEmpty()) return false
        if (installed.isNotEmpty() && remote == installed) return false
        if (installedCode > 0 && remote.length >= 6 && remote.all { it.isDigit() }) {
            val code = remote.toLongOrNull() ?: return false
            return code > installedCode
        }
        if (installed.isEmpty()) return false
        return compare(remote, installed) > 0
    }

    fun compare(left: String, right: String): Int {
        val a = parts(canonical(left))
        val b = parts(canonical(right))
        val n = maxOf(a.size, b.size)
        for (i in 0 until n) {
            val d = a.getOrElse(i) { 0L }.compareTo(b.getOrElse(i) { 0L })
            if (d != 0) return d
        }
        return 0
    }

    internal fun canonical(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return trimmed
        val noApk = trimmed.replace(Regex("""\.apk$""", RegexOption.IGNORE_CASE), "")
        val hits = artifactVersion.findAll(noApk).map { it.groupValues[1] }.toList()
        return hits.maxByOrNull { it.count { ch -> ch == '.' } * 32 + it.length } ?: trimmed
    }

    private fun parts(raw: String): List<Long> {
        val cleaned = raw.trim().trimStart { it == 'v' || it == 'V' }
        return cleaned.split('.', '-', '_').mapNotNull { token ->
            token.takeWhile { it.isDigit() }.toLongOrNull()
        }
    }
}
