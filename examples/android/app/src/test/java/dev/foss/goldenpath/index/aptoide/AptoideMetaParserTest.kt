package dev.foss.goldenpath.index.aptoide

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class AptoideMetaParserTest {
    private val now = 1_720_000_000_000L

    @Test
    fun readsUpdatedField() {
        val json = readFixture("updated-ok.json")
        val lookup = AptoideMetaParser.parse(json, now)
        val expected = LocalDateTime.of(2024, 6, 15, 12, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli()
        assertEquals(AptoideLookupStatus.Ok, lookup.status)
        assertEquals(expected, lookup.updatedOnMs)
        assertEquals("2.1.0", lookup.publishedVersion)
    }

    @Test
    fun missingDataIsUnknown() {
        val lookup = AptoideMetaParser.parse(readFixture("missing-data.json"), now)
        assertEquals(AptoideLookupStatus.UnknownCheckManually, lookup.status)
        assertNull(lookup.updatedOnMs)
    }

    @Test
    fun badDateIsUnknownNotGuessed() {
        val lookup = AptoideMetaParser.parse(readFixture("bad-date.json"), now)
        assertEquals(AptoideLookupStatus.UnknownCheckManually, lookup.status)
        assertNull(lookup.updatedOnMs)
        assertEquals("1.0", lookup.publishedVersion)
    }

    @Test
    fun cacheTtl() {
        assertEquals(true, AptoideCachePolicy.isFresh(now, now + 1_000L))
        assertEquals(false, AptoideCachePolicy.isFresh(now, now + AptoideCachePolicy.TTL_MS))
    }

    private fun readFixture(name: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream("aptoide/$name"))
            .bufferedReader()
            .use { it.readText() }
}
