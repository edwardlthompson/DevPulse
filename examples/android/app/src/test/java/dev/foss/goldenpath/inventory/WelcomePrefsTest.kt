package dev.foss.goldenpath.inventory

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.foss.goldenpath.clearPreferenceDataStores
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class WelcomePrefsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun resetDataStore() {
        context.clearPreferenceDataStores()
    }

    @Test
    fun markSeenAndAckBothCountAsSeen() = runBlocking {
        val welcome = WelcomePrefs(context)
        welcome.markSeen()
        assertTrue(welcome.seen.first())
        InventoryPreferences(context).setQueryAllPackagesAcknowledged(true)
        assertTrue(welcome.seen.first())
    }
}
