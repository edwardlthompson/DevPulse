package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.inventory.InstalledApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ForgeLibraryMatchTest {
    @Test
    fun exactBeatsSuffix() {
        val installed = listOf(app("dev.imranr.obtainium"))
        val library = mapOf(
            "dev.imranr.obtainium" to "ImranR98/Obtainium",
            "dev.imranr.obtainium.fdroid" to "other/fork",
        )
        val matches = ForgeLibraryMatch.bind("ImranR98/Obtainium", installed, library)
        assertEquals(LibraryMatchRank.ExactPackage, matches.single().rank)
    }

    @Test
    fun suffixVariantMatchesFdroidFlavor() {
        val installed = listOf(app("dev.imranr.obtainium"))
        val library = mapOf("dev.imranr.obtainium.fdroid" to "ImranR98/Obtainium")
        val matches = ForgeLibraryMatch.autoBind(
            ForgeLibraryMatch.bind("ImranR98/Obtainium", installed, library),
        )
        assertEquals("dev.imranr.obtainium", matches.single().packageName)
        assertEquals(LibraryMatchRank.SuffixVariant, matches.single().rank)
    }

    @Test
    fun unmatchedStaysEmptyAndEvidenceNeedsHaystack() {
        val installed = listOf(app("org.app"))
        assertTrue(ForgeLibraryMatch.bind("acme/missing", installed, emptyMap()).isEmpty())
        val evidence = ForgeLibraryMatch.bind(
            "acme/app",
            installed,
            emptyMap(),
            releaseHaystack = "org.app release",
        )
        assertEquals(LibraryMatchRank.ReleaseEvidence, evidence.single().rank)
        assertTrue(ForgeLibraryMatch.autoBind(evidence).isEmpty())
    }

    private fun app(pkg: String) = InstalledApp(
        packageName = pkg,
        label = pkg,
        versionName = "1.0",
        versionCode = 1L,
        lastUpdateTimeMs = 1L,
        firstInstallTimeMs = 1L,
        minSdk = 26,
        targetSdk = 37,
        isSystemApp = false,
    )
}
