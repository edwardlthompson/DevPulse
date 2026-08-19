package dev.foss.goldenpath.about

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class ApkDownloadHelperTest {
    @Test
    fun downloadReturnsFalseForInvalidUrl() = runBlocking {
        val dest = File.createTempFile("apk-download", ".apk")
        assertFalse(ApkDownloadHelper.download("https://invalid.invalid/file.apk", dest))
    }

    @Test
    fun downloadReturnsFalseWhenDestinationParentIsAFile() = runBlocking {
        val parentFile = File.createTempFile("apk-parent", ".tmp")
        val dest = File(parentFile, "update.apk")
        assertFalse(ApkDownloadHelper.download("https://example.com/a.apk", dest))
    }
}
