package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.about.ProductUpdate

data class GithubCatalogPage(
    val status: Int,
    val body: String,
    val etag: String? = null,
)

fun interface GithubCatalogFetcher {
    fun get(url: String, etag: String?): GithubCatalogPage
}

data class GithubCatalogApply(
    val rows: Map<String, String>,
    val etag: String?,
    val saved: Boolean,
)

object GithubCatalogSync {
    const val RAW_PATH = "examples/android/app/src/main/assets/github-repos/verified.tsv"

    fun rawUrl(repo: String = ProductUpdate.RELEASE_REPO): String =
        "https://raw.githubusercontent.com/$repo/main/$RAW_PATH"

    fun due(lastPullAt: Long?, now: Long): Boolean = ProductUpdate.shouldCheckDaily(lastPullAt, now)

    fun merge(local: Map<String, String>, pulled: Map<String, String>): Map<String, String> =
        pulled + local

    fun apply(page: GithubCatalogPage, local: Map<String, String>, etag: String?): GithubCatalogApply {
        if (page.status == 304) {
            return GithubCatalogApply(local, etag ?: page.etag, saved = false)
        }
        if (page.status !in 200..299) {
            return GithubCatalogApply(local, etag, saved = false)
        }
        val pulled = GithubVerifiedCodec.decode(page.body)
        if (pulled.isEmpty()) {
            return GithubCatalogApply(local, page.etag ?: etag, saved = false)
        }
        return GithubCatalogApply(merge(local, pulled), page.etag ?: etag, saved = true)
    }
}
