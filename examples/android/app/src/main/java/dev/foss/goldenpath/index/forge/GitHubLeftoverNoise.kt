package dev.foss.goldenpath.index.forge

/** Leftover search skips Chrome WebAPKs and this app (self-pulse has its own GitHub check). */
object GitHubLeftoverNoise {
    fun skip(packageName: String): Boolean {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return true
        if (pkg == "app.devpulse") return true
        return pkg.startsWith("org.chromium.webapk.")
    }
}
