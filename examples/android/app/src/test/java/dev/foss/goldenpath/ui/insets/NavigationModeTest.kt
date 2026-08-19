package dev.foss.goldenpath.ui.insets

import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class NavigationModeTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun readNavigationMode_threeButton() {
        Settings.Secure.putInt(context.contentResolver, "navigation_mode", 0)
        assertEquals(NavigationMode.ThreeButton, context.readNavigationMode())
    }

    @Test
    fun readNavigationMode_twoButton() {
        Settings.Secure.putInt(context.contentResolver, "navigation_mode", 1)
        assertEquals(NavigationMode.TwoButton, context.readNavigationMode())
    }

    @Test
    fun readNavigationMode_gesture() {
        Settings.Secure.putInt(context.contentResolver, "navigation_mode", 2)
        assertEquals(NavigationMode.Gesture, context.readNavigationMode())
    }

    @Test
    fun readNavigationMode_unknownWhenUnset() {
        Settings.Secure.putInt(context.contentResolver, "navigation_mode", -1)
        assertEquals(NavigationMode.Unknown, context.readNavigationMode())
    }
}
