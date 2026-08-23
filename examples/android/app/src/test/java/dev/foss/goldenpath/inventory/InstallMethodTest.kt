package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Test

class InstallMethodTest {
    @Test
    fun sessionFallsBackToSystemWhenInstallsBlocked() {
        assertEquals(InstallMethod.System, InstallMethod.Session.effective(false))
        assertEquals(InstallMethod.Session, InstallMethod.Session.effective(true))
        assertEquals(InstallMethod.System, InstallMethod.System.effective(false))
        assertEquals(InstallMethod.Root, InstallMethod.Root.effective(false))
    }

    @Test
    fun parseUnknownIsSystem() {
        assertEquals(InstallMethod.System, InstallMethod.parse(null))
        assertEquals(InstallMethod.Session, InstallMethod.parse("Session"))
    }
}
