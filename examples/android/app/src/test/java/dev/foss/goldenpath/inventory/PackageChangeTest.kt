package dev.foss.goldenpath.inventory

import android.content.Intent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PackageChangeTest {
    @Test
    fun installAndUninstallReload() {
        assertTrue(PackageChange.shouldReload(Intent.ACTION_PACKAGE_ADDED, "app.new", false, "app.devpulse"))
        assertTrue(PackageChange.shouldReload(Intent.ACTION_PACKAGE_REMOVED, "app.old", false, "app.devpulse"))
        assertTrue(PackageChange.shouldReload(Intent.ACTION_PACKAGE_REPLACED, "app.up", false, "app.devpulse"))
    }

    @Test
    fun updateStepsDoNotDoubleReloadUntilReplaced() {
        assertFalse(PackageChange.shouldReload(Intent.ACTION_PACKAGE_REMOVED, "app.up", true, "app.devpulse"))
        assertFalse(PackageChange.shouldReload(Intent.ACTION_PACKAGE_ADDED, "app.up", true, "app.devpulse"))
        assertTrue(PackageChange.shouldReload(Intent.ACTION_PACKAGE_REPLACED, "app.up", true, "app.devpulse"))
    }

    @Test
    fun ignoresSelfBlankAndUnknown() {
        assertFalse(PackageChange.shouldReload(Intent.ACTION_PACKAGE_ADDED, "app.devpulse", false, "app.devpulse"))
        assertFalse(PackageChange.shouldReload(Intent.ACTION_PACKAGE_ADDED, "  ", false, "app.devpulse"))
        assertFalse(PackageChange.shouldReload(Intent.ACTION_PACKAGE_CHANGED, "app.x", false, "app.devpulse"))
    }
}
