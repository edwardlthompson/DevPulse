package dev.foss.goldenpath.about

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class DonationsLoaderTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun parseDisabledAndEnabled() {
        val off = DonationsLoader.parse("""{"enabled":false,"message":"Hi","links":[]}""")
        assertTrue(!off.enabled)
        assertEquals("Hi", off.message)
        assertEquals(0, off.links.size)
        val on = DonationsLoader.parse(
            """{"enabled":true,"message":"Hi","links":[{"label":"Ko-fi","url":"https://example.com/donate"}]}""",
        )
        assertTrue(on.enabled)
        assertEquals("Ko-fi", on.links.single().label)
    }

    @Test
    fun loadAssetsUsesDisabledProductDefault() {
        val cfg = DonationsLoader.load(context)
        assertEquals("If this project helps you, consider supporting development.", cfg.message)
        assertTrue(!cfg.enabled)
        assertEquals(0, cfg.links.size)
    }
}
