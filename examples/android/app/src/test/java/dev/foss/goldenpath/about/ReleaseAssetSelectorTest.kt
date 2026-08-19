package dev.foss.goldenpath.about

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReleaseAssetSelectorTest {
    @Test
    fun select_matchesExactFormatOnly() {
        val assets = listOf(
            ReleaseAsset("msi", "https://example.com/a.msi"),
            ReleaseAsset("exe", "https://example.com/a.exe"),
        )
        assertEquals("a.msi", ReleaseAssetSelector.select(assets, "msi")?.url?.substringAfterLast('/'))
        assertNull(ReleaseAssetSelector.select(assets, "deb"))
    }
}
