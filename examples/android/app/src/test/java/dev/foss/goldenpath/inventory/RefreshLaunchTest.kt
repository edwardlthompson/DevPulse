package dev.foss.goldenpath.inventory

import android.content.Intent
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
}
