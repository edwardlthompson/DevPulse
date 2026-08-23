package dev.foss.goldenpath.inventory

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ListingFitTest {
    @Test
    fun sdkBlocksWhenListingNeedsHigherApi() {
        assertTrue(ListingFit.sdkOk(null, 34))
        assertTrue(ListingFit.sdkOk(26, 34))
        assertFalse(ListingFit.sdkOk(35, 34))
        assertTrue(ListingFit.sdkOk(35, 0))
    }

    @Test
    fun abiBlocksWhenNoOverlap() {
        assertTrue(ListingFit.abiOk(emptySet(), setOf("arm64-v8a")))
        assertTrue(ListingFit.abiOk(setOf("arm64-v8a"), setOf("arm64-v8a", "armeabi-v7a")))
        assertFalse(ListingFit.abiOk(setOf("x86_64"), setOf("arm64-v8a")))
    }
}
