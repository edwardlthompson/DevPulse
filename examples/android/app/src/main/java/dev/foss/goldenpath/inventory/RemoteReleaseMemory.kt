package dev.foss.goldenpath.inventory

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object RemoteReleaseMemory {
    @Volatile
    var byPackage: Map<String, RemoteReleasePick> = emptyMap()
        private set

    private val lock = Any()
    private val revisionState = MutableStateFlow(0)
    val revision: StateFlow<Int> = revisionState.asStateFlow()

    fun putAll(updates: Map<String, RemoteReleasePick>) {
        if (updates.isEmpty()) return
        synchronized(lock) {
            byPackage = buildMap {
                putAll(byPackage)
                updates.forEach { (pkg, pick) ->
                    put(pkg, RemoteReleaseRollup.merge(this[pkg], pick))
                }
            }
            persist?.save(byPackage)
            revisionState.value += 1
        }
    }

    fun hydrate(store: RemoteReleaseStore) {
        synchronized(lock) {
            persist = store
            if (byPackage.isNotEmpty()) return
            val loaded = store.load()
            if (loaded.isEmpty()) return
            byPackage = loaded
            revisionState.value += 1
        }
    }

    fun merge(app: InstalledApp): InstalledApp {
        val pick = byPackage[app.packageName]
        val dated = if (pick != null) RemoteRelease.apply(app, pick) else app
        return dated.copy(origin = AppOriginResolver.refine(dated.origin, pick?.source))
    }

    fun drop(packageName: String) {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return
        synchronized(lock) {
            if (pkg !in byPackage) return
            byPackage = byPackage - pkg
            persist?.save(byPackage)
            revisionState.value += 1
        }
    }

    fun clear() {
        synchronized(lock) {
            byPackage = emptyMap()
            revisionState.value += 1
        }
    }

    private var persist: RemoteReleaseStore? = null
}
