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
        assertEquals("example-app", lookup.uname)
        assertEquals("https://pool.apk.aptoide.com/example/app.example.apk", lookup.fileUrl)
        assertEquals(210L, lookup.versionCode)
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
    fun pathAltIsUsedWhenPathMissing() {
        val json = """{"data":{"updated":"2024-06-15 12:00:00","file":{"vername":"2.0","vercode":44,"path_alt":"https://pool.apk.aptoide.com/alt/app.apk"}}}"""
        val lookup = AptoideMetaParser.parse(json, now)
        assertEquals(AptoideLookupStatus.Ok, lookup.status)
        assertEquals("https://pool.apk.aptoide.com/alt/app.apk", lookup.fileUrl)
        assertEquals(44L, lookup.versionCode)
    }

    @Test
    fun readsHardwareCpus() {
        val json = """{"data":{"updated":"2024-06-15 12:00:00","file":{"vername":"2.0","vercode":1,"path":"https://pool.apk.aptoide.com/a.apk","hardware":{"cpus":["x86","armeabi-v7a"]}}}}"""
        val lookup = AptoideMetaParser.parse(json, now)
        assertEquals(setOf("x86", "armeabi-v7a"), lookup.nativeCodes)
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
