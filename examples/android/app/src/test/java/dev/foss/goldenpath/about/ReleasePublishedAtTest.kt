package dev.foss.goldenpath.about

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReleasePublishedAtTest {
    @Test
    fun parsesIsoOrBlank() {
        assertEquals(1_776_556_800_000L, ReleaseTagFetcher.publishedAtMs("2026-04-19T00:00:00Z"))
        assertNull(ReleaseTagFetcher.publishedAtMs("  "))
    }
}
