package dev.foss.goldenpath.index.fdroid

fun interface FdroidPackageClient {
    fun packageJson(packageName: String): Result<String>
}

fun interface FdroidPageClient {
    fun packagePage(packageName: String): Result<String>
}

fun interface FdroidHostResolver {
    fun resolve(repo: FdroidRepo, wanted: Set<String>): List<FdroidAppRecord>
}

object FdroidHostResolve {
    fun record(
        packageName: String,
        repoId: String,
        apiJson: String?,
        pageHtml: String?,
    ): FdroidAppRecord? {
        val page = pageHtml?.let { FdroidPackagePage.parse(it, packageName) }
        val parsed = apiJson?.let { FdroidPackageParser.parse(packageName, it, repoId) }
        if (parsed == null) return null
        return parsed.copy(
            lastUpdatedMs = page?.lastUpdatedMs ?: parsed.lastUpdatedMs,
            sourceCode = page?.sourceCode ?: parsed.sourceCode,
            category = page?.category,
            relatedPackages = page?.relatedPackages.orEmpty(),
            apkName = page?.apkName ?: parsed.apkName,
        )
    }

    fun pageRecord(packageName: String, repoId: String, pageHtml: String?): FdroidAppRecord? {
        val page = pageHtml?.let { FdroidPackagePage.parse(it, packageName) } ?: return null
        if (page.lastUpdatedMs == null && page.sourceCode == null && page.category == null && page.apkName == null) {
            return null
        }
        return FdroidAppRecord(
            packageName = packageName,
            lastUpdatedMs = page.lastUpdatedMs,
            sourceCode = page.sourceCode,
            repoId = repoId,
            category = page.category,
            relatedPackages = page.relatedPackages,
            apkName = page.apkName,
        )
    }

    fun records(
        wanted: Set<String>,
        repo: FdroidRepo,
        api: FdroidPackageClient,
        pages: FdroidPageClient,
    ): List<FdroidAppRecord> = wanted.mapNotNull { pkg ->
        val json = api.packageJson(pkg).getOrNull()
        val html = pages.packagePage(pkg).getOrNull()
        record(pkg, repo.id, json, html)
    }
}
