package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.aptoide.AptoideMetaFetcher
import org.junit.Assert.assertFalse
import org.junit.Test

class ReleaseRefreshProbesTest {
    @Test
    fun aptoideMissStaysUnknown() {
        val offer = ReleaseRefreshProbes.aptoide(
            "app.x",
            AptoideMetaFetcher { Result.success("") },
            nowMs = 1L,
        )
        assertFalse(offer.listed)
        assertFalse(offer.known)
    }
}
