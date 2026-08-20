package dev.foss.goldenpath.index.aptoide

import org.junit.Assert.assertEquals
import org.junit.Test

class AptoideLinkTest {
    @Test
    fun unameBuildsAppViewHost() {
        assertEquals(
            "https://wipefiles.en.aptoide.com/",
            AptoideLink.webPage("uk.org.platitudes.wipefiles", "wipefiles"),
        )
    }

    @Test
    fun rejectsUnsafeUname() {
        assertEquals(
            "https://en.aptoide.com/app?package_name=app.x",
            AptoideLink.webPage("app.x", "evil.en.aptoide.com"),
        )
    }

    @Test
    fun appOpenKeepsUnameListing() {
        val page = "https://wipefiles.en.aptoide.com/"
        assertEquals(page, AptoideLink.appOpenUri(page))
    }

    @Test
    fun appOpenSearchesWhenOnlyPackageQuery() {
        assertEquals(
            "aptoidesearch://app.x",
            AptoideLink.appOpenUri("https://en.aptoide.com/app?package_name=app.x"),
        )
    }
}
