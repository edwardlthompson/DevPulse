package dev.foss.goldenpath.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class UpdateApplierTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun buildInstallIntentTargetsApkMimeType() {
        val uri = Uri.parse("content://dev.foss.goldenpath.fileprovider/updates/test-update.apk")
        val intent = UpdateApplier.buildInstallIntent(context, uri)

        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals("application/vnd.android.package-archive", intent.type)
        assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }
}
