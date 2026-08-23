package dev.foss.goldenpath.inventory

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionThenSystemTest {
    @Test
    fun sessionSuccessSkipsSystem() {
        var system = 0
        assertTrue(SessionThenSystem.finish(sessionOk = true, awaitOk = true) { system += 1; true })
        assertTrue(system == 0)
    }

    @Test
    fun pendingOrTimeoutFallsBackToSystem() {
        var system = 0
        assertTrue(SessionThenSystem.finish(sessionOk = true, awaitOk = false) { system += 1; true })
        assertTrue(system == 1)
        assertFalse(SessionThenSystem.finish(sessionOk = false, awaitOk = false) { true })
    }
}
