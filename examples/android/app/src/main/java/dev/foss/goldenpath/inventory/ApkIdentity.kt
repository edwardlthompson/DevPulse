package dev.foss.goldenpath.inventory

import java.io.File
import java.security.MessageDigest

data class ApkInspect(
    val packageName: String?,
    val signers: Set<String>,
    val nativeCodes: Set<String> = emptySet(),
)

data class InstalledIdentity(
    val packageName: String,
    val signers: Set<String>,
    val abis: Set<String> = emptySet(),
)

object ApkIdentity {
    fun digest(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { byte -> "%02x".format(byte) }
    }

    fun digestFile(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buf = ByteArray(8192)
            while (true) {
                val n = input.read(buf)
                if (n <= 0) break
                digest.update(buf, 0, n)
            }
        }
        return digest.digest().joinToString("") { byte -> "%02x".format(byte) }
    }

    fun hashesMatch(expected: String?, actual: String): Boolean {
        val want = expected?.trim()?.lowercase()?.ifEmpty { null } ?: return true
        return want == actual.lowercase()
    }

    fun signersMatch(apk: Set<String>, installed: Set<String>?): Boolean {
        if (installed.isNullOrEmpty()) return true
        if (apk.isEmpty()) return false
        return apk.intersect(installed).isNotEmpty()
    }

    fun identityReady(artifact: UpdateArtifact, inspect: ApkInspect, installed: InstalledIdentity): Boolean {
        val pkg = artifact.packageName.trim()
        if (pkg.isEmpty() || inspect.packageName != pkg || installed.packageName != pkg) return false
        if (inspect.signers.isEmpty() || installed.signers.isEmpty()) return false
        if (inspect.signers.intersect(installed.signers).isEmpty()) return false
        val natives = artifact.nativeCodes.ifEmpty { inspect.nativeCodes }
        if (natives.isNotEmpty() && installed.abis.isNotEmpty() && natives.intersect(installed.abis).isEmpty()) {
            return false
        }
        return true
    }
}
