package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateArtifactStoreTest {
    @Before
    fun reset() {
        UpdateArtifactMemory.clear()
    }

    @Test
    fun hydrateRoundTripsUrlAndWipeDeletesFile() {
        val dir = File.createTempFile("arts", "dir").apply { delete(); mkdirs() }
        UpdateArtifactStore.hydrate(dir)
        UpdateArtifactMemory.add(
            UpdateArtifact(
                "app.one",
                RemoteReleasedSource.Aptoide,
                "https://pool.apk.aptoide.com/apps/a.apk",
                "2.0",
                20L,
                nativeCodes = setOf("x86"),
            ),
        )
        val stored = File(dir, UpdateArtifactStore.FILE)
        assertTrue(stored.isFile)
        UpdateArtifactMemory.clear()
        UpdateArtifactStore.hydrate(dir)
        val restored = UpdateArtifactMemory.forSource("app.one", RemoteReleasedSource.Aptoide)
        assertEquals("https://pool.apk.aptoide.com/apps/a.apk", restored?.downloadUrl)
        assertEquals("2.0", restored?.versionName)
        assertEquals(20L, restored?.versionCode)
        assertEquals(setOf("x86"), restored?.nativeCodes)
        CacheWipe.remotes(dir)
        assertTrue(!stored.exists())
        assertEquals(null, UpdateArtifactMemory.forSource("app.one", RemoteReleasedSource.Aptoide))
        dir.deleteRecursively()
    }
}
