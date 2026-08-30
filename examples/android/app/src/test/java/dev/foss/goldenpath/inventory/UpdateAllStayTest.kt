package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateAllStayTest {
    @Before
    fun reset() {
        IgnoredUpdates.clear()
        AppliedUpdates.clear()
        SignerReplaceQueue.clear()
        UpdateAllCancel.arm()
    }

    @Test
    fun successHidesAppAndLastFailHidesWhenNoRetry() {
        val snaps = mutableListOf<UpdateAllSnap>()
        UpdateAll.run(
            jobs = listOf(UpdateAllJob("app.ok", "Ok", RemoteReleasedSource.Play, null, "2.0")),
            prepare = { _, _ -> listOf(File.createTempFile("okapk", ".apk")) },
            install = { true },
            onSnap = { snaps += it },
        )
        assertFalse(snaps.last { it.phase == UpdateAllPhase.Ok }.stay)
        assertTrue(AppliedUpdates.settled("app.ok"))
        snaps.clear()
        UpdateAll.run(
            jobs = listOf(UpdateAllJob("app.no", "No", RemoteReleasedSource.Play, null, "2.0")),
            prepare = { _, _ -> null },
            install = { false },
            onSnap = { snaps += it },
        )
        assertFalse(snaps.last { it.phase == UpdateAllPhase.Fail }.stay)
        assertFalse(IgnoredUpdates.has("app.no", RemoteReleasedSource.Play, "2.0"))
    }

    @Test
    fun noFileWritesLogAndStaysRetryable() {
        val dir = File.createTempFile("ualog", "dir").apply { delete(); mkdirs() }
        UpdateAll.run(
            jobs = listOf(UpdateAllJob("app.no", "No", RemoteReleasedSource.Play, null, "2.0")),
            prepare = { _, _ -> null },
            install = { false },
            filesDir = dir,
        )
        val row = UpdateAllLog.load(UpdateAllLog.file(dir)).single()
        assertEquals("failDl", row.result)
        assertEquals("PlayStore", row.why)
        assertTrue(IgnoredUpdates.has("app.no", RemoteReleasedSource.Play, "2.0"))
    }

    @Test
    fun failedNewestKeepsRowUntilRetrySucceeds() {
        val play = UpdateAllJob("app.a", "A", RemoteReleasedSource.Play, null, "3.0")
        val fdroid = UpdateAllJob("app.a", "A", RemoteReleasedSource.Fdroid, null, "2.0")
        val snaps = mutableListOf<UpdateAllSnap>()
        UpdateAll.run(
            jobs = listOf(play),
            groups = listOf(listOf(play, fdroid)),
            prepare = { job, _ ->
                if (job.source == RemoteReleasedSource.Play) null else listOf(File.createTempFile("okapk", ".apk"))
            },
            install = { true },
            onSnap = { snaps += it },
        )
        assertTrue(snaps.first { it.phase == UpdateAllPhase.Fail }.stay)
        assertFalse(snaps.last { it.phase == UpdateAllPhase.Ok }.stay)
    }

    @Test
    fun signingClashIsKeptForLaterAndNotIgnored() {
        val dir = File.createTempFile("uasign", "dir").apply { delete(); mkdirs() }
        val apk = File.createTempFile("clash", ".apk")
        apk.writeBytes(byteArrayOf(1, 2, 3, 4))
        val job = UpdateAllJob("app.sign", "Sign", RemoteReleasedSource.Fdroid, null, "2.0")
        val snaps = mutableListOf<UpdateAllSnap>()
        var installed = 0
        UpdateAll.run(
            jobs = listOf(job),
            prepare = { _, _ -> listOf(apk) },
            install = { installed += 1; true },
            clash = { _, _ -> true },
            filesDir = dir,
            onSnap = { snaps += it },
        )
        assertEquals(0, installed)
        assertEquals(InstallWhy.Signing, snaps.last { it.phase == UpdateAllPhase.Fail }.failWhy)
        assertFalse(IgnoredUpdates.has("app.sign", RemoteReleasedSource.Fdroid, "2.0"))
        assertTrue(SignerReplaceQueue.has("app.sign"))
        apk.delete()
        dir.deleteRecursively()
    }

    @Test
    fun laterMatchingSourceDropsSigningHold() {
        val dir = File.createTempFile("uafall", "dir").apply { delete(); mkdirs() }
        val play = UpdateAllJob("app.a", "A", RemoteReleasedSource.Play, null, "3.0")
        val fdroid = UpdateAllJob("app.a", "A", RemoteReleasedSource.Fdroid, null, "2.0")
        val playApk = File.createTempFile("play", ".apk").also { it.writeBytes(byteArrayOf(1, 2, 3, 4)) }
        val fdroidApk = File.createTempFile("fdroid", ".apk").also { it.writeBytes(byteArrayOf(5, 6, 7, 8)) }
        UpdateAll.run(
            jobs = listOf(play),
            groups = listOf(listOf(play, fdroid)),
            prepare = { job, _ -> if (job.source == RemoteReleasedSource.Play) listOf(playApk) else listOf(fdroidApk) },
            install = { files -> files.first().name.startsWith("fdroid") },
            clash = { job, _ -> job.source == RemoteReleasedSource.Play },
            filesDir = dir,
        )
        assertFalse(SignerReplaceQueue.has("app.a"))
        assertTrue(AppliedUpdates.settled("app.a"))
        playApk.delete()
        fdroidApk.delete()
        dir.deleteRecursively()
    }
}
