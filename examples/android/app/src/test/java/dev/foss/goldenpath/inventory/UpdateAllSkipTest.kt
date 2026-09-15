package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateAllSkipTest {
    @Test
    fun playOlderDropsApkPureAndMirror() {
        val group = mutableListOf(
            UpdateAllJob("app.a", "A", RemoteReleasedSource.Play, null, "1"),
            UpdateAllJob("app.a", "A", RemoteReleasedSource.ApkPure, null, "1"),
            UpdateAllJob("app.a", "A", RemoteReleasedSource.ApkMirror, null, "1"),
            UpdateAllJob("app.a", "A", RemoteReleasedSource.Fdroid, null, "2"),
        )
        UpdateAllSkip.dropSideloadAfterPlayOlder(group, InstallWhy.Older, RemoteReleasedSource.Play)
        assertEquals(
            listOf(RemoteReleasedSource.Play, RemoteReleasedSource.Fdroid),
            group.map { it.source },
        )
    }

    @Test
    fun sdkDoesNotDropApkPure() {
        val group = mutableListOf(
            UpdateAllJob("app.a", "A", RemoteReleasedSource.Aptoide, null, "1"),
            UpdateAllJob("app.a", "A", RemoteReleasedSource.ApkPure, null, "1"),
        )
        UpdateAllSkip.dropSideloadAfterPlayOlder(group, InstallWhy.Sdk, RemoteReleasedSource.Aptoide)
        assertEquals(2, group.size)
    }

    @Test
    fun stopFallbacksClearsTheGroup() {
        val group = mutableListOf(
            UpdateAllJob("app.a", "A", RemoteReleasedSource.Play, null, "1"),
            UpdateAllJob("app.a", "A", RemoteReleasedSource.Fdroid, null, "1"),
        )
        UpdateAllSkip.stopFallbacks(group)
        assertTrue(group.isEmpty())
    }

    @Test
    fun dlLineSkipsOlderAndSdk() {
        assertEquals("ok", UpdateAllSkip.dlLine(true, InstallWhy.Older))
        assertEquals("skip Older", UpdateAllSkip.dlLine(false, InstallWhy.Older))
        assertEquals("skip Sdk", UpdateAllSkip.dlLine(false, InstallWhy.Sdk))
        assertEquals("fail NoFile", UpdateAllSkip.dlLine(false, InstallWhy.NoFile))
        assertFalse(UpdateAllSkip.dlLine(false, InstallWhy.Timeout).startsWith("skip"))
    }

    @Test
    fun playCurrentBlocksApkPureMirror() {
        val current = sampleApp(
            "com.google.android.keep",
            "Keep",
            latestListings = listOf(
                UpdateLink(RemoteReleasedSource.Play, versionName = "5.26.361", listed = true, versionCode = 1),
            ),
        ).copy(versionName = "5.26.361", versionCode = 1)
        assertFalse(UpdateAllSkip.allowSideloadMirror(current, RemoteReleasedSource.ApkPure))
        val behind = current.copy(versionName = "5.26.360", versionCode = 0)
        assertTrue(UpdateAllSkip.allowSideloadMirror(behind, RemoteReleasedSource.ApkPure))
        assertTrue(UpdateAllSkip.allowSideloadMirror(current, RemoteReleasedSource.Fdroid))
    }
}
