package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FdroidRepoLinkTest {
    @Test
    fun fingerprintsStayHexOnly() {
        val izzy = FdroidRepoLink.addUri(FdroidRepoLink.IZZY_HOST, FdroidRepoLink.IZZY_FINGERPRINT)
        val guardian = FdroidRepoLink.addUri(FdroidRepoLink.GUARDIAN_HOST, FdroidRepoLink.GUARDIAN_FINGERPRINT)
        val calyx = FdroidRepoLink.addUri(FdroidRepoLink.CALYX_HOST, FdroidRepoLink.CALYX_FINGERPRINT)
        assertTrue(izzy.startsWith("fdroidrepo://"))
        assertEquals(FdroidRepoLink.IZZY_FINGERPRINT, FdroidRepoLink.fingerprintOf(izzy))
        assertEquals(FdroidRepoLink.GUARDIAN_FINGERPRINT, FdroidRepoLink.fingerprintOf(guardian))
        assertEquals(FdroidRepoLink.CALYX_FINGERPRINT, FdroidRepoLink.fingerprintOf(calyx))
        assertTrue(listOf(izzy, guardian, calyx).none { ' ' in it })
        assertNull(FdroidRepoLink.fingerprintHex("not-hex"))
        assertEquals("", FdroidRepoLink.addUri("host", "abc"))
        assertEquals(izzy, FdroidRepoLink.extraAddUri("izzy"))
        assertEquals(guardian, FdroidRepoLink.extraAddUri("guardian"))
        assertEquals(calyx, FdroidRepoLink.extraAddUri("calyx"))
        assertEquals(null, FdroidRepoLink.extraAddUri("official"))
        assertEquals("org.fdroid.fdroid", FdroidRepoLink.preferredClient(true, true))
        assertEquals("com.looker.droidify", FdroidRepoLink.preferredClient(false, true))
        assertEquals(null, FdroidRepoLink.preferredClient(false, false))
    }
}
