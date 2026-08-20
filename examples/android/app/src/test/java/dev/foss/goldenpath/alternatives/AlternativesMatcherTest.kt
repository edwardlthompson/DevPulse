package dev.foss.goldenpath.alternatives

import org.junit.Assert.assertEquals
import org.junit.Test

class AlternativesMatcherTest {
    @Test
    fun ranksSimilarTitlesAndDedupesSources() {
        val hits = AlternativesMatcher.match(
            queryTitle = "Offline maps",
            candidates = listOf(
                AlternativeHit("org.maps", "Offline Maps", 0, "https://f-droid.org/app"),
                AlternativeHit("org.other", "Calculator", 0, "https://example.com"),
            ),
        )
        assertEquals("org.maps", hits.first().packageName)
        assertEquals(
            listOf("https://a", "https://b"),
            SourcesList.merge(listOf("https://a", " https://b ", "https://a")),
        )
    }
}
