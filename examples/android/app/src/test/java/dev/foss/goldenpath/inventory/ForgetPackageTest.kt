package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ForgetPackageTest {
    @Before
    fun reset() {
        RemoteReleaseMemory.clear()
        IgnoredUpdates.clear()
        AppliedUpdates.clear()
    }

    @Test
    fun wipeDropsListingsAndIgnore() {
        val dir = File.createTempFile("forget", "dir").apply { delete(); mkdirs() }
        RemoteReleaseMemory.putAll(
            mapOf("app.x" to RemoteReleasePick(1L, RemoteReleasedSource.Play, "2.0")),
        )
        IgnoredUpdates.add("app.x", RemoteReleasedSource.Play, "2.0", dir)
        AppliedUpdates.settle("app.x")
        ForgetPackage.wipe("app.x", dir)
        assertTrue(RemoteReleaseMemory.byPackage["app.x"] == null)
        assertFalse(IgnoredUpdates.has("app.x", RemoteReleasedSource.Play, "2.0"))
        assertFalse(AppliedUpdates.settled("app.x"))
    }
}
