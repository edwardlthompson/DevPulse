package dev.foss.goldenpath.about

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.foss.goldenpath.clearPreferenceDataStores
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class AppUpdatePreferencesTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun resetDataStore() {
        context.clearPreferenceDataStores()
    }

    @Test
    fun defaultsCheckIntervalToOff() = runBlocking {
        val prefs = AppUpdatePreferences(context)
        assertEquals("off", prefs.checkInterval.first())
    }

    @Test
    fun persistsCheckInterval() = runBlocking {
        val prefs = AppUpdatePreferences(context)
        prefs.setCheckInterval("weekly")
        assertEquals("weekly", prefs.checkInterval.first())
    }

    @Test
    fun ensureInstalledFormatDetectsApk() = runBlocking {
        val prefs = AppUpdatePreferences(context)
        val format = prefs.ensureInstalledFormat()
        assertFalse(format.isBlank())
    }

    @Test
    fun persistsPendingRestart() = runBlocking {
        val prefs = AppUpdatePreferences(context)
        assertFalse(prefs.pendingRestart.first())
        prefs.setPendingRestart(true)
        assertTrue(prefs.pendingRestart.first())
        prefs.clearPendingRestart()
        assertFalse(prefs.pendingRestart.first())
    }
}
