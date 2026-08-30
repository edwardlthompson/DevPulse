package dev.foss.goldenpath.inventory

import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow

data class SignerReplaceHold(
    val packageName: String,
    val label: String,
    val source: RemoteReleasedSource,
    val paths: List<String>,
) {
    fun files(): List<File> = paths.map(::File)
}

object SignerReplaceStore {
    val pending = MutableStateFlow<SignerReplaceHold?>(null)

    fun file(filesDir: File): File = File(filesDir, "signer_replace.tsv")

    fun stage(filesDir: File, packageName: String, files: List<File>): List<File>? {
        if (!SignerClash.filesReady(files)) return null
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return null
        val dir = File(filesDir, "signer-replace/$pkg")
        if (!dir.mkdirs() && !dir.isDirectory) return null
        val copied = runCatching {
            files.map { src ->
                val dest = File(dir, src.name.ifEmpty { "app.apk" })
                src.copyTo(dest, overwrite = true)
                dest
            }
        }.getOrNull() ?: return null
        return copied.takeIf { SignerClash.filesReady(it) }
    }

    fun capture(
        filesDir: File,
        packageName: String,
        label: String,
        source: RemoteReleasedSource,
        files: List<File>,
    ): Boolean {
        val staged = stage(filesDir, packageName, files) ?: return false
        save(filesDir, SignerReplaceHold(packageName, label, source, staged.map { it.absolutePath }))
        return true
    }

    fun save(filesDir: File, hold: SignerReplaceHold) {
        val line = listOf(
            hold.packageName,
            hold.label.replace('\t', ' '),
            hold.source.name,
            hold.paths.joinToString("|"),
        ).joinToString("\t")
        runCatching { file(filesDir).writeText(line) }
        pending.value = hold
    }

    fun load(filesDir: File): SignerReplaceHold? {
        val held = pending.value ?: parse(runCatching { file(filesDir).readText() }.getOrNull())
        pending.value = held
        return held
    }

    fun installable(filesDir: File, installed: (String) -> Boolean): SignerReplaceHold? {
        load(filesDir)
        val held = pending.value
        if (held != null && gone(held, installed)) return held
        val row = SignerReplaceQueue.rows.firstOrNull { gone(it, installed) } ?: return null
        save(filesDir, row)
        return row
    }

    private fun gone(hold: SignerReplaceHold, installed: (String) -> Boolean): Boolean =
        !installed(hold.packageName) && SignerClash.filesReady(hold.files())

    fun clear(filesDir: File, deleteFiles: Boolean = true, packageName: String? = null) {
        val pkg = (packageName ?: pending.value?.packageName).orEmpty().trim()
        if (pkg.isEmpty()) return
        if (pending.value?.packageName == pkg) {
            pending.value = null
            runCatching { file(filesDir).delete() }
        }
        if (!deleteFiles) return
        runCatching { File(filesDir, "signer-replace/$pkg").deleteRecursively() }
        SignerReplaceQueue.drop(filesDir, pkg, deleteFiles = false)
    }

    fun parse(raw: String?): SignerReplaceHold? {
        val cols = raw?.trim()?.split('\t') ?: return null
        if (cols.size < 4) return null
        val pkg = cols[0].trim()
        val source = runCatching { RemoteReleasedSource.valueOf(cols[2].trim()) }.getOrNull()
        val paths = cols[3].split('|').map { it.trim() }.filter { it.isNotEmpty() }
        if (pkg.isEmpty() || source == null || paths.isEmpty()) return null
        return SignerReplaceHold(pkg, cols[1], source, paths)
    }
}
