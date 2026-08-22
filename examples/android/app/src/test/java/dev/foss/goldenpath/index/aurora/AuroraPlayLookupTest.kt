package dev.foss.goldenpath.index.aurora

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuroraPlayLookupTest {
    @Test
    fun fillOmittedRetriesMissingAndHoles() {
        var retried = emptyList<String>()
        val filled = AuroraPlayLookup.fillOmitted(
            listOf("com.instagram.android", "app.gone", "app.hit"),
            mapOf(
                "app.hit" to AuroraPlayApp(AuroraPlayStatus.Listed, "1"),
                "app.gone" to AuroraPlayApp(AuroraPlayStatus.Missing),
            ),
        ) { names ->
            retried = names
            mapOf("com.instagram.android" to AuroraPlayApp(AuroraPlayStatus.Listed, "1.0", 2L))
        }
        assertEquals(setOf("com.instagram.android", "app.gone"), retried.toSet())
        assertEquals(AuroraPlayStatus.Listed, filled.getValue("com.instagram.android").status)
        assertEquals(AuroraPlayStatus.Listed, filled.getValue("app.hit").status)
        assertEquals(AuroraPlayStatus.Missing, filled.getValue("app.gone").status)
    }

    @Test
    fun fillOmittedSkipsRetryWhenComplete() {
        var retries = 0
        AuroraPlayLookup.fillOmitted(
            listOf("app.x"),
            mapOf("app.x" to AuroraPlayApp(AuroraPlayStatus.Listed, "1")),
        ) {
            retries += 1
            emptyMap()
        }
        assertEquals(0, retries)
    }

    @Test
    fun fillOmittedStillMissingAfterRetry() {
        val filled = AuroraPlayLookup.fillOmitted(
            listOf("app.gone"),
            emptyMap(),
        ) { emptyMap() }
        assertTrue(filled.getValue("app.gone").status == AuroraPlayStatus.Missing)
    }
}
