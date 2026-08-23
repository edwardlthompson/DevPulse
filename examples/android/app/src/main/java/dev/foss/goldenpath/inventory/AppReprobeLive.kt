package dev.foss.goldenpath.inventory

import android.content.Context
import android.util.Log
import dev.foss.goldenpath.index.aptoide.AptoideHttpFetcher
import dev.foss.goldenpath.index.aurora.AuroraPlayLive
import dev.foss.goldenpath.index.forge.EncryptedForgeTokenStore
import dev.foss.goldenpath.index.forge.FileGithubVerifiedStore
import dev.foss.goldenpath.index.forge.GitHubSearchHttp
import dev.foss.goldenpath.index.forge.GithubHint
import dev.foss.goldenpath.index.play.PlayHttpFetcher
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object AppReprobeLive {
    fun run(context: Context, app: InstalledApp): Int {
        AppReprobe.forgetFetched(app.packageName)
        val prefs = InventoryPreferences(context)
        val (playOn, aptoideOn, forgeOn) = runBlocking {
            Triple(
                prefs.playLookupEnabled.first(),
                prefs.aptoideLookupEnabled.first(),
                prefs.forgeLookupEnabled.first(),
            )
        }
        val nowMs = System.currentTimeMillis()
        val offers = mutableListOf<RemoteReleaseOffer>()
        if (playOn) {
            offers += ReleaseRefreshProbes.play(
                app.packageName,
                PlayHttpFetcher,
                nowMs,
                AuroraPlayLive.details(context),
            )
        }
        if (aptoideOn) {
            offers += ReleaseRefreshProbes.aptoide(app.packageName, AptoideHttpFetcher, nowMs)
        }
        if (forgeOn) {
            val hint = FileGithubVerifiedStore(File(context.filesDir, "github_verified.tsv"))
                .load()[app.packageName]?.let { GithubHint(it) }
            offers += ReleaseRefreshProbes.github(
                app.packageName,
                app.label,
                GitHubSearchHttp(EncryptedForgeTokenStore.wrap(context).getToken()),
                hint = hint,
                nowMs = nowMs,
            )
        }
        AppReprobe.apply(app.packageName, offers)
        val listed = offers.count { it.listed }
        Log.i("DevPulse", "reprobe ${app.packageName} listed=$listed")
        return listed
    }
}
