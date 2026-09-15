package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.aurora.AuroraPlayFile
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ListingPlayTest {
    @Test
    fun writesBaseAndSplit() {
        val dir = File.createTempFile("play", "dir").apply { delete(); mkdirs() }
        val files = ListingPlay.write(
            dir,
            "com.trandllstudio.jcp",
            listOf(
                AuroraPlayFile("https://redirector.gvt1.com/edgedl/android/market/base", "3.0.7", 31, true),
                AuroraPlayFile("https://redirector.gvt1.com/edgedl/android/market/split", "3.0.7", 31, false),
            ),
            bytesFor = { url -> if (url.contains("base")) byteArrayOf(1) else byteArrayOf(2) },
            inspect = { ApkInspect("com.trandllstudio.jcp", setOf("play")) },
        )
        assertEquals(2, files?.size)
        assertEquals(setOf("1", "2"), files?.map { it.readBytes().first().toString() }?.toSet())
    }

    @Test
    fun skipsOtherPackageAndKeepsMatch() {
        val dir = File.createTempFile("play", "dir").apply { delete(); mkdirs() }
        val files = ListingPlay.write(
            dir,
            "com.a",
            listOf(
                AuroraPlayFile("https://redirector.gvt1.com/edgedl/android/market/skip", base = false),
                AuroraPlayFile("https://redirector.gvt1.com/edgedl/android/market/base", base = true),
            ),
            bytesFor = { url ->
                if (url.contains("skip")) byteArrayOf(1) else byteArrayOf(0x50, 0x4B, 3)
            },
            inspect = { file ->
                if (file.name.contains("-1.apk")) ApkInspect("com.a", setOf("a")) else ApkInspect("com.other", setOf("x"))
            },
        )
        assertEquals(1, files?.size)
    }

    @Test
    fun keepsZipWhenInspectHasNoPackage() {
        val dir = File.createTempFile("play", "dir").apply { delete(); mkdirs() }
        val files = ListingPlay.write(
            dir,
            "com.a",
            listOf(AuroraPlayFile("https://redirector.gvt1.com/edgedl/android/market/split", base = false)),
            bytesFor = { zipProbe() },
            inspect = { ApkInspect(null, emptySet()) },
        )
        assertEquals(1, files?.size)
    }

    @Test
    fun rejectsMissingPart() {
        val dir = File.createTempFile("play", "dir").apply { delete(); mkdirs() }
        assertNull(
            ListingPlay.write(
                dir,
                "com.a",
                listOf(AuroraPlayFile("https://redirector.gvt1.com/edgedl/android/market/base", base = true)),
                bytesFor = { null },
                inspect = { ApkInspect("com.a", setOf("a")) },
            ),
        )
    }

    @Test
    fun abortsWhenAPartFetchFails() {
        val dir = File.createTempFile("play", "dir").apply { delete(); mkdirs() }
        val files = ListingPlay.write(
            dir,
            "com.a",
            listOf(
                AuroraPlayFile("https://redirector.gvt1.com/edgedl/android/market/base", base = true),
                AuroraPlayFile("https://redirector.gvt1.com/edgedl/android/market/split", base = false),
            ),
            bytesFor = { url -> if (url.contains("base")) byteArrayOf(0x50, 0x4B, 3) else null },
            inspect = { ApkInspect("com.a", setOf("a")) },
        )
        assertNull(files)
        assertEquals(0, dir.listFiles()?.size ?: 0)
    }

    @Test
    fun refusesPlayFilesOlderThanInstalled() {
        val parts = listOf(
            AuroraPlayFile("https://redirector.gvt1.com/edgedl/android/market/base", "18.1.3", 175963030L, true),
        )
        assertFalse(ListingPlay.newerThanInstalled(parts, "18.2.4", 175981782L))
        assertTrue(ListingPlay.newerThanInstalled(parts, "18.1.0", 175900000L))
    }
}

private fun zipProbe(): ByteArray {
    val bytes = ByteArray(40)
    bytes[0] = 0x50
    bytes[1] = 0x4B
    bytes[2] = 3
    bytes[3] = 4
    bytes[18] = 0x50
    bytes[19] = 0x4B
    bytes[20] = 5
    bytes[21] = 6
    return bytes
}
