package dev.foss.goldenpath.about

import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class ReleaseTagFetcherTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun loadReleaseRepoReturnsNullWhenEmpty() {
        assertNull(ReleaseTagFetcher.loadReleaseRepo(context))
    }

    @Test
    fun manifestDeclaresInternetPermission() {
        val info = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS,
        )
        val perms = info.requestedPermissions?.toList() ?: emptyList()
        assertTrue(
            "INTERNET permission required for GitHub release fetch",
            perms.contains("android.permission.INTERNET"),
        )
    }

    @Test
    fun fetchLatestReleaseReturnsNullForInvalidRepo() {
        val result = kotlinx.coroutines.runBlocking {
            ReleaseTagFetcher.fetchLatestRelease("invalid/empty-repo-404")
        }
        assertNull(result)
    }
}
