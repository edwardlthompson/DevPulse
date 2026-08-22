package dev.foss.goldenpath.index.apkmirror

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApkMirrorDownloadTest {
    @Test
    fun readsDownloadPhpAndButton() {
        assertEquals(
            "https://www.apkmirror.com/wp-content/themes/APKMirror/download.php?id=9",
            ApkMirrorDownload.fileUrl(
                """<a href="/wp-content/themes/APKMirror/download.php?id=9">file</a>""",
            ),
        )
        assertEquals(
            "https://www.apkmirror.com/apk/a/a-apk-download/download/?key=1",
            ApkMirrorDownload.nextPage(
                """<a class="downloadButton" href="/apk/a/a-apk-download/download/?key=1">dl</a>""",
            ),
        )
        assertNull(ApkMirrorDownload.fileUrl("<p>no file</p>"))
    }
}
