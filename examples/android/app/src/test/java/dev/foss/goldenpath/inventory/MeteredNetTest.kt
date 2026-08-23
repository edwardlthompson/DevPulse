package dev.foss.goldenpath.inventory

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MeteredNetTest {
    @Test
    fun confirmOnlyWhenMeteredAndHasJobs() {
        assertTrue(MeteredNet.needsConfirm(metered = true, jobs = 2))
        assertFalse(MeteredNet.needsConfirm(metered = false, jobs = 2))
        assertFalse(MeteredNet.needsConfirm(metered = true, jobs = 0))
    }
}
