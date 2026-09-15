package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RootSessionInstallTest {
    @After
    fun resetSu() {
        RootPmInstall.resetAvailable()
    }
    @Test
    fun parsesSessionIdAndWritesSplits() {
        assertEquals("42", RootSessionInstall.sessionId("Success: created install session [42]"))
        assertNull(RootSessionInstall.sessionId("Failure"))
        val apk = File.createTempFile("split", ".apk").apply { writeBytes(byteArrayOf(1, 2)) }
        val args = RootSessionInstall.writeArgs("42", 1, apk)
        assertTrue(args!!.last().contains("pm install-write -S ${apk.length()} 42 split-1.apk"))
        assertNull(RootSessionInstall.writeArgs("nope", 0, apk))
    }

    @Test
    fun installsSplitsThroughFakeShell() {
        val a = File.createTempFile("base", ".apk").apply { writeBytes(byteArrayOf(1)) }
        val b = File.createTempFile("cfg", ".apk").apply { writeBytes(byteArrayOf(2)) }
        val cmds = mutableListOf<String>()
        val shell = InstallShell { args ->
            val cmd = args.last()
            cmds += cmd
            when {
                "install-create" in cmd -> InstallShellResult(0, "Success: created install session [7]\n")
                "install-write" in cmd -> InstallShellResult(0, "")
                "install-commit" in cmd -> InstallShellResult(0, "Success\n")
                else -> InstallShellResult(1, "no")
            }
        }
        assertTrue(RootSessionInstall.run(listOf(a, b), shell))
        assertTrue(cmds.any { "install-create" in it })
        assertEquals(2, cmds.count { "install-write" in it })
        assertTrue(cmds.any { "install-commit 7" in it })
    }

    @Test
    fun missingSuDoesNotThrow() {
        RootPmInstall.resetAvailable()
        val apk = File.createTempFile("one", ".apk").apply { writeBytes(byteArrayOf(1)) }
        val shell = InstallShell { throw java.io.IOException("Cannot run program su") }
        assertFalse(RootSessionInstall.run(listOf(apk), shell))
        RootPmInstall.resetAvailable()
        assertFalse(RootPmInstall.available(InstallShell { InstallShellResult(127, "su: not found") }))
        val miss = ProcessInstallShell.run(listOf("devpulse-no-such-binary"))
        assertTrue(miss.exitCode != 0)
    }

    @Test
    fun waitIsNotSuccessWhenAlreadyNewer() {
        assertFalse(RootSessionInstall.waitNeeded(175981782L, 175963030L))
        assertFalse(RootSessionInstall.waitOk(175981782L, 175963030L, 175981782L))
        assertTrue(RootSessionInstall.waitNeeded(10L, 20L))
        assertTrue(RootSessionInstall.waitOk(10L, 20L, 20L))
        assertFalse(RootSessionInstall.waitOk(10L, 20L, 10L))
    }
}
