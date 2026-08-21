package dev.foss.goldenpath.about

/** Continuum-style self-update and donate rules. Prefs stay device-local. */
object ProductUpdate {
    const val MS_DAY = 86_400_000L
    const val RELEASE_REPO = "edwardlthompson/DevPulse"
    const val RELEASES_API = "https://api.github.com/repos/$RELEASE_REPO/releases/latest"
    const val RELEASES_PAGE = "https://github.com/$RELEASE_REPO/releases/latest"
    const val VENMO_URL = "https://venmo.com/code?user_id=1857304970395648420"

    data class NamedAsset(val name: String, val url: String)
    data class ProductAsset(val version: String, val url: String)

    fun shouldCheckDaily(lastCheckAt: Long?, now: Long): Boolean {
        if (lastCheckAt == null || lastCheckAt < 0L) return true
        return now - lastCheckAt >= MS_DAY
    }

    fun isNewerVersion(current: String, latest: String): Boolean {
        fun parts(v: String) = v.split('.').map { it.toIntOrNull() ?: 0 }
        val a = parts(current)
        val b = parts(latest)
        for (i in 0..2) {
            val diff = (a.getOrElse(i) { 0 }) - (b.getOrElse(i) { 0 })
            if (diff != 0) return diff < 0
        }
        return false
    }

    fun parseApkVersion(name: String): String? {
        val match = Regex("devpulse-(\\d+\\.\\d+\\.\\d+)(?:-foss)?\\.apk", RegexOption.IGNORE_CASE)
            .find(name.trim())
        return match?.groupValues?.get(1)
    }

    fun selectApkAsset(assets: List<NamedAsset>): ProductAsset? {
        for (asset in assets) {
            val version = parseApkVersion(asset.name) ?: continue
            if (asset.url.isNotBlank()) return ProductAsset(version, asset.url)
        }
        return null
    }

    fun installUrl(assetUrl: String?, htmlUrl: String?): String {
        return assetUrl?.takeIf { it.isNotBlank() }
            ?: htmlUrl?.takeIf { it.isNotBlank() }
            ?: RELEASES_PAGE
    }

    fun shouldNudgeDonate(lastSeenVersion: String?, currentVersion: String): Boolean {
        if (currentVersion.isBlank()) return false
        if (lastSeenVersion.isNullOrBlank()) return false
        return lastSeenVersion.trim() != currentVersion.trim()
    }

    fun shouldPromptUpdate(
        currentVersion: String,
        latestVersion: String?,
        dismissedVersion: String?,
    ): Boolean {
        if (latestVersion.isNullOrBlank()) return false
        if (!isNewerVersion(currentVersion, latestVersion)) return false
        if (dismissedVersion == latestVersion) return false
        return true
    }
}
