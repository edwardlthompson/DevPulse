package dev.foss.goldenpath.inventory

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionCompareTest {
    @Test
    fun detectsNewerRemote() {
        assertTrue(VersionCompare.isNewer("2.0", "1.9"))
        assertTrue(VersionCompare.isNewer("1.10.0", "1.9.9"))
        assertFalse(VersionCompare.isNewer("1.2.0", "1.2.0"))
        assertFalse(VersionCompare.isNewer("1.1", "1.2"))
        assertFalse(VersionCompare.isNewer(null, "1.0"))
    }
}
