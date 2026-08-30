package dev.foss.goldenpath.inventory

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppliedUpdatesTest {
    @Before
    fun reset() {
        AppliedUpdates.clear()
        IgnoredUpdates.clear()
    }

    @Test
    fun successHidesUntilRefreshClears() {
        val app = listed("app.x", RemoteReleasedSource.Play, "3.0")
        assertTrue(UpdateInventory.hasUpdate(app))
        AppliedUpdates.settle("app.x")
        assertFalse(UpdateInventory.hasUpdate(app))
        AppliedUpdates.clear()
        assertTrue(UpdateInventory.hasUpdate(app))
    }

    @Test
    fun filenameAndVersionCodeListingsAreNotUpdates() {
        assertFalse(
            UpdateInventory.hasUpdate(
                listed("eu.faircode.email", RemoteReleasedSource.Forge, "FairEmail-v1.2332a-large-release.apk")
                    .copy(versionName = "1.2332", versionCode = 2332L),
            ),
        )
        assertFalse(
            UpdateInventory.hasUpdate(
                listed("net.kollnig.missioncontrol", RemoteReleasedSource.Fdroid, "2026080501")
                    .copy(versionName = "2026.08.05", versionCode = 2026080501L),
            ),
        )
        assertFalse(
            UpdateInventory.hasUpdate(
                listed(
                    "com.akylas.documentscanner",
                    RemoteReleasedSource.Forge,
                    "com.akylas.documentscanner/android/github/1.25.0/160",
                ).copy(versionName = "1.25.0.160", versionCode = 160L),
            ),
        )
    }

    @Test
    fun persistSurvivesClearAndHydrate() {
        val dir = java.io.File.createTempFile("applied", "dir").apply { delete(); mkdirs() }
        AppliedUpdates.settle("app.x", "2.0", 20L, dir)
        AppliedUpdates.clear()
        assertFalse(AppliedUpdates.settled("app.x"))
        AppliedUpdates.hydrate(dir)
        assertTrue(AppliedUpdates.settled("app.x"))
        val same = listed("app.x", RemoteReleasedSource.Play, "2.0").copy(versionName = "2.0", versionCode = 20L)
        assertTrue(AppliedUpdates.hides(same))
        val newer = listed("app.x", RemoteReleasedSource.Play, "3.0").copy(versionName = "2.0", versionCode = 20L)
        assertFalse(AppliedUpdates.hides(newer))
        assertTrue(UpdateInventory.hasUpdate(newer))
    }

    @Test
    fun apkMirrorOnlyIsNotAnUpdate() {
        val app = listed("app.m", RemoteReleasedSource.ApkMirror, "9.0")
        assertFalse(UpdateInventory.hasUpdate(app))
        assertTrue(UpdateInventory.usable(app).isEmpty())
    }

    @Test
    fun ignoredRemoteVersionDropsFallback() {
        val app = InstalledApp(
            packageName = "app.r",
            label = "R",
            versionName = "1.0",
            versionCode = 1L,
            lastUpdateTimeMs = 1L,
            firstInstallTimeMs = 1L,
            minSdk = 26,
            targetSdk = 37,
            isSystemApp = false,
            remoteVersionName = "2.0",
            remoteVersionSource = RemoteReleasedSource.Play,
        )
        assertTrue(UpdateInventory.hasUpdate(app))
        IgnoredUpdates.add("app.r", RemoteReleasedSource.Play, "2.0")
        assertFalse(UpdateInventory.hasUpdate(app))
    }

    private fun listed(pkg: String, source: RemoteReleasedSource, version: String) = InstalledApp(
        packageName = pkg,
        label = pkg,
        versionName = "1.0",
        versionCode = 1L,
        lastUpdateTimeMs = 1L,
        firstInstallTimeMs = 1L,
        minSdk = 26,
        targetSdk = 37,
        isSystemApp = false,
        remoteVersionName = version,
        latestListings = listOf(UpdateLink(source, versionName = version, listed = true)),
    )
}
