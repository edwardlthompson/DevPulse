package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Test

class PackageShareTest {
    @Test
    fun joinsPackageAndSha1() {
        assertEquals("app.x abc", PackageShare.line(" app.x ", " abc "))
        assertEquals("app.x", PackageShare.line("app.x", "  "))
        assertEquals("", PackageShare.line("  ", null))
    }
}
