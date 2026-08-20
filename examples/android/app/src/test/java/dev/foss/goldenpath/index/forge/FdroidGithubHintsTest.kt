package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.index.fdroid.FdroidAppRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FdroidGithubHintsTest {
    @Test
    fun onlyGithubSourceCode() {
        val records = listOf(
            FdroidAppRecord(
                "uk.org.platitudes.wipefiles",
                1L,
                "https://github.com/plat/wipefiles",
                "official",
            ),
            FdroidAppRecord("org.example.app", 1L, "https://example.com/src", "official"),
            FdroidAppRecord("org.gitlab.app", 1L, "https://gitlab.com/g/p", "izzy"),
            FdroidAppRecord("org.other.app", 1L, null, "official"),
        )
        val wanted = setOf(
            "uk.org.platitudes.wipefiles",
            "org.example.app",
            "org.gitlab.app",
            "org.other.app",
        )
        val hints = FdroidGithubHints.map(records, wanted)
        assertEquals(mapOf("uk.org.platitudes.wipefiles" to "plat/wipefiles"), hints)
        assertTrue("example.com" !in hints.values.joinToString())
    }

    @Test
    fun officialBeatsIzzyWrongRepo() {
        val pkg = "uk.org.platitudes.wipefiles"
        val records = listOf(
            FdroidAppRecord(pkg, 9L, "https://github.com/other/wrong", "izzy", "9.9"),
            FdroidAppRecord(pkg, 1L, "https://github.com/plat/wipefiles", "official", "1.0"),
        )
        val hints = FdroidGithubHints.hints(records, setOf(pkg))
        assertEquals("plat/wipefiles", hints[pkg]?.ownerRepo)
        assertEquals(1L, hints[pkg]?.ms)
        assertEquals("1.0", hints[pkg]?.versionName)
        assertEquals("plat/wipefiles", FdroidGithubHints.map(records, setOf(pkg))[pkg])
    }

    @Test
    fun archiveHintWhenNoOfficialGithub() {
        val pkg = "org.archive.only"
        val records = listOf(
            FdroidAppRecord(pkg, 1L, null, "official"),
            FdroidAppRecord(pkg, 1L, "https://github.com/old/archiveapp", "archive"),
        )
        val hints = FdroidGithubHints.map(records, setOf(pkg))
        assertEquals(mapOf(pkg to "old/archiveapp"), hints)
    }

    @Test
    fun harvestKeepsWipeFilesOffNeighbor() {
        val raw = """
            {"apps":[
              {"sourceCode":"https://github.com/peterhearty/WipeFiles","packageName":"uk.org.platitudes.wipefiles","lastUpdated":1610000000000},
              {"sourceCode":"https://github.com/Foxek/WiseTimer","packageName":"com.foxek.simpletimer","lastUpdated":1620000000000}
            ]}
        """.trimIndent().toByteArray()
        val library = FdroidGithubHints.harvest(raw, "izzy")
        assertEquals("peterhearty/WipeFiles", library["uk.org.platitudes.wipefiles"]?.ownerRepo)
        assertEquals("Foxek/WiseTimer", library["com.foxek.simpletimer"]?.ownerRepo)
        assertEquals(1610000000000L, library["uk.org.platitudes.wipefiles"]?.ms)
    }

    @Test
    fun harvestArchiveGuardianCalyx() {
        val archive = FdroidGithubHints.harvest(
            """{"apps":[{"packageName":"org.old.app","sourceCode":"https://github.com/old/archiveapp"}]}""".toByteArray(),
            "archive",
        )
        val guardian = FdroidGithubHints.harvest(
            """{"apps":[{"packageName":"org.guardian.app","sourceCode":"https://github.com/guardianproject/foo"}]}""".toByteArray(),
            "guardian",
        )
        val calyx = FdroidGithubHints.harvest(
            """{"apps":[{"packageName":"org.calyx.app","sourceCode":"https://github.com/calyxos/bar"}]}""".toByteArray(),
            "calyx",
        )
        assertEquals("old/archiveapp", archive["org.old.app"]?.ownerRepo)
        assertEquals("guardianproject/foo", guardian["org.guardian.app"]?.ownerRepo)
        assertEquals("calyxos/bar", calyx["org.calyx.app"]?.ownerRepo)
    }

    @Test
    fun mergeLibraryOfficialBeatsIzzyAndKeepsGaps() {
        val merged = FdroidGithubHints.mergeLibrary(
            mapOf("org.persisted.app" to "keep/me"),
            listOf(
                "izzy" to mapOf(
                    "org.both.app" to GithubHint("izzy/wrong"),
                    "org.izzy.only" to GithubHint("izzy/only"),
                ),
                "official" to mapOf("org.both.app" to GithubHint("off/right")),
            ),
        )
        assertEquals("off/right", merged["org.both.app"]?.ownerRepo)
        assertEquals("izzy/only", merged["org.izzy.only"]?.ownerRepo)
        assertEquals("keep/me", merged["org.persisted.app"]?.ownerRepo)
    }
}
