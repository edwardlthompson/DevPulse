package dev.foss.goldenpath.about

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class ProductReleaseFetcherTest {
    @Test
    fun parseReadsNamedAssetsAndHtmlUrl() {
        val parsed = ProductReleaseFetcher.parse(
            """
            {
              "html_url": "https://github.com/edwardlthompson/DevPulse/releases/tag/v0.27.0",
              "assets": [
                {"name": "sbom.cyclonedx.json", "browser_download_url": "https://example.com/sbom"},
                {"name": "DevPulse-0.27.0.apk", "browser_download_url": "https://example.com/a.apk"}
              ]
            }
            """.trimIndent(),
        )
        assertEquals("https://github.com/edwardlthompson/DevPulse/releases/tag/v0.27.0", parsed?.htmlUrl)
        assertEquals(2, parsed?.assets?.size)
        val picked = ProductUpdate.selectApkAsset(parsed?.assets ?: emptyList())
        assertEquals("0.27.0", picked?.version)
    }

    @Test
    fun parseEmptyAssetsStaysSilent() {
        val parsed = ProductReleaseFetcher.parse("""{"html_url":"https://example.com/rel","assets":[]}""")
        assertTrue(parsed?.assets?.isEmpty() == true)
        assertNull(ProductUpdate.selectApkAsset(parsed?.assets ?: emptyList()))
    }

    @Test
    fun parseInvalidJsonIsNull() {
        assertNull(ProductReleaseFetcher.parse("{"))
    }
}
