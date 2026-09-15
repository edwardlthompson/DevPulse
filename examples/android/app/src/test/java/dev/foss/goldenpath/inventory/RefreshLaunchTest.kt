package dev.foss.goldenpath.inventory

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class RefreshLaunchTest {
    @Test
    fun requestedReadsRefreshExtra() {
        assertFalse(RefreshLaunch.requested(null))
        assertFalse(RefreshLaunch.requested(Intent()))
        assertTrue(RefreshLaunch.requested(Intent().putExtra(RefreshLaunch.EXTRA, true)))
        assertFalse(RefreshLaunch.requested(Intent().putExtra(RefreshLaunch.EXTRA, false)))
    }

    @Test
    fun flagReadsAllSourcesExtra() {
        assertEquals(
            true,
            RefreshLaunch.flag(
                Intent().putExtra(RefreshLaunch.EXTRA_ALL_SOURCES, true),
                RefreshLaunch.EXTRA_ALL_SOURCES,
            ),
        )
    }

    @Test
    fun flagReadsPresentDumpExtras() {
        assertEquals(null, RefreshLaunch.flag(Intent(), RefreshLaunch.EXTRA_APK_MIRROR))
        assertEquals(true, RefreshLaunch.flag(
            Intent().putExtra(RefreshLaunch.EXTRA_APK_MIRROR, true),
            RefreshLaunch.EXTRA_APK_MIRROR,
        ))
        assertEquals(false, RefreshLaunch.flag(
            Intent().putExtra(RefreshLaunch.EXTRA_APK_PURE, false),
            RefreshLaunch.EXTRA_APK_PURE,
        ))
    }

    @Test
    fun downloadPackageReadsExtra() {
        assertEquals(null, DownloadLaunch.packageName(null))
        assertEquals(null, DownloadLaunch.packageName(Intent()))
        assertEquals(
            "org.example.app",
            DownloadLaunch.packageName(Intent().putExtra(DownloadLaunch.EXTRA, " org.example.app ")),
        )
        assertEquals(
            "https://f-droid.org/repo/app.apk",
            DownloadLaunch.urlOverride(Intent().putExtra(DownloadLaunch.EXTRA_URL, "https://f-droid.org/repo/app.apk")),
        )
    }

    @Test
    fun listingExtrasDefaultToPlay() {
        assertEquals(null, RefreshLaunch.listingPackage(Intent()))
        assertEquals(
            "com.trandllstudio.jcp",
            RefreshLaunch.listingPackage(Intent().putExtra(RefreshLaunch.EXTRA_LISTING, " com.trandllstudio.jcp ")),
        )
        assertEquals(RemoteReleasedSource.Play, RefreshLaunch.listingSource(Intent()))
        assertEquals(
            RemoteReleasedSource.Fdroid,
            RefreshLaunch.listingSource(Intent().putExtra(RefreshLaunch.EXTRA_LISTING_SOURCE, "Fdroid")),
        )
    }

    @Test
    fun packageNamesSplitsCsv() {
        assertEquals(emptyList<String>(), RefreshLaunch.packageNames(null))
        assertEquals(emptyList<String>(), RefreshLaunch.packageNames(Intent()))
        assertEquals(
            listOf("org.continuumcalendar.app", "dev.foss.obdforge"),
            RefreshLaunch.packageNames(
                Intent().putExtra(RefreshScope.EXTRA_PACKAGES, " org.continuumcalendar.app,dev.foss.obdforge "),
            ),
        )
    }

    @Test
    fun updateAllExtraIsRead() {
        assertFalse(UpdateAllLaunch.requested(null))
        assertFalse(UpdateAllLaunch.requested(Intent()))
        assertTrue(UpdateAllLaunch.requested(Intent().putExtra(UpdateAllLaunch.EXTRA, true)))
    }

    @Test
    fun installMethodAndAckExtrasArePresent() {
        val intent = Intent()
            .putExtra(RefreshLaunch.EXTRA_INSTALL_METHOD, "Session")
            .putExtra(RefreshLaunch.EXTRA_ACK_QUERY, true)
        assertEquals("Session", intent.getStringExtra(RefreshLaunch.EXTRA_INSTALL_METHOD))
        assertEquals(true, RefreshLaunch.flag(intent, RefreshLaunch.EXTRA_ACK_QUERY))
    }
}
