package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IgnoredUpdatesTest {
    @Before
    fun reset() {
        IgnoredUpdates.clear()
        AppliedUpdates.clear()
    }

    @Test
    fun remembersSourceAndVersionOnly() {
        IgnoredUpdates.add("app.x", RemoteReleasedSource.ApkPure, "3.1")
        assertTrue(IgnoredUpdates.has("app.x", RemoteReleasedSource.ApkPure, "3.1"))
        assertFalse(IgnoredUpdates.has("app.x", RemoteReleasedSource.Play, "3.1"))
        assertFalse(IgnoredUpdates.has("app.x", RemoteReleasedSource.ApkPure, "3.2"))
        assertFalse(IgnoredUpdates.has("app.y", RemoteReleasedSource.ApkPure, "3.1"))
    }

    @Test
    fun dropsLegacyThreeColumnRows() {
        assertEquals(null, IgnoredUpdates.parse("app.x\tPlay\t2.0"))
        assertTrue(IgnoredUpdates.parse("app.x\tPlay\t2.0\tkeep") != null)
    }

    @Test
    fun skipsBlankVersionAndReloadsFile() {
        IgnoredUpdates.add("app.x", RemoteReleasedSource.Play, "  ")
        assertFalse(IgnoredUpdates.has("app.x", RemoteReleasedSource.Play, "1.0"))
        val dir = File.createTempFile("ignored", "dir").apply { delete(); mkdirs() }
        IgnoredUpdates.add("app.x", RemoteReleasedSource.Fdroid, "2.0", dir)
        IgnoredUpdates.clear()
        IgnoredUpdates.hydrate(dir)
        assertTrue(IgnoredUpdates.has("app.x", RemoteReleasedSource.Fdroid, "2.0"))
    }

    @Test
    fun hasUpdateDropsWhenEveryNewerListingIsIgnored() {
        val app = InstalledApp(
            packageName = "app.x",
            label = "X",
            versionName = "1.0",
            versionCode = 1L,
            lastUpdateTimeMs = 1L,
            firstInstallTimeMs = 1L,
            minSdk = 26,
            targetSdk = 37,
            isSystemApp = false,
            remoteVersionName = "3.0",
            latestListings = listOf(
                UpdateLink(RemoteReleasedSource.Play, versionName = "3.0", listed = true),
                UpdateLink(RemoteReleasedSource.Fdroid, versionName = "2.0", listed = true),
            ),
        )
        assertTrue(UpdateInventory.hasUpdate(app))
        IgnoredUpdates.add("app.x", RemoteReleasedSource.Play, "3.0")
        assertTrue(UpdateInventory.hasUpdate(app))
        IgnoredUpdates.add("app.x", RemoteReleasedSource.Fdroid, "2.0")
        assertFalse(UpdateInventory.hasUpdate(app))
        IgnoredUpdates.add("app.x", RemoteReleasedSource.Play, "3.1")
        val newer = app.copy(
            latestListings = app.latestListings + UpdateLink(
                RemoteReleasedSource.ApkPure,
                versionName = "3.2",
                listed = true,
            ),
        )
        assertTrue(UpdateInventory.hasUpdate(newer))
    }

    @Test
    fun removeSpecificIgnoredEntry() {
        val dir = File.createTempFile("ignored", "dir").apply { delete(); mkdirs() }
        IgnoredUpdates.add("app.x", RemoteReleasedSource.Forge, "v1.6.14", dir)
        IgnoredUpdates.add("app.x", RemoteReleasedSource.Play, "2.0", dir)
        assertTrue(IgnoredUpdates.has("app.x", RemoteReleasedSource.Forge, "v1.6.14"))
        assertTrue(IgnoredUpdates.has("app.x", RemoteReleasedSource.Play, "2.0"))

        IgnoredUpdates.remove("app.x", RemoteReleasedSource.Forge, "v1.6.14", dir)
        assertFalse(IgnoredUpdates.has("app.x", RemoteReleasedSource.Forge, "v1.6.14"))
        assertTrue(IgnoredUpdates.has("app.x", RemoteReleasedSource.Play, "2.0"))

        IgnoredUpdates.clear()
        IgnoredUpdates.hydrate(dir)
        assertFalse(IgnoredUpdates.has("app.x", RemoteReleasedSource.Forge, "v1.6.14"))
        assertTrue(IgnoredUpdates.has("app.x", RemoteReleasedSource.Play, "2.0"))
    }
}
