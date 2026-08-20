package dev.foss.goldenpath.index.apkmirror

import dev.foss.goldenpath.inventory.RemoteReleasedSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class ApkMirrorMetaParserTest {
    private val now = 1_720_000_000_000L

    @Test
    fun readsExistsVersionAndDate() {
        val offer = ApkMirrorMetaParser.parseMany(readFixture("exists-ok.json"), now).getValue("app.listed")
        val expected = LocalDate.of(2024, 6, 15).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        assertTrue(offer.listed)
        assertTrue(offer.known)
        assertEquals("2.4.0", offer.versionName)
        assertEquals(expected, offer.ms)
        assertEquals(RemoteReleasedSource.ApkMirror, offer.source)
        assertEquals("https://www.apkmirror.com/apk/listed/", offer.pageUrl)
    }

    @Test
    fun missingIsKnownNotListed() {
        val offer = ApkMirrorMetaParser.parseMany(readFixture("missing.json"), now).getValue("app.gone")
        assertFalse(offer.listed)
        assertTrue(offer.known)
        assertNull(offer.pageUrl)
    }

    private fun readFixture(name: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream("apkmirror/$name"))
            .bufferedReader()
            .use { it.readText() }
}
