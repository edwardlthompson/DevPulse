package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApkInstallTest {
    @Test
    fun rootCommandQuotesSafeApkPath() {
        val args = RootPmInstall.args("/data/local/tmp/app.one.apk")
        assertEquals(listOf("su", "-c", "pm install -r --user 0 \"/data/local/tmp/app.one.apk\""), args)
        assertNull(RootPmInstall.args("/tmp/evil\"; rm -rf /.apk"))
        assertNull(RootPmInstall.args("/tmp/nope.bin"))
    }

    @Test
    fun rootOutcomeReadsSuccess() {
        assertEquals(ApkInstallResult.Ok, RootPmInstall.outcome(InstallShellResult(0, "Success\n")))
        assertTrue(RootPmInstall.outcome(InstallShellResult(1, "Failure [INSTALL_FAILED]")) is ApkInstallResult.Failed)
    }

    @Test
    fun applyDispatchesWithoutGuessing() {
        val apk = File.createTempFile("app", ".apk").apply { writeBytes(byteArrayOf(1)) }
        var system = 0
        var session = 0
        var rootArgs: List<String>? = null
        val shell = InstallShell { args ->
            rootArgs = args
            InstallShellResult(0, "Success")
        }
        assertEquals(
            ApkInstallResult.Launched,
            ApkInstall.apply(apk, InstallMethod.System, { system += 1 }, { session += 1 }, shell),
        )
        assertEquals(
            ApkInstallResult.Launched,
            ApkInstall.apply(apk, InstallMethod.Session, { system += 1 }, { session += 1 }, shell),
        )
        assertEquals(
            ApkInstallResult.Ok,
            ApkInstall.apply(apk, InstallMethod.Root, { system += 1 }, { session += 1 }, shell),
        )
        assertEquals(1, system)
        assertEquals(1, session)
        assertTrue(rootArgs.orEmpty().contains("su"))
    }
}
