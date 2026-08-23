package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Test

class RefreshScopeTest {
    @Test
    fun emptyWantedKeepsAll() {
        val all = listOf(sampleApp("a", "A"), sampleApp("b", "B"))
        assertEquals(all, RefreshScope.apps(all, emptySet()))
    }

    @Test
    fun wantedKeepsMatchingPackages() {
        val all = listOf(sampleApp("a", "A"), sampleApp("b", "B"), sampleApp("c", "C"))
        assertEquals(listOf(sampleApp("b", "B")), RefreshScope.apps(all, setOf("b")))
    }
}
