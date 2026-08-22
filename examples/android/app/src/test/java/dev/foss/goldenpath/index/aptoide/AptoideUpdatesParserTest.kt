package dev.foss.goldenpath.index.aptoide

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class AptoideUpdatesParserTest {
    private val now = 1_720_000_000_000L

    @Test
    fun readsListedPackages() {
        val json = checkNotNull(javaClass.classLoader?.getResourceAsStream("aptoide/updates-ok.json"))
            .bufferedReader().use { it.readText() }
        val parsed = AptoideUpdatesParser.parseMany(json, now)
        val expected = LocalDateTime.of(2024, 6, 15, 12, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli()
        assertEquals(AptoideLookupStatus.Ok, parsed.getValue("cm.aptoide.pt").status)
        assertEquals(expected, parsed.getValue("cm.aptoide.pt").updatedOnMs)
        assertEquals("9.22.0", parsed.getValue("cm.aptoide.pt").publishedVersion)
        assertEquals("aptoide", parsed.getValue("cm.aptoide.pt").uname)
        assertEquals("1.21.0", parsed.getValue("org.fdroid.fdroid").publishedVersion)
    }

    @Test
    fun emptyOrFailedBodyIsEmpty() {
        assertTrue(AptoideUpdatesParser.parseMany("", now).isEmpty())
        assertTrue(AptoideUpdatesParser.parseMany("""{"info":{"status":"OK"},"list":[]}""", now).isEmpty())
    }
}
