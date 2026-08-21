package dev.foss.goldenpath.about

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductUpdateTest {

    @Test
    fun dailyCheckWaitsAFullDay() {
        assertTrue(ProductUpdate.shouldCheckDaily(null, 0L))
        assertFalse(ProductUpdate.shouldCheckDaily(0L, ProductUpdate.MS_DAY - 1))
        assertTrue(ProductUpdate.shouldCheckDaily(0L, ProductUpdate.MS_DAY))
    }

    @Test
    fun apkVersionIgnoresTemplateTags() {
        assertEquals("0.26.0", ProductUpdate.parseApkVersion("DevPulse-0.26.0.apk"))
        assertEquals("1.2.3", ProductUpdate.parseApkVersion("devpulse-1.2.3-foss.apk"))
        assertEquals(null, ProductUpdate.parseApkVersion("v0.22.1"))
        assertEquals(null, ProductUpdate.parseApkVersion("sbom.cyclonedx.json"))
    }

    @Test
    fun isNewerThanCurrent() {
        assertTrue(ProductUpdate.isNewerVersion("0.26.0", "0.27.0"))
        assertFalse(ProductUpdate.isNewerVersion("0.27.0", "0.26.0"))
        assertFalse(ProductUpdate.isNewerVersion("0.26.0", "0.26.0"))
    }

    @Test
    fun donateNudgeOnlyAfterVersionChange() {
        assertFalse(ProductUpdate.shouldNudgeDonate(null, "0.26.0"))
        assertFalse(ProductUpdate.shouldNudgeDonate("0.26.0", "0.26.0"))
        assertTrue(ProductUpdate.shouldNudgeDonate("0.26.0", "0.27.0"))
        assertFalse(ProductUpdate.shouldNudgeDonate("0.27.0", ""))
    }

    @Test
    fun updatePromptSkipsDismissedVersion() {
        assertTrue(ProductUpdate.shouldPromptUpdate("0.26.0", "0.27.0", null))
        assertFalse(ProductUpdate.shouldPromptUpdate("0.26.0", "0.27.0", "0.27.0"))
        assertFalse(ProductUpdate.shouldPromptUpdate("0.27.0", "0.27.0", null))
        assertFalse(ProductUpdate.shouldPromptUpdate("0.26.0", null, null))
    }

    @Test
    fun selectApkAssetReadsProductFilename() {
        val picked = ProductUpdate.selectApkAsset(
            listOf(
                ProductUpdate.NamedAsset("sbom.cyclonedx.json", "https://example.com/sbom"),
                ProductUpdate.NamedAsset("DevPulse-0.27.0.apk", "https://example.com/a.apk"),
            ),
        )
        assertEquals("0.27.0", picked?.version)
        assertEquals("https://example.com/a.apk", picked?.url)
    }

    @Test
    fun installUrlFallsBackToReleasePage() {
        assertEquals("https://example.com/a.apk", ProductUpdate.installUrl("https://example.com/a.apk", "https://x"))
        assertEquals("https://github.com/x", ProductUpdate.installUrl("  ", "https://github.com/x"))
        assertEquals(ProductUpdate.RELEASES_PAGE, ProductUpdate.installUrl(null, null))
    }
}
