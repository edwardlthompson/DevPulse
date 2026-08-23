package dev.foss.goldenpath.opportunity

import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.InventoryExport
import dev.foss.goldenpath.inventory.UsageSnapshot

object OpportunityExport {
    fun quietTitles(
        apps: List<InstalledApp>,
        usage: List<UsageSnapshot> = emptyList(),
    ): List<String> {
        val used = usage.associate { it.packageName to it.lastTimeUsedMs }
        return apps.sortedByDescending { used[it.packageName] ?: 0L }.map { it.label.trim() }.filter { it.isNotEmpty() }
    }

    fun csv(titles: List<String>, gaps: List<CategoryGap>): String = buildString {
        append("kind,name,count\n")
        titles.forEach { append("quiet,").append(InventoryExport.escapeCsv(it)).append(",\n") }
        gaps.forEach { gap ->
            append("gap,").append(InventoryExport.escapeCsv(gap.category)).append(',').append(gap.quietCount).append('\n')
        }
    }

    fun json(titles: List<String>, gaps: List<CategoryGap>): String = buildString {
        append("{\"quiet\":[")
        append(titles.joinToString(",") { "\"${it.replace("\\", "\\\\").replace("\"", "\\\"")}\"" })
        append("],\"gaps\":[")
        append(
            gaps.joinToString(",") { gap ->
                val name = gap.category.replace("\\", "\\\\").replace("\"", "\\\"")
                "{\"category\":\"$name\",\"quietCount\":${gap.quietCount}}"
            },
        )
        append("]}")
    }

    fun markdown(titles: List<String>, gaps: List<CategoryGap>): String = buildString {
        append("# Fork list\n")
        titles.forEach { append("- ").append(it).append('\n') }
        if (gaps.isNotEmpty()) {
            append("\n## Gaps\n")
            gaps.forEach { append("- ").append(it.category).append(" (").append(it.quietCount).append(")\n") }
        }
    }
}
