package dev.foss.goldenpath.ui.theme

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.foss.goldenpath.clearPreferenceDataStores
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class ThemePreferencesTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun resetDataStore() {
        context.clearPreferenceDataStores()
    }

    @Test
    fun defaultsToSystemTheme() = runBlocking {
        val prefs = ThemePreferences(context)
        assertEquals(ThemeMode.System, prefs.themeMode.first())
    }

    @Test
    fun persistsThemeMode() = runBlocking {
        val prefs = ThemePreferences(context)
        prefs.setThemeMode(ThemeMode.Dark)
        assertEquals(ThemeMode.Dark, prefs.themeMode.first())
    }
}
