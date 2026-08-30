package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCacheTest {
    @Test
    fun unlimitedCapsKeepEveryApk() {
        val dir = File.createTempFile("updcache", "dir").apply {
            delete()
            mkdirs()
        }
        repeat(12) { i ->
            File(dir, "keep-$i.apk").writeBytes(ByteArray(16))
        }
        UpdateCache.evict(dir, maxFiles = 0, maxBytes = 0L)
        assertEquals(12, dir.listFiles()?.count { it.extension == "apk" })
        dir.deleteRecursively()
    }

    @Test
    fun fileCapStillEvictsOldest() {
        val dir = File.createTempFile("updcache", "dir").apply {
            delete()
            mkdirs()
        }
        repeat(3) { i ->
            val file = File(dir, "old-$i.apk")
            file.writeBytes(ByteArray(8))
            file.setLastModified(1_000L + i)
        }
        UpdateCache.evict(dir, maxFiles = 2, maxBytes = 0L)
        val left = dir.listFiles()?.filter { it.extension == "apk" }?.map { it.name }?.toSet().orEmpty()
        assertEquals(setOf("old-2.apk"), left)
        dir.deleteRecursively()
    }

    @Test
    fun twoGigabyteListingIsAllowed() {
        assertTrue(ApkSizeCap.allow(2L * 1024 * 1024 * 1024))
    }
}
