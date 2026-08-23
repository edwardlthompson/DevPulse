package dev.foss.goldenpath.index.play

import dev.foss.goldenpath.index.forge.ForgeUrl
import java.util.concurrent.ConcurrentHashMap

object PlaySourceHints {
    private val urls = ConcurrentHashMap<String, String>()

    fun note(packageName: String, url: String?) {
        val pkg = packageName.trim()
        val href = ForgeUrl.downloadPage(url) ?: return
        if (pkg.isEmpty()) return
        urls[pkg] = href
    }

    fun snapshot(): Map<String, String> = urls.toMap()

    fun clear() {
        urls.clear()
    }
}
