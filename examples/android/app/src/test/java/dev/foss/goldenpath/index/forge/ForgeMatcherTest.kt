package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ForgeMatcherTest {
    @Test
    fun exactPackageIdBeatsWrongTitle() {
        val json = checkNotNull(javaClass.classLoader?.getResourceAsStream("forge/search-wrong-title.json"))
            .bufferedReader()
            .readText()
        val match = ForgeMatcher.rank("app.devpulse", "DevPulse", ForgeSearchParser.parse(json))
        assertEquals("edwardlthompson/DevPulse", match?.candidate?.ownerRepo)
        assertEquals(MatchConfidence.ExactPackage, match?.confidence)
    }

    @Test
    fun backoffOnlyFor403And429() {
        assertEquals(1000L, ForgeBackoff.nextDelayMs(403, 1))
        assertEquals(4000L, ForgeBackoff.nextDelayMs(429, 2))
        assertNull(ForgeBackoff.nextDelayMs(200, 1))
    }

    @Test
    fun sourceLinksSkipBlanks() {
        assertEquals(
            listOf("https://example.com/src"),
            ForgeSourceLinks.fromRecords("https://example.com/src", "  "),
        )
    }
}
