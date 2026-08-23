package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IgnoreBackupTest {
    @Before
    fun reset() {
        IgnoredUpdates.clear()
    }

    @Test
    fun restoreReloadsIgnoredRows() {
        val dir = File.createTempFile("bak", "dir").apply { delete(); mkdirs() }
        IgnoredUpdates.add("app.x", RemoteReleasedSource.Play, "2.0", dir)
        val raw = IgnoreBackup.export(dir)
        IgnoredUpdates.clear()
        IgnoreBackup.restore(dir, raw)
        assertTrue(IgnoredUpdates.has("app.x", RemoteReleasedSource.Play, "2.0"))
    }
}
