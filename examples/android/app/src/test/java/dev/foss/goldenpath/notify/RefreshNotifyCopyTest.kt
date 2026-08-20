package dev.foss.goldenpath.notify

import dev.foss.goldenpath.inventory.RefreshProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class RefreshNotifyCopyTest {
    @Test
    fun lookedUpCountPrefersTotal() {
        assertEquals(10, RefreshNotifyCopy.lookedUpCount(RefreshProgress(3, 10)))
        assertEquals(4, RefreshNotifyCopy.lookedUpCount(RefreshProgress(4, 0)))
    }
}
