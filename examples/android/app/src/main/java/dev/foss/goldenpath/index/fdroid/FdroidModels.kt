package dev.foss.goldenpath.index.fdroid

enum class FdroidRepoKind {
    Official,
    Archive,
    Izzy,
    Guardian,
    Calyx,
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
