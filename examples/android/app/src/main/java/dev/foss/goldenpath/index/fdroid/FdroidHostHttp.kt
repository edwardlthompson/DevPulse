package dev.foss.goldenpath.index.fdroid

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

class FdroidHostHttp(
    private val api: FdroidPackageClient = FdroidPackageClient { pkg -> get(apiUrl(pkg)) },
    private val pages: FdroidPageClient = FdroidPageClient { pkg -> get(pageUrl(pkg)) },
) : FdroidHostResolver {
    private val cache = ConcurrentHashMap<String, FdroidAppRecord?>()

    override fun resolve(repo: FdroidRepo, wanted: Set<String>): List<FdroidAppRecord> =
        wanted.mapNotNull { pkg ->
            val key = "${repo.id}:$pkg"
            val hit = cache[key] ?: fetch(repo, pkg)?.also { cache[key] = it }
            hit?.copy(repoId = repo.id)
        }

    private fun fetch(repo: FdroidRepo, pkg: String): FdroidAppRecord? =
        if (FdroidIndexBudget.extraHostResolve(repo)) {
            FdroidHostResolve.pageRecord(pkg, repo.id, extraPage(repo, pkg).getOrNull())
        } else {
            FdroidHostResolve.record(
                pkg,
                repo.id,
                api.packageJson(pkg).getOrNull(),
                pages.packagePage(pkg).getOrNull(),
            )
        }

    companion object {
        private const val USER_AGENT = "DevPulse/0.1 (https://github.com/edwardlthompson/DevPulse)"

        fun apiUrl(packageName: String): String =
            "https://f-droid.org/api/v1/packages/${pathSeg(packageName)}"

        fun pageUrl(packageName: String): String =
            "https://f-droid.org/packages/${pathSeg(packageName)}/"

        fun extraPageUrl(repo: FdroidRepo, packageName: String): String? =
            if (FdroidIndexBudget.extraHostResolve(repo)) {
                "https://apt.izzysoft.de/fdroid/index/apk/${pathSeg(packageName)}"
            } else {
                null
            }

        private fun extraPage(repo: FdroidRepo, packageName: String): Result<String> {
            val url = extraPageUrl(repo, packageName) ?: return Result.failure(IllegalArgumentException("no extra page"))
            return get(url)
        }

        private fun pathSeg(raw: String): String =
            URLEncoder.encode(raw, Charsets.UTF_8.name()).replace("+", "%20")

        private fun get(url: String): Result<String> = runCatching {
            val conn = URL(url).openConnection() as HttpURLConnection
            try {
                conn.requestMethod = "GET"
                conn.setRequestProperty("User-Agent", USER_AGENT)
                conn.instanceFollowRedirects = true
                conn.connectTimeout = 15_000
                conn.readTimeout = 20_000
                if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                    error("fdroid package ${conn.responseCode}")
                }
                conn.inputStream.bufferedReader().use { it.readText() }
            } finally {
                conn.disconnect()
            }
        }
    }
}
