package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DownloadLaunchTest {
    @Before
    fun reset() {
        UpdateArtifactMemory.clear()
    }

    @Test
    fun playStaysPageOnly() {
        assertEquals("no file url", DownloadLaunch.reason(null))
        val play = UpdateArtifact("app.one", RemoteReleasedSource.Play, "https://play.google.com/store/apps/details?id=app.one")
        assertEquals("page only", DownloadLaunch.reason(play))
    }

    @Test
    fun fdroidIsDirect() {
        UpdateArtifactMemory.add(
            UpdateArtifact("app.one", RemoteReleasedSource.Fdroid, "https://f-droid.org/repo/app.one_1.apk"),
        )
        assertEquals("direct", DownloadLaunch.reason(UpdateArtifactMemory.best("app.one")))
    }
}
