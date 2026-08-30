package dev.foss.goldenpath.inventory

import android.content.Intent
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SignerClashTest {
    @Test
    fun offerOnlyWhenCertsDifferAndPackageMatches() {
        assertTrue(SignerClash.offer("app.x", "app.x", setOf("aa"), setOf("bb"), false))
        assertFalse(SignerClash.offer("app.x", "app.x", setOf("aa"), setOf("aa"), false))
        assertFalse(SignerClash.offer("app.x", "app.y", setOf("aa"), setOf("bb"), false))
        assertFalse(SignerClash.offer("app.x", "app.x", setOf("aa"), setOf("bb"), true))
        assertFalse(SignerClash.offer("  ", "app.x", setOf("aa"), setOf("bb"), false))
        assertFalse(SignerClash.offer("app.x", "app.x", emptySet(), setOf("bb"), false))
        assertFalse(SignerClash.offer("app.x", "app.x", setOf("aa"), emptySet(), false))
        assertFalse(SignerClash.offer("app.x", null, setOf("aa"), setOf("bb"), false))
    }

    @Test
    fun resumeAndAfterUninstallNeverUninstallTwice() {
        assertEquals(SignerReplaceNext.Confirm, SignerClash.resume(installed = true, filesReady = true))
        assertEquals(SignerReplaceNext.Install, SignerClash.resume(installed = false, filesReady = true))
        assertEquals(SignerReplaceNext.MissingFile, SignerClash.resume(installed = false, filesReady = false))
        assertEquals(SignerReplaceNext.Cancelled, SignerClash.afterUninstall(installed = true, filesReady = true))
        assertEquals(SignerReplaceNext.Install, SignerClash.afterUninstall(installed = false, filesReady = true))
        assertEquals(SignerReplaceNext.MissingFile, SignerClash.afterUninstall(installed = false, filesReady = false))
    }

    @Test
    fun filesReadyRejectsEmptyOrMissing() {
        val gone = File("no-such-apk.apk")
        assertFalse(SignerClash.filesReady(emptyList()))
        assertFalse(SignerClash.filesReady(listOf(gone)))
        val ok = File.createTempFile("hold", ".apk")
        ok.writeBytes(byteArrayOf(1, 2, 3))
        assertTrue(SignerClash.filesReady(listOf(ok)))
        ok.delete()
    }

    @Test
    fun uninstallDoneIgnoresReplaceAndOtherPackages() {
        assertTrue(
            SignerClash.uninstallDone(Intent.ACTION_PACKAGE_REMOVED, "app.x", replacing = false, "app.x"),
        )
        assertFalse(
            SignerClash.uninstallDone(Intent.ACTION_PACKAGE_REMOVED, "app.x", replacing = true, "app.x"),
        )
        assertFalse(
            SignerClash.uninstallDone(Intent.ACTION_PACKAGE_REMOVED, "app.y", replacing = false, "app.x"),
        )
        assertFalse(
            SignerClash.uninstallDone(Intent.ACTION_PACKAGE_ADDED, "app.x", replacing = false, "app.x"),
        )
    }
}
