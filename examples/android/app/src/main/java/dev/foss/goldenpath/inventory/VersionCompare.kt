package dev.foss.goldenpath.inventory

object VersionCompare {
    fun isNewer(remoteName: String?, installedName: String?): Boolean {
        val remote = remoteName?.trim().orEmpty()
        val installed = installedName?.trim().orEmpty()
        if (remote.isEmpty() || installed.isEmpty() || remote == installed) return false
        return compare(remote, installed) > 0
    }

    fun compare(left: String, right: String): Int {
        val a = parts(left)
        val b = parts(right)
        val n = maxOf(a.size, b.size)
        for (i in 0 until n) {
            val d = a.getOrElse(i) { 0L }.compareTo(b.getOrElse(i) { 0L })
            if (d != 0) return d
        }
        return 0
    }

    private fun parts(raw: String): List<Long> {
        val cleaned = raw.trim().trimStart { it == 'v' || it == 'V' }
        return cleaned.split('.', '-', '_').mapNotNull { token ->
            token.takeWhile { it.isDigit() }.toLongOrNull()
        }
    }
}
