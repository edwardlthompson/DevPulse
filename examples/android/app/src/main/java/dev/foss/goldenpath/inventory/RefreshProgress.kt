package dev.foss.goldenpath.inventory

data class RefreshProgress(
    val done: Int,
    val total: Int,
    val location: String = "",
    val outlets: List<RefreshOutletSnap> = emptyList(),
) {
    val fraction: Float get() = if (total <= 0) 0f else (done.toFloat() / total).coerceIn(0f, 1f)
}

class RefreshProgressClock(
    private val onProgress: (RefreshProgress) -> Unit,
) {
    private val lock = Any()
    private var location: String = ""
    var done: Int = 0
        private set
    var total: Int = 0
        private set

    init {
        active = this
    }

    fun addWork(count: Int) {
        if (count <= 0) return
        synchronized(lock) {
            total += count
            push()
        }
    }

    fun begin(location: String) {
        synchronized(lock) {
            this.location = location
            RefreshTrace.line(location)
            push()
        }
    }

    fun tick(location: String) {
        synchronized(lock) {
            this.location = location
            done += 1
            push()
        }
    }

    fun planOutlet(id: String, title: String, count: Int) {
        RefreshOutletBoard.plan(id, title, count)
        pulse()
    }

    fun outletAt(id: String, current: String) {
        RefreshOutletBoard.at(id, current)
        pulse()
    }

    fun outletTick(id: String) {
        RefreshOutletBoard.tick(id)
        pulse()
    }

    fun pulse() {
        synchronized(lock) { push() }
    }

    private fun push() {
        onProgress(RefreshProgress(done, total, location, RefreshOutletBoard.snaps()))
    }

    companion object {
        @Volatile
        var active: RefreshProgressClock? = null
            private set

        fun pulseActive() {
            active?.pulse()
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
