package dev.foss.goldenpath.opportunity

data class CategoryGap(
    val category: String,
    val quietCount: Int,
)

data class DevelopNextNote(
    val packageName: String,
    val note: String,
)

data class SelfPulseConfig(
    val packageName: String,
    val repo: String,
)

object OpportunityRanker {
    fun gaps(categoryByPackage: Map<String, String>, quietPackages: Set<String>): List<CategoryGap> =
        categoryByPackage.entries
            .filter { it.key in quietPackages }
            .groupBy({ it.value }, { it.key })
            .map { (category, packages) -> CategoryGap(category, packages.size) }
            .sortedByDescending { it.quietCount }

    fun selfPulseMatches(config: SelfPulseConfig, packageName: String): Boolean =
        config.packageName == packageName
}
