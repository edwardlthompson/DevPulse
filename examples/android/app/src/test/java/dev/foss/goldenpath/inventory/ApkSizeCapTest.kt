package dev.foss.goldenpath.inventory

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApkSizeCapTest {
    @Test
    fun unknownOrChunkedLengthIsAllowed() {
        assertTrue(ApkSizeCap.allow(-1L))
        assertTrue(ApkSizeCap.allow(0L))
    }

    @Test
    fun listedSizeMustFitTheCap() {
        assertTrue(ApkSizeCap.allow(2L * 1024 * 1024 * 1024))
        assertTrue(ApkSizeCap.allow(ApkHttpFetcher.MAX_BYTES))
        assertTrue(ApkSizeCap.allow(268_435_472L))
        assertFalse(ApkSizeCap.allow(50L, maxBytes = 10L))
    }

    @Test
    fun retryOnlyTransientTransferDrops() {
        assertTrue(ApkHttpFetcher.retryable("Connection reset"))
        assertTrue(ApkHttpFetcher.retryable("Read timed out"))
        assertFalse(ApkHttpFetcher.retryable("apk too large"))
        assertFalse(ApkHttpFetcher.retryable("apk cancelled"))
        assertFalse(ApkHttpFetcher.retryable("apk 404"))
    }
}
