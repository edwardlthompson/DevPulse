package dev.foss.goldenpath.settings

import dev.foss.goldenpath.index.fdroid.FdroidCustomIndex
import dev.foss.goldenpath.index.forge.GithubAdd
import dev.foss.goldenpath.index.forge.PastedRepoCodec

enum class SourceToggleId {
    Play,
    Aurora,
    Forge,
    SearchUnknowns,
    Starred,
    Aptoide,
    ApkMirror,
    ApkPure,
    CustomFdroid,
}

data class SourceGateState(
    val githubTokenPresent: Boolean,
    val customIndexUrl: String,
)

object SourceToggleGate {
    fun allowOn(id: SourceToggleId, state: SourceGateState): Boolean = when (id) {
        SourceToggleId.SearchUnknowns, SourceToggleId.Starred -> state.githubTokenPresent
        SourceToggleId.CustomFdroid -> FdroidCustomIndex.valid(state.customIndexUrl)
        else -> true
    }
}

object SourceFieldValidate {
    fun leftoverToken(raw: String): Boolean {
        val token = raw.trim()
        return token.length in 8..256 && token.none { it.isWhitespace() }
    }

    fun paste(packageName: String, url: String): Boolean =
        PastedRepoCodec.normalize(packageName, url) != null && GithubAdd.ownerRepo(url) != null
}
