package dev.foss.goldenpath.inventory

import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class IgnoredUpdate(
    val packageName: String,
    val source: RemoteReleasedSource,
    val versionName: String,
)

object IgnoredUpdates {
    const val FILE = "ignored_updates.tsv"

    @Volatile
    var rows: Set<IgnoredUpdate> = emptySet()
        private set

    private val lock = Any()
    private val revisionState = MutableStateFlow(0)
    val revision: StateFlow<Int> = revisionState.asStateFlow()
    private var persist: File? = null

    fun file(filesDir: File): File = File(filesDir, FILE)

    fun hydrate(filesDir: File) {
        synchronized(lock) {
            persist = file(filesDir)
            rows = load(persist!!)
            revisionState.value += 1
        }
    }

    fun has(packageName: String, source: RemoteReleasedSource, versionName: String?): Boolean {
        val key = key(packageName, source, versionName) ?: return false
        return key in rows
    }

    fun add(
        packageName: String,
        source: RemoteReleasedSource,
        versionName: String?,
        filesDir: File? = null,
    ) {
        val key = key(packageName, source, versionName) ?: return
        synchronized(lock) {
            if (key in rows) return
            rows = rows + key
            if (filesDir != null) persist = file(filesDir)
            persist?.let { save(it, rows) }
            revisionState.value += 1
        }
    }

    fun drop(packageName: String, filesDir: File? = null) {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return
        synchronized(lock) {
            val next = rows.filterNot { it.packageName == pkg }.toSet()
            if (next.size == rows.size) return
            rows = next
            if (filesDir != null) persist = file(filesDir)
            persist?.let { save(it, rows) }
            revisionState.value += 1
        }
    }

    fun clearPersisted(filesDir: File) {
        synchronized(lock) {
            rows = emptySet()
            persist = file(filesDir)
            save(persist!!, rows)
            revisionState.value += 1
        }
    }

    fun clear() {
        synchronized(lock) {
            rows = emptySet()
            persist = null
            revisionState.value += 1
        }
    }

    internal fun key(
        packageName: String,
        source: RemoteReleasedSource,
        versionName: String?,
    ): IgnoredUpdate? {
        val pkg = packageName.trim()
        val ver = versionName?.trim().orEmpty()
        if (pkg.isEmpty() || ver.isEmpty() || source == RemoteReleasedSource.None) return null
        return IgnoredUpdate(pkg, source, ver)
    }

    internal fun load(file: File): Set<IgnoredUpdate> {
        if (!file.isFile) return emptySet()
        return runCatching { file.readLines().mapNotNull(::parse).toSet() }.getOrDefault(emptySet())
    }

    internal fun parse(line: String): IgnoredUpdate? {
        val parts = line.split('\t')
        if (parts.size < 3) return null
        val source = runCatching { RemoteReleasedSource.valueOf(parts[1].trim()) }.getOrNull() ?: return null
        return key(parts[0], source, parts[2])
    }

    internal fun encode(row: IgnoredUpdate): String =
        listOf(row.packageName, row.source.name, row.versionName).joinToString("\t")

    private fun save(file: File, rows: Set<IgnoredUpdate>) {
        file.parentFile?.mkdirs()
        runCatching { file.writeText(rows.joinToString("\n") { encode(it) }) }
    }
}
