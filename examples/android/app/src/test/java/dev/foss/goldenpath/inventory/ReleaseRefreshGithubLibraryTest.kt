package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.aptoide.AptoideMetaFetcher
import dev.foss.goldenpath.index.fdroid.FdroidIndexFetcher
import dev.foss.goldenpath.index.fdroid.FdroidRepo
import dev.foss.goldenpath.index.fdroid.FdroidRepoKind
import dev.foss.goldenpath.index.forge.GithubVerifiedStore
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ReleaseRefreshGithubLibraryTest {
    @Before
    fun reset() {
        RemoteReleaseMemory.clear()
        RefreshTrace.emit = {}
    }

    @Test
    fun harvestPersistsUninstalledGithubApps() {
        val store = object : GithubVerifiedStore {
            var rows = emptyMap<String, String>()
            override fun load() = rows
            override fun save(all: Map<String, String>) {
                rows = all
            }
            override fun put(packageName: String, ownerRepo: String) {
                rows = rows + (packageName to ownerRepo)
            }
        }
        val json = """{"apps":[
              {"packageName":"uk.org.platitudes.wipefiles","sourceCode":"https://github.com/peterhearty/WipeFiles"},
              {"packageName":"org.other.githubapp","sourceCode":"https://github.com/other/githubapp"},
              {"packageName":"org.guardian.app","sourceCode":"https://github.com/guardianproject/foo"}
            ]}"""
        val izzy = FdroidRepo("izzy", FdroidRepoKind.Izzy, "https://example/izzy.json", true)
        ReleaseRefresh.run(
            apps = listOf(sampleApp("uk.org.platitudes.wipefiles", "Wipe Files", installedAtMs = 1_600_000_000_000L)),
            repos = listOf(izzy),
            aptoideEnabled = false,
            fdroidFetcher = FdroidIndexFetcher { Result.success(json.toByteArray()) },
            aptoideFetcher = AptoideMetaFetcher { Result.success("") },
            nowMs = 1_720_000_000_000L,
            gitHubClient = { error("no search") },
            sleepMs = {},
            verifiedStore = store,
        )
        assertEquals("peterhearty/WipeFiles", store.rows["uk.org.platitudes.wipefiles"])
        assertEquals("other/githubapp", store.rows["org.other.githubapp"])
        assertEquals("guardianproject/foo", store.rows["org.guardian.app"])
    }
}
