package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemoteReleaseTest {
    @Test
    fun newestSuccessfulWins() {
        val pick = RemoteRelease.pick(
            RemoteDate(100L, RemoteReleasedSource.Fdroid),
            RemoteDate(300L, RemoteReleasedSource.Aptoide),
            RemoteDate(200L, RemoteReleasedSource.Forge),
        )
        assertEquals(300L, pick.ms)
        assertEquals(RemoteReleasedSource.Aptoide, pick.source)
    }

    @Test
    fun emptyIsNone() {
        val pick = RemoteRelease.pick(null, null)
        assertNull(pick.ms)
        assertEquals(RemoteReleasedSource.None, pick.source)
    }
}
