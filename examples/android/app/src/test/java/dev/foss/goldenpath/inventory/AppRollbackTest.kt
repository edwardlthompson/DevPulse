package dev.foss.goldenpath.inventory

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AppRollbackTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setup() {
        SignerReplaceStore.clear(context.filesDir)
        SignerReplaceQueue.clear()
    }

    @Test
    fun failsWhenNoDownloadUrl() {
        val app = InstalledApp("org.test.app", "Test App", "2.0", 20, 0L, 0L, 21, 34, false)
        val version = AppVersionItem("1.0", 10, 1000L, RemoteReleasedSource.Fdroid, null, AppVersionState.Rollback)
        val res = AppRollback.rollback(context, app, version)
        assertTrue(res is RollbackResult.Failed)
    }

    @Test
    fun failsWhenPackageNameMismatch() {
        val app = InstalledApp("org.test.app", "Test App", "2.0", 20, 0L, 0L, 21, 34, false)
        val version = AppVersionItem("1.0", 10, 1000L, RemoteReleasedSource.Fdroid, "https://example.com/app.apk", AppVersionState.Rollback)
        val res = AppRollback.rollback(
            context = context,
            app = app,
            version = version,
            fetcher = { _, dest, _ ->
                dest.writeBytes(byteArrayOf(1, 2, 3, 4))
                Result.success(dest)
            },
            inspectApk = { _ -> ApkInspect("org.other.package", emptySet()) },
        )
        assertTrue(res is RollbackResult.Failed)
        assertTrue((res as RollbackResult.Failed).reason.contains("mismatch"))
    }

    @Test
    fun stagesApkWhenDownloadAndInspectMatch() {
        val app = InstalledApp("org.test.app", "Test App", "2.0", 20, 0L, 0L, 21, 34, false)
        val version = AppVersionItem("1.0", 10, 1000L, RemoteReleasedSource.Fdroid, "https://example.com/app.apk", AppVersionState.Rollback)
        val res = AppRollback.rollback(
            context = context,
            app = app,
            version = version,
            fetcher = { _, dest, _ ->
                dest.writeBytes(byteArrayOf(1, 2, 3, 4))
                Result.success(dest)
            },
            inspectApk = { _ -> ApkInspect("org.test.app", emptySet()) },
        )
        assertTrue(res is RollbackResult.Success)
        val held = SignerReplaceStore.load(context.filesDir)
        assertEquals("org.test.app", held?.packageName)
        assertEquals("Test App", held?.label)
        assertEquals(RemoteReleasedSource.Fdroid, held?.source)
        assertTrue(held!!.files().isNotEmpty())
    }
}
