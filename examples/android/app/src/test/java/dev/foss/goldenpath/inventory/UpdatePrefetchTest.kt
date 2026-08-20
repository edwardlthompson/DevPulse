package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdatePrefetchTest {
    private val artifact = UpdateArtifact(
        "app.one",
        RemoteReleasedSource.Fdroid,
        "https://f-droid.org/repo/app.one_1.apk",
        sha256 = "039058c6f2c0cb492c533b0a4d14ef77cc0f78abccced5287d84a1a2011cfb81",
    )
    private val inspect = ApkInspect("app.one", setOf("aa"))
    private val installed = InstalledIdentity("app.one", setOf("aa"), setOf("arm64-v8a"))

    @Before
    fun reset() {
        UpdateArtifactMemory.clear()
    }

    @Test
    fun skipsWhenOffOrMetered() {
        assertTrue(UpdatePrefetch.candidates(false, true, listOf(artifact)) { installed }.isEmpty())
        assertTrue(UpdatePrefetch.candidates(true, false, listOf(artifact)) { installed }.isEmpty())
    }

    @Test
    fun skipsPlayAndWrongAbi() {
        val play = artifact.copy(source = RemoteReleasedSource.Play)
        val arm = artifact.copy(nativeCodes = setOf("armeabi-v7a"))
        assertTrue(UpdatePrefetch.candidates(true, true, listOf(play)) { installed }.isEmpty())
        assertTrue(UpdatePrefetch.candidates(true, true, listOf(arm)) { installed }.isEmpty())
    }

    @Test
    fun stagesOnlyWhenHashAndIdentityMatch() {
        val dir = File.createTempFile("pref", "dir").apply { delete(); mkdirs() }
        val ready = UpdatePrefetch.run(
            enabled = true,
            unmetered = true,
            cacheDir = dir,
            artifacts = listOf(artifact),
            fetch = { Result.success(byteArrayOf(1, 2, 3)) },
            inspect = { inspect },
            installed = { installed },
        )
        assertEquals(1, ready)
        assertEquals(dir.listFiles()?.single()?.absolutePath, UpdateArtifactMemory.best("app.one")?.localPath)
        val badHash = UpdatePrefetch.run(
            true,
            true,
            dir,
            listOf(artifact.copy(packageName = "app.two", sha256 = "00")),
            fetch = { Result.success(byteArrayOf(1, 2, 3)) },
            inspect = { inspect.copy(packageName = "app.two") },
            installed = { installed.copy(packageName = "app.two") },
        )
        assertEquals(0, badHash)
        assertFalse(UpdateArtifactMemory.best("app.two")?.localPath != null)
    }
}
