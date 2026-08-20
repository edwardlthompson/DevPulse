package dev.foss.goldenpath.inventory

data class RefreshProgress(
    val done: Int,
    val total: Int,
    val location: String = "",
) {
    val fraction: Float get() = if (total <= 0) 0f else (done.toFloat() / total).coerceIn(0f, 1f)
}

class RefreshProgressClock(
    private val onProgress: (RefreshProgress) -> Unit,
) {
    private val lock = Any()
    var done: Int = 0
        private set
    var total: Int = 0
        private set

    fun addWork(count: Int) {
        if (count <= 0) return
        synchronized(lock) {
            total += count
            onProgress(RefreshProgress(done, total))
        }
    }

    fun begin(location: String) {
        synchronized(lock) {
            RefreshTrace.line(location)
            onProgress(RefreshProgress(done, total, location))
        }
    }

    fun tick(location: String) {
        synchronized(lock) {
            done += 1
            onProgress(RefreshProgress(done, total, location))
        }
    }
}

object RefreshLocations {
    fun probesPerApp(
        play: Boolean,
        aptoide: Boolean,
        forge: Boolean,
        apkMirror: Boolean = false,
        apkPure: Boolean = false,
    ): Int = listOf(play, aptoide, forge, apkMirror, apkPure).count { it }

    fun total(
        repos: Int,
        apps: Int,
        play: Boolean,
        aptoide: Boolean,
        forge: Boolean,
        apkMirror: Boolean = false,
        apkPure: Boolean = false,
    ): Int = repos.coerceAtLeast(0) +
        apps.coerceAtLeast(0) * probesPerApp(play, aptoide, forge, apkMirror, apkPure)

    fun storeProbes(apps: Int, play: Boolean, aptoide: Boolean): Int =
        apps.coerceAtLeast(0) * listOf(play, aptoide).count { it }

    fun forgeProbes(apps: Int, forge: Boolean): Int =
        if (forge) apps.coerceAtLeast(0) else 0

    fun label(source: String, name: String, packageName: String = ""): String {
        val who = if (packageName.isEmpty()) name else "$name ($packageName)"
        return "$source · $who"
    }
}
