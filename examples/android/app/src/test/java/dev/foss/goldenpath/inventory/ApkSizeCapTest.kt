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
        assertTrue(ApkSizeCap.allow(ApkHttpFetcher.MAX_BYTES))
        assertFalse(ApkSizeCap.allow(ApkHttpFetcher.MAX_BYTES + 1L))
        assertFalse(ApkSizeCap.allow(268_435_472L))
    }
}
