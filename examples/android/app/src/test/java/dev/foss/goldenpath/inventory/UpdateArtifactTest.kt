package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateArtifactTest {
    @Before
    fun reset() {
        UpdateArtifactMemory.clear()
    }

    @Test
    fun prefersFdroidOverApkPure() {
        UpdateArtifactMemory.add(UpdateArtifact("app.one", RemoteReleasedSource.ApkPure, "https://d.apkpure.com/b/APK/app.one"))
        UpdateArtifactMemory.add(UpdateArtifact("app.one", RemoteReleasedSource.Fdroid, "https://f-droid.org/repo/app.one_1.apk"))
        assertEquals(RemoteReleasedSource.Fdroid, UpdateArtifactMemory.best("app.one")?.source)
    }

    @Test
    fun storeWritesFetchedBytes() {
        val dir = File.createTempFile("apk", "dir").apply { delete(); mkdirs() }
        val artifact = UpdateArtifact("app.one", RemoteReleasedSource.Fdroid, "https://f-droid.org/repo/app.one_1.apk", versionCode = 1)
        val file = ApkFileStore.save(dir, artifact) { Result.success(byteArrayOf(1, 2, 3)) }.getOrThrow()
        assertTrue(file.isFile)
        assertEquals(3, file.length())
        assertEquals("app.one-1.apk", file.name)
    }
}
