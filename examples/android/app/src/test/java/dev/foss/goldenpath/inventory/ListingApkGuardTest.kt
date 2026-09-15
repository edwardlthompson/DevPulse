package dev.foss.goldenpath.inventory

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ListingApkGuardTest {
    @Test
    fun zipAbisAndOlderOrWrongAbiAreDropped() {
        val dir = File.createTempFile("guard", "dir").apply { delete(); mkdirs() }
        val x86 = zipWithLib(File(dir, "x86.apk"), "x86_64")
        assertEquals(setOf("x86_64"), ApkNativeZip.abis(x86))
        assertNull(
            ListingApkGuard.keep(listOf(x86), 1L, setOf("arm64-v8a"), { 20L }),
        )
        assertEquals(InstallWhy.Sdk, ListingFail.why)
        assertTrue(!x86.exists())

        val arm = zipWithLib(File(dir, "arm.apk"), "arm64-v8a")
        assertNull(
            ListingApkGuard.keep(listOf(arm), 50L, setOf("arm64-v8a"), { 20L }),
        )
        assertEquals(InstallWhy.Older, ListingFail.why)

        val newer = zipWithLib(File(dir, "ok.apk"), "arm64-v8a")
        val kept = ListingApkGuard.keep(listOf(newer), 10L, setOf("arm64-v8a"), { 20L })
        assertEquals(listOf(newer), kept)
    }

    private fun zipWithLib(file: File, abi: String): File {
        ZipOutputStream(file.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("lib/$abi/libfoo.so"))
            zip.write(byteArrayOf(1, 2, 3))
            zip.closeEntry()
        }
        return file
    }
}
