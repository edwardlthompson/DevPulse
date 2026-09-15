package dev.foss.goldenpath.inventory

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class XapkUnpackTest {
    @Test
    fun expandsMatchingApkAndSplits() {
        val dir = File.createTempFile("xapk", "dir").apply { delete(); mkdirs() }
        val archive = File(dir, "bundle.xapk")
        ZipOutputStream(archive.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("manifest.json"))
            zip.write("{}".toByteArray())
            zip.closeEntry()
            zip.putNextEntry(ZipEntry("payload/com.a.apk"))
            zip.write("base".toByteArray())
            zip.closeEntry()
            zip.putNextEntry(ZipEntry("payload/config.arm64_v8a.apk"))
            zip.write("split".toByteArray())
            zip.closeEntry()
        }
        val files = XapkUnpack.expand(archive, "com.a", dir) { file ->
            when (file.name) {
                "com.a.apk" -> ApkInspect("com.a", emptySet())
                else -> ApkInspect(null, emptySet())
            }
        }
        assertEquals(2, files?.size)
        assertTrue(files!!.any { it.name == "com.a.apk" })
        assertTrue(files.any { it.name == "config.arm64_v8a.apk" })
    }

    @Test
    fun rejectsZipWithoutMatchingPackage() {
        val dir = File.createTempFile("xapk-bad", "dir").apply { delete(); mkdirs() }
        val archive = File(dir, "bundle.xapk")
        ZipOutputStream(archive.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("other.apk"))
            zip.write("apk".toByteArray())
            zip.closeEntry()
        }
        assertNull(
            XapkUnpack.expand(archive, "com.a", dir) { ApkInspect("com.b", emptySet()) },
        )
    }
}
