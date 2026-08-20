package dev.foss.goldenpath.index.play

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayListingTest {
    @Test
    fun httpMissingIsMissing() {
        val lookup = PlayHtmlParser.parse("")
        assertEquals(PlayPresence.Missing, PlayListing.of(404, "", lookup))
        assertEquals(PlayPresence.Missing, PlayListing.of(410, "", lookup))
    }

    @Test
    fun sorryPageWithoutVersionIsMissing() {
        val html = readFixture("play/missing-page.html")
        val lookup = PlayHtmlParser.parse(html)
        assertEquals(PlayPresence.Missing, PlayListing.of(200, html, lookup))
    }

    @Test
    fun versionWithoutDateIsListed() {
        val html = readFixture("play/missing-date.html")
        val lookup = PlayHtmlParser.parse(html)
        assertEquals(PlayPresence.Listed, PlayListing.of(200, html, lookup))
    }

    @Test
    fun okFixtureIsListed() {
        val html = readFixture("play/updated-ok.html")
        val lookup = PlayHtmlParser.parse(html)
        assertEquals(PlayPresence.Listed, PlayListing.of(200, html, lookup))
    }

    @Test
    fun otherHttpIsUnknown() {
        val lookup = PlayHtmlParser.parse("")
        assertEquals(PlayPresence.Unknown, PlayListing.of(503, "", lookup))
        assertEquals(PlayPresence.Unknown, PlayListing.of(403, "consent", lookup))
    }

    @Test
    fun emptyOrBotHtmlIsUnknown() {
        val empty = PlayHtmlParser.parse("")
        assertEquals(PlayPresence.Unknown, PlayListing.of(200, "", empty))
        val html = readFixture("play/bot-wall.html")
        assertEquals(PlayPresence.Unknown, PlayListing.of(200, html, PlayHtmlParser.parse(html)))
    }

    @Test
    fun modernListedPageIsListed() {
        val html = readFixture("play/listed-page.html")
        assertEquals(PlayPresence.Listed, PlayListing.of(200, html, PlayHtmlParser.parse(html)))
    }

    @Test
    fun listedSignalsWithoutVersionAreListed() {
        val html = """<html><head><title>Maps - Apps on Google Play</title></head></html>"""
        assertEquals(PlayPresence.Listed, PlayListing.of(200, html, PlayHtmlParser.parse(html)))
    }

    private fun readFixture(path: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream(path)).bufferedReader().readText()
}
