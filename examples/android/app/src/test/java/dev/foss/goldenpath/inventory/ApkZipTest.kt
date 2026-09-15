package dev.foss.goldenpath.inventory

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApkZipTest {
    @Test
    fun acceptsZipAndDropsTruncated() {
        val dir = File.createTempFile("zipok", "dir").apply { delete(); mkdirs() }
        val zip = File(dir, "ok.apk")
        ZipOutputStream(zip.outputStream()).use { out ->
            out.putNextEntry(ZipEntry("AndroidManifest.xml"))
            out.write(byteArrayOf(1, 2, 3))
            out.closeEntry()
        }
        assertTrue(ApkZip.ok(zip))
        assertFalse(ApkZip.dropIfInvalid(zip))
        assertTrue(zip.isFile)

        val bad = File(dir, "bad.apk")
        bad.writeBytes(byteArrayOf(0x50, 0x4B, 0x03, 0x04, 1, 2, 3, 4))
        assertFalse(ApkZip.ok(bad))
        assertTrue(ApkZip.dropIfInvalid(bad))
        assertFalse(bad.exists())
    }
}
