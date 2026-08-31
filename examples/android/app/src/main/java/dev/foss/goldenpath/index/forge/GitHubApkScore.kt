package dev.foss.goldenpath.index.forge

object GitHubApkScore {
    fun bestApkUrl(urls: List<String>, packageName: String = ""): String? {
        if (urls.isEmpty()) return null
        if (urls.size == 1) return urls.first()

        val pkg = packageName.trim().lowercase()
        val isFdroidPkg = pkg.endsWith(".fdroid") || pkg.contains(".fdroid.") || pkg == "fdroid"

        return urls.maxByOrNull { url ->
            val filename = GithubAppOptCodec.filename(url).lowercase()
            var score = 0

            val hasFdroidInName = filename.contains("fdroid")
            if (isFdroidPkg) {
                if (hasFdroidInName) score += 1000 else score -= 1000
            } else if (pkg.isNotEmpty()) {
                if (hasFdroidInName) score -= 1000 else score += 100
            }

            if (pkg.isNotEmpty()) {
                val pkgSimple = pkg.replace('.', '-')
                val pkgUnderscore = pkg.replace('.', '_')
                val leaf = pkg.substringAfterLast('.')
                if (filename.contains(pkg) || filename.contains(pkgSimple) || filename.contains(pkgUnderscore)) {
                    score += 500
                } else if (leaf.length >= 3 && filename.contains(leaf)) {
                    score += 200
                }
            }

            val hasArm64 = filename.contains("arm64") || filename.contains("aarch64")
            val hasArm32 = filename.contains("armeabi") || filename.contains("armv7") || (filename.contains("arm") && !hasArm64)
            val hasX86_64 = filename.contains("x86_64") || filename.contains("x64")
            val hasX86 = filename.contains("x86") && !hasX86_64
            val hasAnyAbi = hasArm64 || hasArm32 || hasX86_64 || hasX86

            if (hasArm64) {
                score += 50
            } else if (!hasAnyAbi) {
                score += 30
            } else if (hasArm32) {
                score += 15
            } else {
                score += 5
            }

            if (filename.contains("debug") || filename.contains("unsigned")) {
                score -= 100
            } else if (filename.contains("release")) {
                score += 10
            }

            score
        }
    }
}
