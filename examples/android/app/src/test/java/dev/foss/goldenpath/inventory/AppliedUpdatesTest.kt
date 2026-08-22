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
