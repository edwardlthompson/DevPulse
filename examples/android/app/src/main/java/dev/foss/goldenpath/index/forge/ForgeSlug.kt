package dev.foss.goldenpath.index.forge

/** Compact repo-name match so leftovers bind without the package id in the APK filename. */
object ForgeSlug {
    private const val MIN = 6

    fun compact(raw: String): String =
        raw.lowercase().replace(Regex("[^a-z0-9]+"), "")

    fun matches(ownerRepo: String, label: String, packageName: String): Boolean {
        val repo = compact(ownerRepo.substringAfter('/').removeSuffix(".git"))
        if (repo.length < MIN) return false
        val labelBits = compact(label)
        if (labelBits.length >= MIN && repo == labelBits) return true
        return packageName.split('.').any { part ->
            val bit = compact(part)
            bit.length >= MIN && (repo == bit || repo.contains(bit) && bit.length >= 8)
        }
    }
}
