package dev.foss.goldenpath.crashcapture

object CrashInbox {
    fun enabled(raw: String): Boolean =
        Regex("\"enabled\"\\s*:\\s*true").containsMatchIn(raw)
}
