package dev.foss.goldenpath.inventory

import android.content.Intent
import dev.foss.goldenpath.index.aptoide.AptoideLink
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class StoreListingIntentTest {
    @Test
    fun aptoideInstalledOpensStoreApp() {
        val intent = StoreListingIntent.viewIntent(
            "https://wipefiles.en.aptoide.com/",
            RemoteReleasedSource.Aptoide,
            aptoideInstalled = true,
            packageName = "uk.org.platitudes.wipefiles",
        )
        assertEquals(Intent.ACTION_VIEW, intent?.action)
        assertEquals(AptoideLink.STORE_PACKAGE, intent?.`package`)
        assertEquals("aptoidesearch://uk.org.platitudes.wipefiles", intent?.dataString)
    }

    @Test
    fun aptoideMissingOpensWebListing() {
        val intent = StoreListingIntent.viewIntent(
            "https://en.aptoide.com/app?package_name=app.x",
            RemoteReleasedSource.Aptoide,
            aptoideInstalled = false,
        )
        assertNull(intent?.`package`)
        assertEquals("https://en.aptoide.com/app?package_name=app.x", intent?.dataString)
    }

    @Test
    fun blankUrlIgnored() {
        assertNull(StoreListingIntent.viewIntent("  ", RemoteReleasedSource.Aptoide, true))
    }
}
