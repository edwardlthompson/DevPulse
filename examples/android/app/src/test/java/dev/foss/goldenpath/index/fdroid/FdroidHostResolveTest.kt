package dev.foss.goldenpath.index.fdroid

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FdroidHostResolveTest {
    @Test
    fun mergesApiVersionAndPageDate() {
        val rec = FdroidHostResolve.record(
            "org.fdroid.fdroid",
            "official",
            """{"packageName":"org.fdroid.fdroid","suggestedVersionCode":1,"packages":[{"versionName":"1.0","versionCode":1}]}""",
            """Added on Jan 02, 2024 <a href="https://github.com/f-droid/fdroidclient">src</a>
               <a href="https://f-droid.org/repo/org.fdroid.fdroid_1.apk">apk</a>""",
        )
        assertEquals("1.0", rec?.suggestedVersionName)
        assertEquals("https://github.com/f-droid/fdroidclient", rec?.sourceCode)
        assertEquals(FdroidPackagePage.monthDayYear("Jan 02, 2024"), rec?.lastUpdatedMs)
        assertEquals("org.fdroid.fdroid_1.apk", rec?.apkName)
    }

    @Test
    fun missingApiIsNull() {
        assertNull(FdroidHostResolve.record("org.x", "official", null, "<html>"))
    }

    @Test
    fun hostResolveOfficialAndArchiveOnly() {
        val official = FdroidRepoCatalog.defaults().first { it.id == "official" }
        val izzy = FdroidRepoCatalog.defaults().first { it.id == "izzy" }
        val guardian = FdroidRepoCatalog.defaults().first { it.id == "guardian" }
        assertTrue(FdroidIndexBudget.hostResolve(official))
        assertTrue(!FdroidIndexBudget.hostResolve(izzy))
        assertTrue(FdroidIndexBudget.extraHostResolve(izzy))
        assertTrue(!FdroidIndexBudget.extraHostResolve(guardian))
    }

    @Test
    fun skipArchiveWhenOfficialOn() {
        val official = FdroidRepoCatalog.defaults().first { it.id == "official" }
        val archive = FdroidRepoCatalog.defaults().first { it.id == "archive" }
        assertTrue(FdroidIndexBudget.skipArchiveHost(true, archive))
        assertTrue(!FdroidIndexBudget.skipArchiveHost(false, archive))
        assertTrue(!FdroidIndexBudget.skipArchiveHost(true, official))
        assertTrue(!FdroidIndexBudget.extraHostAllowed(388))
        assertTrue(FdroidIndexBudget.extraHostAllowed(39))
        assertTrue(FdroidIndexBudget.extraHostAllowed(1))
    }

    @Test
    fun pageOnlyIzzyRecord() {
        val rec = FdroidHostResolve.pageRecord(
            "org.maps",
            "izzy",
            """Added on Jan 02, 2024 <a href="/categories/Maps">Maps</a>
               <a href="/packages/org.other/">o</a>
               <a href="https://apt.izzysoft.de/fdroid/repo/org.maps_2.apk">apk</a>""",
        )
        assertEquals("Maps", rec?.category)
        assertEquals(listOf("org.other"), rec?.relatedPackages)
        assertEquals("org.maps_2.apk", rec?.apkName)
        assertEquals("https://apt.izzysoft.de/fdroid/repo/org.maps_2.apk", FdroidApkUrl.of("izzy", rec?.apkName))
    }
}
