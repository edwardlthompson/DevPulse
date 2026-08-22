package dev.foss.goldenpath.index.aurora

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuroraPlayBundleTest {
    @Test
    fun requiresBaseAndKeepsSplits() {
        val files = AuroraPlayBundle.files(
            "com.trandllstudio.jcp",
            AuroraPlayFiles {
                listOf(
                    AuroraPlayFile("https://redirector.gvt1.com/edgedl/android/market/split", base = false),
                    AuroraPlayFile("https://redirector.gvt1.com/edgedl/android/market/base", "3.0.7", 31, base = true),
                    AuroraPlayFile("ftp://evil.example/x", base = true),
                )
            },
        )
        assertEquals(2, files.size)
        assertTrue(files.first().base)
        assertEquals("https://redirector.gvt1.com/edgedl/android/market/base", files.first().url)
        assertEquals("https://redirector.gvt1.com/edgedl/android/market/split", files.last().url)
    }

    @Test
    fun untypedFilesAreKeptWhenNoBaseFlag() {
        val files = AuroraPlayBundle.files(
            "app.one",
            AuroraPlayFiles {
                listOf(AuroraPlayFile("https://redirector.gvt1.com/edgedl/android/market/one", base = false))
            },
        )
        assertEquals(1, files.size)
    }
}
