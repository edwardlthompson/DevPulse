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

    @Test
    fun filesKeepsMatchingApkAndUnpacksZipWithoutIdentity() {
        val dir = File.createTempFile("keep", "dir").apply { delete(); mkdirs() }
        val apk = File(dir, "app.apk").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val artifact = UpdateArtifact("com.a", RemoteReleasedSource.ApkPure, "https://download.cdnpure.com/b/APK/com.a")
        val kept = ListingDownload.files(apk, artifact, { ApkInspect("com.a", emptySet()) }, dir)
        assertEquals(listOf(apk), kept)

        val archive = File(dir, "bundle.xapk")
        java.util.zip.ZipOutputStream(archive.outputStream()).use { zip ->
            zip.putNextEntry(java.util.zip.ZipEntry("com.a.apk"))
            zip.write(byteArrayOf(4, 5, 6))
            zip.closeEntry()
        }
        val unpacked = ListingDownload.files(
            archive,
            artifact,
            { file -> if (file.name == "com.a.apk") ApkInspect("com.a", emptySet()) else ApkInspect(null, emptySet()) },
            dir,
        )
        assertEquals(1, unpacked?.size)
        assertEquals("com.a.apk", unpacked?.first()?.name)
        assertTrue(!archive.exists())
    }
}
