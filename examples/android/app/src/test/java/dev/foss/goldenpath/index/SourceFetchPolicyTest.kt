package dev.foss.goldenpath.index

import dev.foss.goldenpath.index.fdroid.FdroidCachePolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SourceFetchPolicyTest {
    @Test
    fun twentySecondPerSourceTimeout() {
        assertEquals(20_000, SourceFetchPolicy.TIMEOUT_MS)
        assertEquals(20_000, SourceFetchPolicy.CONNECT_TIMEOUT_MS)
        assertEquals(20_000, SourceFetchPolicy.READ_TIMEOUT_MS)
    }

    @Test
    fun twelveHourCachedIndex() {
        assertEquals(12 * 60 * 60 * 1000L, SourceFetchPolicy.INDEX_CACHE_TTL_MS)
        assertEquals(SourceFetchPolicy.INDEX_CACHE_TTL_MS, FdroidCachePolicy.TTL_MS)
        assertTrue(FdroidCachePolicy.isFresh(10L, 10L + FdroidCachePolicy.TTL_MS - 1))
        assertFalse(FdroidCachePolicy.isFresh(10L, 10L + FdroidCachePolicy.TTL_MS))
    }
}
