package dev.foss.goldenpath.inventory

object SettingsPack {
    fun encode(values: Map<String, String>): String =
        values.entries.sortedBy { it.key }.joinToString("\n") { "${it.key}=${it.value.replace("\n", " ")}" }

    fun decode(raw: String): Map<String, String> {
        if (raw.isBlank()) return emptyMap()
        return raw.lineSequence().mapNotNull { line ->
            val eq = line.indexOf('=')
            if (eq <= 0) return@mapNotNull null
            val key = line.substring(0, eq).trim()
            if (key.isEmpty()) return@mapNotNull null
            key to line.substring(eq + 1).trim()
        }.toMap()
    }
}
