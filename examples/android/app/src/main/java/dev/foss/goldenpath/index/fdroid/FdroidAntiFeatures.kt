package dev.foss.goldenpath.index.fdroid

object FdroidAntiFeatures {
    private val block = Regex(""""antiFeatures"\s*:\s*\[([^\]]*)\]""")

    fun parse(chunk: String): List<String> {
        val raw = block.find(chunk)?.groupValues?.get(1) ?: return emptyList()
        return raw.split(',')
            .map { it.trim().trim('"') }
            .filter { it.isNotEmpty() }
            .distinct()
    }
}
