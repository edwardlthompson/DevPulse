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
    fun appOpenPrefersOfficialAppPath() {
        assertEquals(
            "https://en.aptoide.com/app?package_name=uk.org.platitudes.wipefiles",
            AptoideLink.appOpenUri("https://wipefiles.en.aptoide.com/", "uk.org.platitudes.wipefiles"),
        )
    }

    @Test
    fun appOpenKeepsUnameListingWithoutPackage() {
        val page = "https://wipefiles.en.aptoide.com/"
        assertEquals(page, AptoideLink.appOpenUri(page))
    }

    @Test
    fun appOpenUsesPackageQueryOnOfficialHost() {
        assertEquals(
            "https://en.aptoide.com/app?package_name=app.x",
            AptoideLink.appOpenUri("https://en.aptoide.com/app?package_name=app.x"),
        )
    }
}
