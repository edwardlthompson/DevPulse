package dev.foss.goldenpath.index.play

import dev.foss.goldenpath.inventory.RefreshTrace
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayScanTest {
    @Test
    fun listedPageKeepsPlayUrl() {
        val html = readFixture("play/updated-ok.html")
        val offer = PlayScan.toOffer("app.x", PlayPageClient { PlayPageResponse(200, html) })
        assertTrue(offer.listed)
        assertTrue(offer.known)
        assertEquals("2.3.1", offer.versionName)
        assertEquals("https://play.google.com/store/apps/details?id=app.x", offer.pageUrl)
    }

    @Test
    fun missingPageIsDelisted() {
        val html = readFixture("play/missing-page.html")
        val offer = PlayScan.toOffer("app.gone", PlayPageClient { PlayPageResponse(200, html) })
        assertFalse(offer.listed)
        assertTrue(offer.known)
        assertNull(offer.pageUrl)
    }

    @Test
    fun http404IsDelisted() {
        val offer = PlayScan.toOffer("app.gone", PlayPageClient { PlayPageResponse(404, "") })
        assertFalse(offer.listed)
        assertTrue(offer.known)
        assertNull(offer.pageUrl)
    }

    @Test
    fun missingRecoversDateFromWaybackHtml() {
        WaybackPlay.client = WaybackPlayClient {
            PlayHtmlParser.parse(readFixture("play/updated-ok.html"))
        }
        try {
            val offer = PlayScan.toOffer("app.gone", PlayPageClient { PlayPageResponse(404, "") })
            assertFalse(offer.listed)
            assertTrue(offer.known)
            assertEquals(PlayHtmlParser.parse(readFixture("play/updated-ok.html")).updatedOnMs, offer.ms)
            assertEquals("2.3.1", offer.versionName)
        } finally {
            WaybackPlay.client = null
        }
    }

    @Test
    fun listedDoesNotCallWayback() {
        var hits = 0
        WaybackPlay.client = WaybackPlayClient { hits += 1; null }
        try {
            val html = readFixture("play/updated-ok.html")
            val offer = PlayScan.toOffer("app.x", PlayPageClient { PlayPageResponse(200, html) })
            assertTrue(offer.listed)
            assertEquals(0, hits)
        } finally {
            WaybackPlay.client = null
        }
    }

    @Test
    fun botWallIsUnknownNotDelisted() {
        val html = readFixture("play/bot-wall.html")
        val offer = PlayScan.toOffer("com.instagram.android", PlayPageClient { PlayPageResponse(200, html) })
        assertFalse(offer.listed)
        assertFalse(offer.known)
        assertNull(offer.pageUrl)
    }

    @Test
    fun fetchErrorIsUnknownNotDelisted() {
        val offer = PlayScan.toOffer("com.instagram.android", PlayPageClient { error("timeout") })
        assertFalse(offer.listed)
        assertFalse(offer.known)
        assertNull(offer.pageUrl)
    }

    @Test
    fun listedHtmlIsListed() {
        val html = readFixture("play/listed-page.html")
        val offer = PlayScan.toOffer("com.instagram.android", PlayPageClient { PlayPageResponse(200, html) })
        assertTrue(offer.listed)
        assertTrue(offer.known)
        assertEquals("192.168.1.2", offer.versionName)
    }

    @Test
    fun tracesHttpCodeForUnknownBotWall() {
        val lines = mutableListOf<String>()
        RefreshTrace.emit = { lines.add(it) }
        val html = readFixture("play/bot-wall.html")
        PlayScan.toOffer("com.instagram.android", PlayPageClient { PlayPageResponse(403, html) })
        assertTrue(lines.any { it.startsWith("play com.instagram.android http 403 unknown") })
        RefreshTrace.emit = {}
    }

    private fun readFixture(path: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream(path)).bufferedReader().readText()
}
