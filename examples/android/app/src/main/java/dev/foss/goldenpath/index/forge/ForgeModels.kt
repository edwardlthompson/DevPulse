package dev.foss.goldenpath.index.forge

enum class ForgeHost {
    GitHub,
    GitLab,
    Codeberg,
}

enum class MatchConfidence {
    ExactPackage,
    GradleId,
    PackageInRepo,
    TitleFuzzy,
}

data class ForgeCandidate(
    val host: ForgeHost,
    val ownerRepo: String,
    val packageId: String?,
    val title: String,
    val latestCommitMs: Long?,
    val latestReleaseMs: Long?,
    val archived: Boolean,
    val description: String? = null,
)

data class ForgeMatch(
    val candidate: ForgeCandidate,
    val confidence: MatchConfidence,
)

data class PastedRepo(
    val packageName: String,
    val url: String,
)

interface ForgeTokenStore {
    fun getToken(): String?
    fun setToken(token: String?)
}
