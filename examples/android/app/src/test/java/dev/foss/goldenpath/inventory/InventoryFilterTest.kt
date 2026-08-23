package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InventoryFilterTest {
    private val user = sampleApp("app.user", "Alpha", isSystemApp = false)
    private val system = sampleApp("app.system", "Zebra", isSystemApp = true)

    @Test
    fun hidesSystemAppsByDefault() {
        assertEquals(listOf(user), InventoryFilter.visibleApps(listOf(user, system), includeSystem = false))
    }

    @Test
    fun includesSystemAppsWhenToggled() {
        assertEquals(listOf(user, system), InventoryFilter.visibleApps(listOf(user, system), includeSystem = true))
    }

    @Test
    fun blankQueryReturnsAll() {
        assertEquals(listOf(user, system), InventoryFilter.matchesQuery(listOf(user, system), "  "))
    }

    @Test
    fun queryMatchesLabelOrPackage() {
        assertEquals(listOf(user), InventoryFilter.matchesQuery(listOf(user, system), "alpha"))
        assertEquals(listOf(system), InventoryFilter.matchesQuery(listOf(user, system), "app.system"))
        val signed = user.copy(signingSha1 = "aa:bb:cc")
        assertEquals(listOf(signed), InventoryFilter.matchesQuery(listOf(signed), "AABBCC"))
    }

    @Test
    fun olderThanExcludesUnknownAnd1971() {
        val now = 1_700_000_000_000L
        val day = 86_400_000L
        val stale = sampleApp("app.stale", "Stale", installedAtMs = now - 200 * day)
        val fresh = sampleApp("app.fresh", "Fresh", installedAtMs = now - 10 * day)
        val junk = sampleApp("app.junk", "Junk", lastUpdateTimeMs = 31_536_000_000L)
        val visible = InventoryFilter.olderThan(listOf(stale, fresh, junk), minAgeDays = 180, nowMs = now)
        assertEquals(listOf("app.stale"), visible.map { it.packageName })
    }

    @Test
    fun withUpdatesKeepsNewerRemoteOnly() {
        val current = sampleApp("app.ok", "Ok", remoteVersionName = "1.0")
        val stale = sampleApp("app.up", "Up", remoteVersionName = "2.0")
        assertEquals(listOf(stale), UpdateInventory.withUpdates(listOf(current, stale)))
        assertTrue(UpdateInventory.hasUpdate(stale))
        assertFalse(UpdateInventory.hasUpdate(current))
    }

    @Test
    fun onGitHubKeepsListedKnownForgeOnly() {
        val listed = listedApp("app.git", "Git", RemoteReleasedSource.Forge, "https://github.com/a/b/releases")
        val miss = sampleApp("app.miss", "Miss", latestListings = listOf(UpdateLink(RemoteReleasedSource.Forge, listed = false, known = true)))
        val unknown = sampleApp("app.unk", "Unk", latestListings = listOf(UpdateLink(RemoteReleasedSource.Forge, listed = false, known = false)))
        assertEquals(listOf(listed), InventoryFilter.onGitHub(listOf(listed, miss, unknown)))
    }

    @Test
    fun onListedSourceKeepsGuardianOrIzzy() {
        val guardian = listedApp("app.guard", "Guard", RemoteReleasedSource.Guardian)
        val izzy = listedApp("app.izzy", "Izzy", RemoteReleasedSource.Izzy)
        val play = listedApp("app.play", "Play", RemoteReleasedSource.Play)
        val apps = listOf(guardian, izzy, play)
        assertEquals(listOf(guardian), InventoryFilter.onListedSource(apps, RemoteReleasedSource.Guardian))
        assertEquals(listOf(guardian, izzy), InventoryFilter.onAnyListedSource(apps, setOf(RemoteReleasedSource.Guardian, RemoteReleasedSource.Izzy)))
        assertEquals(apps, InventoryFilter.onAnyListedSource(apps, emptySet()))
    }

    private fun listedApp(pkg: String, label: String, source: RemoteReleasedSource, url: String? = null) =
        sampleApp(pkg, label, latestListings = listOf(UpdateLink(source, url = url, listed = true, known = true)))

    @Test
    fun sortsByLabelCaseInsensitive() {
        val beta = sampleApp("app.beta", "beta")
        val alpha = sampleApp("app.alpha", "Alpha")
        assertEquals(
            listOf(alpha, beta),
            InventoryFilter.sortedByLabel(listOf(beta, alpha)),
        )
    }
}

internal fun sampleApp(
    packageName: String,
    label: String,
    isSystemApp: Boolean = false,
    lastUpdateTimeMs: Long = 1L,
    installedAtMs: Long? = null,
    remoteReleasedAtMs: Long? = null,
    remoteReleasedSource: RemoteReleasedSource = RemoteReleasedSource.None,
    remoteVersionName: String? = null,
    latestListings: List<UpdateLink> = emptyList(),
): InstalledApp = InstalledApp(
    packageName = packageName,
    label = label,
    versionName = "1.0",
    versionCode = 1L,
    lastUpdateTimeMs = lastUpdateTimeMs,
    firstInstallTimeMs = lastUpdateTimeMs,
    minSdk = 26,
    targetSdk = 37,
    isSystemApp = isSystemApp,
    installedAtMs = installedAtMs,
    installedAtSource = if (installedAtMs == null) {
        InstalledDateSource.Unknown
    } else {
        InstalledDateSource.LastUpdate
    },
    remoteReleasedAtMs = remoteReleasedAtMs,
    remoteReleasedSource = remoteReleasedSource,
    remoteVersionName = remoteVersionName,
    latestListings = latestListings,
)
