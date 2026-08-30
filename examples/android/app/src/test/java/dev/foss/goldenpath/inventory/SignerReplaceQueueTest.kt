package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SignerReplaceQueueTest {
    @Before
    fun reset() {
        SignerReplaceQueue.clear()
        SignerReplaceStore.pending.value = null
        UpdateArtifactMemory.clear()
    }

    @Test
    fun rememberKeepsFirstAndDropRemoves() {
        val dir = File.createTempFile("signq", "dir").apply { delete(); mkdirs() }
        val first = File.createTempFile("first", ".apk")
        val second = File.createTempFile("second", ".apk")
        first.writeBytes(byteArrayOf(1, 2, 3, 4))
        second.writeBytes(byteArrayOf(5, 6, 7, 8))
        val job = UpdateAllJob("app.x", "X", RemoteReleasedSource.Fdroid, null, "2.0")
        assertTrue(SignerReplaceQueue.remember(dir, job, listOf(first)))
        assertTrue(SignerReplaceQueue.has("app.x"))
        val kept = SignerReplaceQueue.rows.single().files().first().readBytes()
        assertTrue(SignerReplaceQueue.remember(dir, job, listOf(second)))
        assertEquals(1, SignerReplaceQueue.rows.size)
        assertTrue(SignerReplaceQueue.rows.single().files().first().readBytes().contentEquals(kept))
        SignerReplaceQueue.hydrate(dir)
        assertEquals("app.x", SignerReplaceQueue.rows.single().packageName)
        SignerReplaceQueue.drop(dir, "app.x")
        assertFalse(SignerReplaceQueue.has("app.x"))
        assertTrue(SignerReplaceQueue.load(SignerReplaceQueue.file(dir)).isEmpty())
        first.delete()
        second.delete()
        dir.deleteRecursively()
    }

    @Test
    fun pickSkipsQueuedPackage() {
        val dir = File.createTempFile("pickq", "dir").apply { delete(); mkdirs() }
        val src = File.createTempFile("src", ".apk")
        src.writeBytes(byteArrayOf(1, 2, 3, 4))
        val job = UpdateAllJob("app.x", "X", RemoteReleasedSource.Fdroid, null, "2.0")
        assertTrue(SignerReplaceQueue.remember(dir, job, listOf(src)))
        UpdateArtifactMemory.add(
            UpdateArtifact("app.x", RemoteReleasedSource.Fdroid, "https://f-droid.org/repo/a.apk"),
        )
        assertEquals(0, UpdateAllPick.groups(listOf(sampleApp("app.x", "X", remoteVersionName = "2.0"))).size)
        src.delete()
        dir.deleteRecursively()
        UpdateArtifactMemory.clear()
    }
}
