package dev.foss.goldenpath.index.aurora

import dev.foss.goldenpath.inventory.ApkDownloadUrl

object AuroraPlayBundle {
    fun files(packageName: String, live: AuroraPlayFiles): List<AuroraPlayFile> {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return emptyList()
        val usable = live.files(pkg).mapNotNull { file ->
            val url = ApkDownloadUrl.httpsFile(file.url) ?: return@mapNotNull null
            file.copy(url = url)
        }
        val base = usable.filter { it.base }
        if (base.isEmpty()) return usable
        return base + usable.filter { !it.base }
    }
}
