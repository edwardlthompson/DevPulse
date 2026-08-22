package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ListingDownloadTest {
    @Before
    fun reset() {
        UpdateArtifactMemory.clear()
    }

    @Test
    fun writesWhenPackageMatchesEvenIfSignerDiffers() {
        val dir = File.createTempFile("listing", "dir").apply { delete(); mkdirs() }
        val artifact = UpdateArtifact(
            "com.trandllstudio.jcp",
            RemoteReleasedSource.Play,
            "https://redirector.gvt1.com/edgedl/android/market/jcp",
        )
        val file = ListingDownload.write(dir, artifact, byteArrayOf(1, 2, 3)) {
            ApkInspect("com.trandllstudio.jcp", setOf("play-cert"))
        }
        assertTrue(file != null && file.isFile)
        assertEquals(artifact.downloadUrl, UpdateArtifactMemory.forSource("com.trandllstudio.jcp", RemoteReleasedSource.Play)?.downloadUrl)
    }

    @Test
    fun rejectsOtherPackageAndEmptyBytes() {
        val dir = File.createTempFile("listing", "dir").apply { delete(); mkdirs() }
        val artifact = UpdateArtifact("com.a", RemoteReleasedSource.Play, "https://redirector.gvt1.com/edgedl/android/market/a")
        assertNull(
            ListingDownload.write(dir, artifact, byteArrayOf(1)) { ApkInspect("com.b", setOf("aa")) },
        )
        assertNull(
            ListingDownload.write(dir, artifact, byteArrayOf()) { ApkInspect("com.a", setOf("aa")) },
        )
    }
}
