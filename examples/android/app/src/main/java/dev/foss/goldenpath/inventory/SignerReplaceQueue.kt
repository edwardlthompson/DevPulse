package dev.foss.goldenpath.inventory

import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Holds cert-clash APKs from Update all so the user can replace them after matching installs. */
object SignerReplaceQueue {
    const val FILE = "signing_issues.tsv"

    @Volatile
    var rows: List<SignerReplaceHold> = emptyList()
        private set

    private val lock = Any()
    private val revisionState = MutableStateFlow(0)
    val revision: StateFlow<Int> = revisionState.asStateFlow()
    private var persist: File? = null

    fun file(filesDir: File): File = File(filesDir, FILE)

    fun has(packageName: String): Boolean {
        val pkg = packageName.trim()
        return pkg.isNotEmpty() && rows.any { it.packageName == pkg }
    }

    fun hydrate(filesDir: File) {
        synchronized(lock) {
            persist = file(filesDir)
            rows = load(persist!!)
            revisionState.value += 1
        }
    }

    fun remember(filesDir: File?, job: UpdateAllJob, files: List<File>): Boolean {
        val pkg = job.packageName.trim()
        if (pkg.isEmpty() || filesDir == null) return false
        synchronized(lock) {
            if (rows.any { it.packageName == pkg }) return true
            val staged = SignerReplaceStore.stage(filesDir, pkg, files) ?: return false
            rows = rows + SignerReplaceHold(pkg, job.label, job.source, staged.map { it.absolutePath })
            persist = file(filesDir)
            save(persist!!, rows)
            revisionState.value += 1
            return true
        }
    }

    fun drop(filesDir: File?, packageName: String, deleteFiles: Boolean = true) {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return
        synchronized(lock) {
            val next = rows.filterNot { it.packageName == pkg }
            if (next.size == rows.size) return
            rows = next
            if (filesDir != null) persist = file(filesDir)
            persist?.let { save(it, rows) }
            if (deleteFiles && filesDir != null) {
                runCatching { File(filesDir, "signer-replace/$pkg").deleteRecursively() }
            }
            revisionState.value += 1
        }
    }

    fun clear() {
        synchronized(lock) {
            rows = emptyList()
            persist = null
            revisionState.value += 1
        }
    }

    internal fun load(file: File): List<SignerReplaceHold> {
        if (!file.isFile) return emptyList()
        return runCatching { file.readLines().mapNotNull(SignerReplaceStore::parse) }.getOrDefault(emptyList())
    }

    private fun save(file: File, rows: List<SignerReplaceHold>) {
        file.parentFile?.mkdirs()
        val body = rows.joinToString("\n") { hold ->
            listOf(
                hold.packageName,
                hold.label.replace('\t', ' ').replace('\n', ' '),
                hold.source.name,
                hold.paths.joinToString("|"),
            ).joinToString("\t")
        }
        runCatching { file.writeText(body) }
    }
}
