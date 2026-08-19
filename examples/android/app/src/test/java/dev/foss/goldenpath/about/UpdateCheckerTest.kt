package dev.foss.goldenpath.about

import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateCheckerTest {
    @Test
    fun parseVersion_extractsSemver() {
        assertEquals("1.2.3", UpdateChecker.parseVersion("v1.2.3"))
    }

    @Test
    fun isNewer_detectsOlderCurrent() {
        assertEquals(true, UpdateChecker.isNewer("0.1.0", "0.2.0"))
    }

    @Test
    fun isNewer_falseWhenCurrentIsLatest() {
        assertEquals(false, UpdateChecker.isNewer("1.0.0", "1.0.0"))
    }
}
