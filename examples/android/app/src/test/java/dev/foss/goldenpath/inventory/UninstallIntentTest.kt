package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class UninstallIntentTest {
    @Test
    fun packageUriOpensUninstall() {
        val intent = UninstallIntent.forPackage("app.old")
        assertEquals(android.content.Intent.ACTION_DELETE, intent?.action)
        assertEquals("package", intent?.data?.scheme)
        assertEquals("app.old", intent?.data?.schemeSpecificPart)
    }

    @Test
    fun blankPackageIsIgnored() {
        assertNull(UninstallIntent.forPackage("  "))
        assertNull(UninstallIntent.forPackage(""))
        assertEquals(false, UninstallIntent.launch(org.robolectric.RuntimeEnvironment.getApplication(), ""))
    }
}
