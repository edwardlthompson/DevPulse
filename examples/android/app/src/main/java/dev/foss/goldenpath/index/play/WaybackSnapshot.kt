package dev.foss.goldenpath.index.play

fun interface WaybackPlayClient {
    fun recover(packageName: String): PlayLookup?
}

object WaybackPlay {
    @Volatile
    var client: WaybackPlayClient? = null
}

object WaybackSnapshot {
    private val available = Regex(""""available"\s*:\s*true""")
    private val snapshot = Regex(""""url"\s*:\s*"(https?://web\.archive\.org/web/[^"]+)"""")
    private val stamp = Regex("""https?://web\.archive\.org/web/(\d+)/""")

    fun snapshotUrl(json: String): String? {
        if (!available.containsMatchIn(json)) return null
        val raw = snapshot.find(json)?.groupValues?.get(1)?.replace("\\/", "/") ?: return null
        return idUrl(raw)
    }

    fun idUrl(url: String): String {
        val match = stamp.find(url) ?: return url.replace("http://", "https://")
        val rest = url.substring(match.range.last + 1)
        return "https://web.archive.org/web/${match.groupValues[1]}id_/$rest"
    }

    fun availabilityUrl(packageName: String): String {
        val encoded = java.net.URLEncoder.encode(
            "https://play.google.com/store/apps/details?id=$packageName",
            Charsets.UTF_8.name(),
        )
        return "https://archive.org/wayback/available?url=$encoded"
    }
}
