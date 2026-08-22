package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.apkmirror.ApkMirrorFetchPolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class ListingPageHttpTest {
    @Test
    fun usesMirrorUserAgentOnMirrorPages() {
        assertEquals(
            ApkMirrorFetchPolicy.USER_AGENT,
            ListingPageHttp.ua("https://www.apkmirror.com/apk/a/"),
        )
        assertEquals(ApkHttpFetcher.USER_AGENT, ListingPageHttp.ua("https://f-droid.org/packages/a/"))
    }
}
