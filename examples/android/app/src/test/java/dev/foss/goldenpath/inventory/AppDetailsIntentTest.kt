package dev.foss.goldenpath.inventory

import android.provider.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class AppDetailsIntentTest {
    @Test
    fun packageUriOpensSystemDetails() {
        val intent = AppDetailsIntent.forPackage("app.old")
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intent?.action)
        assertEquals("package", intent?.data?.scheme)
        assertEquals("app.old", intent?.data?.schemeSpecificPart)
    }

    @Test
    fun blankPackageIsIgnored() {
        assertNull(AppDetailsIntent.forPackage("  "))
        assertNull(AppDetailsIntent.forPackage(""))
    }
}
