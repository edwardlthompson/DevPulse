package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SignerReplaceStoreTest {
    @Test
    fun parseRejectsShortOrBlankRows() {
        assertNull(SignerReplaceStore.parse(null))
        assertNull(SignerReplaceStore.parse("app.x\tLabel\tFdroid"))
        assertNull(SignerReplaceStore.parse("\tLabel\tFdroid\t/tmp/a.apk"))
    }

    @Test
    fun stageCopySurvivesAndClearRemovesIt() {
        val dir = File.createTempFile("hold", "dir").apply { delete(); mkdirs() }
        val src = File.createTempFile("src", ".apk")
        src.writeBytes(byteArrayOf(9, 8, 7, 6))
        assertTrue(SignerReplaceStore.capture(dir, "app.x", "X", RemoteReleasedSource.Fdroid, listOf(src)))
        val hold = SignerReplaceStore.load(dir)
        assertEquals("app.x", hold?.packageName)
        assertTrue(SignerClash.filesReady(hold!!.files()))
        assertFalse(hold.files().first().absolutePath == src.absolutePath)
        SignerReplaceStore.clear(dir)
        assertNull(SignerReplaceStore.load(dir))
        assertFalse(hold.files().first().isFile)
        src.delete()
        dir.deleteRecursively()
    }

    @Test
    fun installableWhenPackageGone() {
        val dir = File.createTempFile("gone", "dir").apply { delete(); mkdirs() }
        val src = File.createTempFile("src", ".apk")
        src.writeBytes(byteArrayOf(1, 2, 3, 4))
        SignerReplaceQueue.clear()
        SignerReplaceStore.clear(dir)
        assertTrue(SignerReplaceStore.capture(dir, "app.x", "X", RemoteReleasedSource.Fdroid, listOf(src)))
        assertEquals("app.x", SignerReplaceStore.installable(dir) { false }?.packageName)
        assertNull(SignerReplaceStore.installable(dir) { true })
        SignerReplaceStore.clear(dir)
        src.delete()
        dir.deleteRecursively()
    }

    @Test
    fun clearDoesNotDeleteAnotherPackage() {
        val dir = File.createTempFile("two", "dir").apply { delete(); mkdirs() }
        val srcA = File.createTempFile("apkA", ".apk")
        val srcB = File.createTempFile("apkB", ".apk")
        srcA.writeBytes(byteArrayOf(1, 2, 3, 4))
        srcB.writeBytes(byteArrayOf(5, 6, 7, 8))
        SignerReplaceQueue.clear()
        assertTrue(SignerReplaceStore.capture(dir, "app.a", "A", RemoteReleasedSource.Fdroid, listOf(srcA)))
        val fileA = SignerReplaceStore.load(dir)!!.files().first()
        val stagedB = SignerReplaceStore.stage(dir, "app.b", listOf(srcB))!!
        SignerReplaceStore.save(
            dir,
            SignerReplaceHold("app.b", "B", RemoteReleasedSource.Forge, stagedB.map { it.absolutePath }),
        )
        SignerReplaceStore.clear(dir, packageName = "app.a")
        assertFalse(fileA.isFile)
        assertTrue(stagedB.first().isFile)
        assertEquals("app.b", SignerReplaceStore.load(dir)?.packageName)
        SignerReplaceStore.clear(dir, packageName = "app.b")
        srcA.delete()
        srcB.delete()
        dir.deleteRecursively()
    }

    @Test
    fun clearLeavesOtherQueuedFiles() {
        val dir = File.createTempFile("qtwo", "dir").apply { delete(); mkdirs() }
        val srcA = File.createTempFile("qapkA", ".apk")
        val srcB = File.createTempFile("qapkB", ".apk")
        srcA.writeBytes(byteArrayOf(1, 2, 3, 4))
        srcB.writeBytes(byteArrayOf(5, 6, 7, 8))
        SignerReplaceQueue.clear()
        SignerReplaceStore.pending.value = null
        val jobA = UpdateAllJob("app.a", "A", RemoteReleasedSource.Fdroid, null, "2.0")
        val jobB = UpdateAllJob("app.b", "B", RemoteReleasedSource.Forge, null, "2.0")
        assertTrue(SignerReplaceQueue.remember(dir, jobA, listOf(srcA)))
        assertTrue(SignerReplaceQueue.remember(dir, jobB, listOf(srcB)))
        val fileB = SignerReplaceQueue.rows.single { it.packageName == "app.b" }.files().first()
        SignerReplaceStore.save(dir, SignerReplaceQueue.rows.first { it.packageName == "app.a" })
        SignerReplaceStore.clear(dir, packageName = "app.a")
        assertTrue(fileB.isFile)
        assertTrue(SignerReplaceQueue.has("app.b"))
        assertFalse(SignerReplaceQueue.has("app.a"))
        SignerReplaceQueue.clear()
        srcA.delete()
        srcB.delete()
        dir.deleteRecursively()
    }
}
