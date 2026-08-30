package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VersionDeltaTest {
    @Test
    fun lineOnlyWhenNewer() {
        assertEquals("1.0 → 2.0", VersionDelta.line("1.0", "2.0"))
        assertNull(VersionDelta.line("2.0", "2.0"))
        assertNull(VersionDelta.line("2.0", "1.0"))
        assertNull(VersionDelta.line("", "2.0"))
        assertNull(VersionDelta.line("1.2332", "FairEmail-v1.2332a-large-release.apk"))
        assertNull(VersionDelta.line("2023", "2023", 8))
    }
}
