package dev.foss.goldenpath.index.apkmirror

import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.UpdateArtifactMemory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ApkMirrorDirectTest {
    @Before
    fun reset() {
        UpdateArtifactMemory.clear()
    }

    @Test
    fun followsDownloadButtonThenFile() {
        val pages = mapOf(
            "https://www.apkmirror.com/apk/a/" to
                """<a href="/apk/a/a-apk-download/download/?key=1">dl</a>""",
            "https://www.apkmirror.com/apk/a/a-apk-download/download/?key=1" to
                """<a href="/wp-content/themes/APKMirror/download.php?id=7">file</a>""",
        )
        val artifact = ApkMirrorDirect.resolve("app.a", "https://www.apkmirror.com/apk/a/") { pages[it] }
        assertEquals(
            "https://www.apkmirror.com/wp-content/themes/APKMirror/download.php?id=7",
            artifact?.downloadUrl,
        )
        assertEquals(RemoteReleasedSource.ApkMirror, artifact?.source)
    }

    @Test
    fun skipsBlankAndEmptyPage() {
        assertNull(ApkMirrorDirect.resolve("  ", "https://www.apkmirror.com/apk/a/") { "<p>" })
        assertNull(ApkMirrorDirect.resolve("app.a", null) { "<p>" })
        assertNull(ApkMirrorDirect.resolve("app.a", "https://www.apkmirror.com/apk/a/") { null })
    }
}
