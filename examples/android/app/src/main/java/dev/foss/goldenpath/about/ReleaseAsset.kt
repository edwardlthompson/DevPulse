package dev.foss.goldenpath.about

data class ReleaseAsset(
    val format: String,
    val url: String,
    val sha256: String? = null,
)

object ReleaseAssetSelector {
    fun select(assets: List<ReleaseAsset>, installedFormat: String): ReleaseAsset? {
        val normalized = installedFormat.lowercase().removePrefix(".")
        return assets.firstOrNull {
            it.format.lowercase().removePrefix(".") == normalized
        }
    }
}
