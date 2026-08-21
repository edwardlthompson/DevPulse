package dev.foss.goldenpath.index.fdroid

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FdroidPackagePageTest {
    @Test
    fun addedOnAndGithub() {
        val html = """
            Version 1.23.2 (1023052) suggested Added on Feb 19, 2026
            <a href="https://github.com/f-droid/fdroidclient">Source</a>
        """.trimIndent()
        val page = FdroidPackagePage.parse(html)
        assertEquals(FdroidPackagePage.monthDayYear("Feb 19, 2026"), page.lastUpdatedMs)
        assertEquals("https://github.com/f-droid/fdroidclient", page.sourceCode)
    }

    @Test
    fun emptyHtml() {
        val page = FdroidPackagePage.parse("  ")
        assertNull(page.lastUpdatedMs)
        assertNull(page.sourceCode)
        assertNull(page.category)
        assertTrue(page.relatedPackages.isEmpty())
    }

    @Test
    fun categoryAndRelatedFromFixture() {
        val html = """
            Added on Feb 19, 2026
            <a href="/en/categories/Navigation">Navigation</a>
            <a href="/packages/org.maps.one/">One</a>
            <a href="/packages/org.self.app/">Self</a>
        """.trimIndent()
        val page = FdroidPackagePage.parse(html, "org.self.app")
        assertEquals("Navigation", page.category)
        assertEquals(listOf("org.maps.one"), page.relatedPackages)
    }

    @Test
    fun apkNamePrefersPackageFile() {
        val html = """
            <a href="https://apt.izzysoft.de/fdroid/repo/org.other_1.apk">other</a>
            <a href="/fdroid/repo/org.self.app_9.apk">self</a>
            <a href="../evil.apk">bad</a>
        """.trimIndent()
        assertEquals("org.self.app_9.apk", FdroidPackagePage.apkName(html, "org.self.app"))
        assertEquals("org.other_1.apk", FdroidPackagePage.parse(html).apkName)
    }
}
