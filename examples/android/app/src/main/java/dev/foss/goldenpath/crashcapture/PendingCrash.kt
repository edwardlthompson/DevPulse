package dev.foss.goldenpath.crashcapture

import java.io.File

object PendingCrash {
    fun file(dir: File): File = File(dir, "pending_crash.txt")

    fun write(dir: File, text: String): Boolean {
        val clean = text.trim()
        if (clean.isEmpty()) return false
        return runCatching {
            file(dir).writeText(clean)
        }.isSuccess
    }

    fun read(dir: File): String? {
        val stored = runCatching { file(dir).takeIf { it.isFile }?.readText() }.getOrNull()
        return stored?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun clear(dir: File) {
        runCatching { file(dir).delete() }
    }
}
