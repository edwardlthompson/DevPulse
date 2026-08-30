package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import java.io.File
import org.junit.Before
import org.junit.Test

class UpdateAllLogTest {
    @Before
    fun reset() {
        IgnoredUpdates.clear()
    }
    @Test
    fun roundTripsAndKeepsNewestRows() {
        val dir = File.createTempFile("ualog", "dir").apply { delete(); mkdirs() }
        val job = UpdateAllJob("app.x", "X", RemoteReleasedSource.Play, null, "2.0")
        UpdateAllLog.note(dir, job, "failDl", "NoFile", atMs = 10L)
        UpdateAllLog.note(dir, job.copy(packageName = "app.y", label = "Y"), "ok", "", atMs = 20L)
        val rows = UpdateAllLog.load(UpdateAllLog.file(dir))
        assertEquals(2, rows.size)
        assertEquals("app.y", rows.last().packageName)
        assertEquals("failDl", rows.first().result)
        assertEquals("NoFile", rows.first().why)
        assertTrue(UpdateAllLog.parse("not-a-row") == null)
    }

    @Test
    fun retryDownloadsDropsIgnoreAndLogRow() {
        val dir = File.createTempFile("ualog", "dir").apply { delete(); mkdirs() }
        IgnoredUpdates.clear()
        val job = UpdateAllJob("app.x", "X", RemoteReleasedSource.Play, null, "2.0")
        UpdateAllLog.note(dir, job, "failDl", "NoFile", atMs = 10L)
        UpdateAllLog.note(dir, job.copy(packageName = "app.y"), "failIns", "Permission", atMs = 11L)
        IgnoredUpdates.add("app.x", RemoteReleasedSource.Play, "2.0", dir)
        assertEquals(1, UpdateAllRetry.downloads(dir))
        assertFalse(IgnoredUpdates.has("app.x", RemoteReleasedSource.Play, "2.0"))
        val left = UpdateAllLog.load(UpdateAllLog.file(dir))
        assertEquals(1, left.size)
        assertEquals("failIns", left.single().result)
        assertEquals(1, UpdateAllRetry.installs(dir))
        assertTrue(UpdateAllLog.load(UpdateAllLog.file(dir)).isEmpty())
        IgnoredUpdates.add("app.z", RemoteReleasedSource.Fdroid, "1.0", dir)
        assertEquals(1, UpdateAllRetry.ignored(dir))
        assertFalse(IgnoredUpdates.has("app.z", RemoteReleasedSource.Fdroid, "1.0"))
    }
}
