package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.forge.GitHubReleaseClient
import dev.foss.goldenpath.index.forge.GitHubSearchClient
import dev.foss.goldenpath.index.forge.GitHubSearchPage
import dev.foss.goldenpath.index.forge.GitHubSearchPace
import dev.foss.goldenpath.index.forge.GithubHint
import dev.foss.goldenpath.index.forge.GithubVerifiedStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class GitHubDiscoverTest {
    @Before
    fun reset() {
        GitHubSearchPace.reset()
        RemoteReleaseMemory.clear()
    }

    @Test
    fun queuedSkipsHintListedNoiseAndSkip() {
        val now = 1_720_000_000_000L
        val apps = listOf(
            sampleApp("org.continuumcalendar.app", "Continuum"),
            sampleApp("org.hinted.app", "Hinted"),
            sampleApp("com.play.app", "Play"),
            sampleApp("org.chromium.webapk.foo", "WebAPK"),
            sampleApp("org.none.app", "None"),
            sampleApp("app.devpulse", "DevPulse"),
        )
        RemoteReleaseMemory.putAll(
            mapOf(
                "com.play.app" to RemoteReleaseRollup.from(
                    listOf(RemoteReleaseOffer(RemoteReleasedSource.Play, 1L, "1.0")),
                ),
            ),
        )
        val skip = mapOf("org.none.app" to now)
        val queued = GitHubDiscover.queued(
            apps,
            mapOf("org.hinted.app" to GithubHint("owner/hint")),
            skip,
            now,
            listed = { ReleaseRefreshProbes.storeListed(it) },
        )
        assertEquals(listOf("org.continuumcalendar.app"), queued.map { it.packageName })
    }

    @Test
    fun tickPersistsHitAndSkipMiss() {
        val now = 1_720_000_000_000L
        val store = memStore()
        val searches = AtomicInteger(0)
        val client = object : GitHubSearchClient, GitHubReleaseClient {
            override fun searchRepos(query: String): GitHubSearchPage {
                searches.incrementAndGet()
                return if (query.contains("org.continuumcalendar.app")) {
                    GitHubSearchPage(
                        200,
                        """{"items":[{"full_name":"owner/continuum-calendar","name":"continuum-calendar","pushed_at":"2026-09-15T00:00:00Z","archived":false}]}""",
                    )
                } else {
                    GitHubSearchPage(200, """{"items":[]}""")
                }
            }

            override fun listReleases(ownerRepo: String): GitHubSearchPage =
                GitHubSearchPage(
                    200,
                    """[{"tag_name":"v1.1.1","assets":[{"browser_download_url":"https://github.com/owner/continuum-calendar/releases/download/v1.1.1/app-release.apk"}]}]""",
                )
        }
        val tick = GitHubDiscover.tick(
            apps = listOf(
                sampleApp("org.continuumcalendar.app", "Continuum Calendar"),
                sampleApp("org.none.app", "None"),
            ),
            library = emptyMap(),
            skip = emptyMap(),
            client = client,
            verified = store,
            nowMs = now,
            listed = { false },
            pause = {},
        )
        assertEquals(2, tick.searched)
        assertEquals(1, tick.hits)
        assertEquals(1, tick.misses)
        assertEquals("owner/continuum-calendar", store.rows["org.continuumcalendar.app"])
        assertTrue(tick.skip.containsKey("org.none.app"))
        assertEquals(0, tick.remaining)
        assertEquals(2, searches.get())
    }

    @Test
    fun tickCapsAtFiveAndLeavesRemaining() {
        val now = 1_720_000_000_000L
        val apps = (1..7).map { sampleApp("org.none.app$it", "None $it") }
        val tick = GitHubDiscover.tick(
            apps = apps,
            library = emptyMap(),
            skip = emptyMap(),
            client = GitHubSearchClient { GitHubSearchPage(200, """{"items":[]}""") },
            verified = memStore(),
            nowMs = now,
            listed = { false },
            pause = {},
        )
        assertEquals(5, tick.searched)
        assertEquals(5, tick.misses)
        assertEquals(2, tick.remaining)
    }

    private fun memStore() = object : GithubVerifiedStore {
        var rows = emptyMap<String, String>()
        override fun load() = rows
        override fun save(all: Map<String, String>) {
            rows = all
        }
        override fun put(packageName: String, ownerRepo: String) {
            rows = rows + (packageName to ownerRepo)
        }
    }
}
