package dev.foss.goldenpath.inventory

import java.io.ByteArrayInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class ApkStreamCopyTest {
    @Before
    fun reset() {
        UpdateAllCancel.arm()
        RefreshTrace.emit = {}
    }

    @Test
    fun cancelAbortsBeforeRead() {
        UpdateAllCancel.request()
        try {
            ApkStreamCopy.run(
                input = ByteArrayInputStream(ByteArray(32)),
                total = 32,
                onProgress = null,
                write = { _, _ -> },
            )
            fail("expected cancel")
        } catch (error: IllegalStateException) {
            assertTrue(error.message.orEmpty().contains("cancelled"))
        }
    }

    @Test
    fun reportsStartEndAndThrottledChunks() {
        val size = (ApkStreamCopy.STEP + 8 * 1024).toInt()
        val calls = mutableListOf<Long>()
        ApkStreamCopy.run(
            input = ByteArrayInputStream(ByteArray(size)),
            total = size.toLong(),
            onProgress = { read, _ -> calls += read },
            write = { _, _ -> },
        )
        assertEquals(0L, calls.first())
        assertEquals(size.toLong(), calls.last())
        assertTrue(calls.contains(ApkStreamCopy.STEP))
    }
}
