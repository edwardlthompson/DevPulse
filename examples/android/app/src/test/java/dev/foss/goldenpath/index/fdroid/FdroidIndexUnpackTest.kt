package dev.foss.goldenpath.index.fdroid

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FdroidIndexUnpackTest {
    @Test
    fun readsJsonFromJar() {
        val json = """{"apps":[{"packageName":"org.nested","name":{"en-US":"Hi"},"lastUpdated":1700000000000}]}"""
        val jar = ByteArrayOutputStream()
        ZipOutputStream(jar).use { zip ->
            zip.putNextEntry(ZipEntry("index-v1.json"))
            zip.write(json.toByteArray())
            zip.closeEntry()
        }
        val unpacked = FdroidIndexUnpack.readJson(ByteArrayInputStream(jar.toByteArray()), fromJar = true)
        val records = FdroidIndexParser.parse(unpacked, "official")
        assertEquals("org.nested", records.single().packageName)
        assertEquals(1_700_000_000_000L, records.single().lastUpdatedMs)
        assertTrue(FdroidIndexUnpack.isJarUrl("https://f-droid.org/repo/index-v1.jar"))
    }

    @Test
    fun rejectsIndexOverByteCap() {
        val json = """{"apps":[{"packageName":"org.huge"}]}"""
        try {
            FdroidIndexUnpack.readJson(ByteArrayInputStream(json.toByteArray()), fromJar = false, maxBytes = 8)
            throw AssertionError("expected oversized index to fail")
        } catch (error: IllegalStateException) {
            assertTrue(error.message.orEmpty().contains("exceeds"))
        }
    }
}
