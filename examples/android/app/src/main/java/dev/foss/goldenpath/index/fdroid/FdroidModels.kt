package dev.foss.goldenpath.index.fdroid

enum class FdroidRepoKind {
    Official,
    Archive,
    Izzy,
    Guardian,
    Calyx,
    Vendor,
    Custom,
}

data class FdroidRepo(
    val id: String,
    val kind: FdroidRepoKind,
    val indexUrl: String,
    val enabled: Boolean,
)

data class FdroidAppRecord(
    val packageName: String,
    val lastUpdatedMs: Long?,
    val sourceCode: String?,
    val repoId: String,
    val suggestedVersionName: String? = null,
    val whatsNew: String? = null,
    val apkName: String? = null,
    val apkSha256: String? = null,
    val nativeCodes: Set<String> = emptySet(),
    val category: String? = null,
    val relatedPackages: List<String> = emptyList(),
)

data class CachedIndex(
    val raw: String,
    val fetchedAtMs: Long,
)

enum class FdroidIndexError {
    DownloadFailed,
    ParseFailed,
    NotFound,
}

data class FdroidLookup(
    val record: FdroidAppRecord?,
    val fromCache: Boolean,
    val error: FdroidIndexError?,
)

fun interface FdroidIndexFetcher {
    fun fetch(url: String): Result<ByteArray>
}
