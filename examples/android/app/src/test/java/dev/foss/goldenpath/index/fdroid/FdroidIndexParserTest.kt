package dev.foss.goldenpath.index.fdroid

import dev.foss.goldenpath.inventory.AppOrigin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FdroidIndexParserTest {
    private val fixture = """
        {"apps":[{"packageName":"org.example.app","lastUpdated":1700000000000,"sourceCode":"https://example.com/src"}]}
    """.trimIndent()

    @Test
    fun parsesLastUpdatedAndSourceCode() {
        val records = FdroidIndexParser.parse(fixture, "official")
        val hit = FdroidLookupEngine.lookup("org.example.app", records)
        assertEquals("org.example.app", hit?.packageName)
        assertEquals(1_700_000_000_000L, hit?.lastUpdatedMs)
        assertEquals("https://example.com/src", hit?.sourceCode)
        assertNull(FdroidLookupEngine.lookup("missing.app", records))
    }

    @Test
    fun parsesWhatsNew() {
        val raw = """{"apps":[{"packageName":"org.ver","localized":{"en-US":{"whatsNew":"Crash fix"}},"lastUpdated":1700000000000}]}"""
        val hit = FdroidLookupEngine.lookup("org.ver", FdroidIndexParser.parse(raw, "official"))
        assertEquals("Crash fix", hit?.whatsNew)
    }

    @Test
    fun parsesSuggestedVersionName() {
        val raw = """{"apps":[{"packageName":"org.ver","suggestedVersionName":"2.4.1","lastUpdated":1700000000000}]}"""
        val hit = FdroidLookupEngine.lookup("org.ver", FdroidIndexParser.parse(raw, "official"))
        assertEquals("2.4.1", hit?.suggestedVersionName)
    }

    @Test
    fun packagesHighestVersionBeatsSuggested() {
        val raw = """
            {"apps":[{"packageName":"org.ver","suggestedVersionName":"1.0","lastUpdated":1700000000000}],
             "packages":{"org.ver":[{"versionName":"1.0","versionCode":1},{"versionCode":32,"versionName":"3.2.1"}]}}
        """.trimIndent()
        val hit = FdroidLookupEngine.lookup("org.ver", FdroidIndexParser.parse(raw, "official"))
        assertEquals("3.2.1", hit?.suggestedVersionName)
    }

    @Test
    fun wantedParseSkipsOtherApps() {
        val raw = """
            {"apps":[
              {"packageName":"org.keep","lastUpdated":1700000000000,"suggestedVersionName":"2.0"},
              {"packageName":"org.skip","lastUpdated":1600000000000,"suggestedVersionName":"9.9"}
            ],"packages":{"org.keep":[{"versionName":"2.1","versionCode":21}],"org.skip":[{"versionName":"9.9","versionCode":99}]}}
        """.trimIndent()
        val records = FdroidIndexParser.parse(raw, "official", setOf("org.keep"))
        assertEquals(1, records.size)
        assertEquals("org.keep", records.single().packageName)
        assertEquals("2.1", records.single().suggestedVersionName)
    }

    @Test
    fun wantedParseFindsSpacedPackageKey() {
        val raw = """{"apps":[{ "packageName" : "org.keep", "lastUpdated": 1700000000000 }]}"""
        val records = FdroidIndexParser.parse(raw, "official", setOf("org.keep"))
        assertEquals("org.keep", records.single().packageName)
        assertEquals(1_700_000_000_000L, records.single().lastUpdatedMs)
    }

    @Test
    fun sourceCodeBeforePackageNameStaysOnSameApp() {
        val raw = """
            {"apps":[{"sourceCode":"https://github.com/peterhearty/WipeFiles","packageName":"uk.org.platitudes.wipefiles","lastUpdated":1700000000000}]}
        """.trimIndent()
        val full = FdroidLookupEngine.lookup("uk.org.platitudes.wipefiles", FdroidIndexParser.parse(raw, "izzy"))
        val wanted = FdroidIndexParser.parse(raw, "izzy", setOf("uk.org.platitudes.wipefiles")).single()
        assertEquals("https://github.com/peterhearty/WipeFiles", full?.sourceCode)
        assertEquals("https://github.com/peterhearty/WipeFiles", wanted.sourceCode)
    }

    @Test
    fun sourceCodeBeforePackageNameDoesNotLeakNextApp() {
        val raw = """
            {"apps":[
              {"sourceCode":"https://github.com/peterhearty/WipeFiles","packageName":"uk.org.platitudes.wipefiles","lastUpdated":1610000000000},
              {"sourceCode":"https://github.com/Foxek/WiseTimer","packageName":"com.foxek.simpletimer","lastUpdated":1620000000000}
            ]}
        """.trimIndent()
        val wipe = "uk.org.platitudes.wipefiles"
        val wise = "com.foxek.simpletimer"
        val full = FdroidIndexParser.parse(raw, "izzy")
        assertEquals("https://github.com/peterhearty/WipeFiles", FdroidLookupEngine.lookup(wipe, full)?.sourceCode)
        assertEquals("https://github.com/Foxek/WiseTimer", FdroidLookupEngine.lookup(wise, full)?.sourceCode)
        val wantedBoth = FdroidIndexParser.parse(raw, "izzy", setOf(wipe, wise))
        assertEquals("https://github.com/peterhearty/WipeFiles", FdroidLookupEngine.lookup(wipe, wantedBoth)?.sourceCode)
        assertEquals("https://github.com/Foxek/WiseTimer", FdroidLookupEngine.lookup(wise, wantedBoth)?.sourceCode)
        val wantedWipe = FdroidIndexParser.parse(raw, "izzy", setOf(wipe)).single()
        assertEquals("https://github.com/peterhearty/WipeFiles", wantedWipe.sourceCode)
        assertFalse(wantedWipe.sourceCode.orEmpty().contains("WiseTimer"))
    }

    @Test
    fun parsesNestedLocalizedName() {
        val nested = """{"apps":[{"packageName":"org.nested","name":{"en-US":"Hi"},"lastUpdated":1700000000000}]}"""
        val hit = FdroidLookupEngine.lookup("org.nested", FdroidIndexParser.parse(nested, "official"))
        assertEquals(1_700_000_000_000L, hit?.lastUpdatedMs)
    }

    @Test
    fun badJsonYieldsEmpty() {
        assertTrue(FdroidIndexParser.parse("{", "official").isEmpty())
    }

    @Test
    fun cacheTtlAndOrigin() {
        assertTrue(FdroidCachePolicy.isFresh(10L, 10L + FdroidCachePolicy.TTL_MS - 1))
        assertFalse(FdroidCachePolicy.isFresh(10L, 10L + FdroidCachePolicy.TTL_MS))
        assertEquals(AppOrigin.Fdroid, FdroidOrigin.from(FdroidRepoKind.Official))
        assertEquals(AppOrigin.ExtraRepo, FdroidOrigin.from(FdroidRepoKind.Izzy))
    }
}
