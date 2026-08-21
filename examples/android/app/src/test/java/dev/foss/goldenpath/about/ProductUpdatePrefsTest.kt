package dev.foss.goldenpath.about

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class ProductUpdatePrefsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun resetPrefs() {
        context.getSharedPreferences(ProductUpdatePrefs.PREFS, Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun firstRunHasNoSeenVersionOrCheck() {
        val prefs = ProductUpdatePrefs(context)
        assertNull(prefs.lastSeenVersion())
        assertNull(prefs.lastCheckAt())
        assertNull(prefs.dismissedVersion())
    }

    @Test
    fun laterDismissesThatVersionOnly() {
        val prefs = ProductUpdatePrefs(context)
        prefs.markChecked(1_000L, "0.27.0")
        assertEquals(1_000L, prefs.lastCheckAt())
        assertEquals("0.27.0", prefs.dismissedVersion())
        assertFalsePrompt("0.27.0", prefs)
    }

    @Test
    fun recordsSeenVersionForDonateNudge() {
        val prefs = ProductUpdatePrefs(context)
        prefs.markVersionSeen("0.26.0")
        assertEquals("0.26.0", prefs.lastSeenVersion())
        assertEquals(true, ProductUpdate.shouldNudgeDonate(prefs.lastSeenVersion(), "0.27.0"))
        prefs.markVersionSeen("0.27.0")
        assertEquals(false, ProductUpdate.shouldNudgeDonate(prefs.lastSeenVersion(), "0.27.0"))
    }

    private fun assertFalsePrompt(latest: String, prefs: ProductUpdatePrefs) {
        assertEquals(false, ProductUpdate.shouldPromptUpdate("0.26.0", latest, prefs.dismissedVersion()))
    }
}
