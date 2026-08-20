package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ForgeUrlTest {
    @Test
    fun githubRepoBecomesReleasesPage() {
        assertEquals(
            "https://github.com/foo/bar/releases",
            ForgeUrl.downloadPage("https://github.com/foo/bar.git"),
        )
        assertEquals(
            "https://github.com/foo/bar/releases",
            ForgeUrl.downloadPage("https://github.com/foo/bar/issues"),
        )
    }

    @Test
    fun homepageAndOrgAreRejected() {
        assertNull(ForgeUrl.downloadPage("https://example.com/src"))
        assertNull(ForgeUrl.downloadPage("https://github.com/foo"))
        assertNull(ForgeUrl.downloadPage("https://github.com/orgs/foo"))
    }

    @Test
    fun gitlabAndCodebergBecomeReleases() {
        assertEquals(
            "https://gitlab.com/g/p/-/releases",
            ForgeUrl.downloadPage("https://gitlab.com/g/p"),
        )
        assertEquals(
            "https://codeberg.org/g/p/releases",
            ForgeUrl.downloadPage("https://codeberg.org/g/p"),
        )
    }
}
