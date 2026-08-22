package dev.foss.goldenpath.index.play

import dev.foss.goldenpath.index.aurora.AuroraPlayApp
import dev.foss.goldenpath.index.aurora.AuroraPlayDetails
import dev.foss.goldenpath.index.aurora.AuroraPlayStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayScanAuroraMissTest {
    @Test
    fun auroraMissingSkipsHtml() {
        var html = 0
        val offer = PlayScan.toOffer(
            "app.gone",
            PlayPageClient { html += 1; PlayPageResponse(200, "") },
            AuroraPlayDetails { mapOf("app.gone" to AuroraPlayApp(AuroraPlayStatus.Missing)) },
        )
        assertEquals(0, html)
        assertFalse(offer.listed)
        assertTrue(offer.known)
    }
}
