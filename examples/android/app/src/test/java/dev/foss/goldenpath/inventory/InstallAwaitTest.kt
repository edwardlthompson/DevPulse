package dev.foss.goldenpath.inventory

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstallAwaitTest {
    @Test
    fun awaitSeesSuccessAndFailure() {
        InstallAwait.arm()
        InstallAwait.signal(true)
        assertTrue(InstallAwait.await(50))
        InstallAwait.arm()
        InstallAwait.signal(false)
        assertFalse(InstallAwait.await(50))
        InstallAwait.arm()
        assertFalse(InstallAwait.await(20))
        assertFalse(InstallAwait.pending)
        InstallAwait.markPending()
        assertTrue(InstallAwait.pending)
    }
}
