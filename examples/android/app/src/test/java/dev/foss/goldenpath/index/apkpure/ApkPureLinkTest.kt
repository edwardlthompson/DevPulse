package dev.foss.goldenpath.index.apkpure

import org.junit.Assert.assertEquals
import org.junit.Test

class ApkPureLinkTest {
    @Test
    fun searchPageUsesPackageQuery() {
        assertEquals("https://apkpure.com/search?q=app.x", ApkPureLink.webPage("app.x"))
    }

    @Test
    fun appOpenPrefersMarketDetails() {
        assertEquals(
            "market://details?id=app.x",
            ApkPureLink.appOpenUri("https://apkpure.com/app-x/app.x", "app.x"),
        )
    }

    @Test
    fun appOpenKeepsListingWithoutPackage() {
        val page = "https://apkpure.com/search?q=app.x"
        assertEquals(page, ApkPureLink.appOpenUri(page))
    }
}
