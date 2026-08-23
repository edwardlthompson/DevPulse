package dev.foss.goldenpath.index.play

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlayHtmlParserTest {
    @Test
    fun parsesIsoDateAndVersionFromFixture() {
        val html = readFixture("play/updated-ok.html")
        val result = PlayHtmlParser.parse(html)
        assertEquals(PlayLookupStatus.Ok, result.status)
        assertEquals("2.3.1", result.publishedVersion)
        val expected = java.time.LocalDate.parse("2024-06-01")
            .atStartOfDay()
            .toInstant(java.time.ZoneOffset.UTC)
            .toEpochMilli()
        assertEquals(expected, result.updatedOnMs)
    }

    @Test
    fun missingDateIsUnknownNeverGuessed() {
        val html = readFixture("play/missing-date.html")
        val result = PlayHtmlParser.parse(html)
        assertEquals(PlayLookupStatus.UnknownCheckManually, result.status)
        assertNull(result.updatedOnMs)
        assertEquals("1.0", result.publishedVersion)
    }

    @Test
    fun garbageDateIsUnknown() {
        val result = PlayHtmlParser.parse("""<div itemprop="datePublished" content="last month"></div>""")
        assertEquals(PlayLookupStatus.UnknownCheckManually, result.status)
        assertNull(result.updatedOnMs)
    }

    @Test
    fun listedFixtureYieldsVersionAndDate() {
        val html = readFixture("play/listed-page.html")
        val result = PlayHtmlParser.parse(html)
        assertEquals(PlayLookupStatus.Ok, result.status)
        assertEquals("192.168.1.2", result.publishedVersion)
        assertEquals(true, PlayHtmlParser.looksListed(html))
    }

    @Test
    fun developerWebsiteIsKept() {
        val result = PlayHtmlParser.parse(
            """<div itemprop="datePublished" content="2024-06-01"></div>""" +
                """{"developerWebsite":{"url":"https://github.com/TeamNewPipe/NewPipe"}}""",
        )
        assertEquals("https://github.com/TeamNewPipe/NewPipe", result.developerUrl)
    }

    @Test
    fun botWallIsNotListed() {
        assertEquals(false, PlayHtmlParser.looksListed(readFixture("play/bot-wall.html")))
    }

    private fun readFixture(path: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream(path)).bufferedReader().readText()
}
