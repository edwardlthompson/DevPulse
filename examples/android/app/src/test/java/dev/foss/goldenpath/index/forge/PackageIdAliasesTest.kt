package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PackageIdAliasesTest {
    @Test
    fun obtainiumFdroidAliasHasNoSiblingVersion() {
        val library = mapOf(
            "dev.imranr.obtainium.fdroid" to GithubHint("ImranR98/Obtainium", 9L, "1.4.3"),
        )
        val hint = PackageIdAliases.hint("dev.imranr.obtainium", library)
        assertEquals("ImranR98/Obtainium", hint?.ownerRepo)
        assertNull(hint?.ms)
        assertNull(hint?.versionName)
    }

    @Test
    fun firefoxDoesNotStripLastSegment() {
        val library = mapOf("org.mozilla" to GithubHint("mozilla/gecko"))
        assertNull(PackageIdAliases.hint("org.mozilla.firefox", library))
        assertEquals(null, PackageIdAliases.strip("org.mozilla.firefox"))
        assertTrue(PackageIdAliases.keys("org.mozilla.firefox").none { it == "org.mozilla" })
    }

    @Test
    fun curatedHintsResolveObdforgeAndObtainium() {
        val hint = PackageIdAliases.hint("dev.foss.obdforge", emptyMap())
        assertEquals("edwardlthompson/OBDForge", hint?.ownerRepo)
    }

    @Test
    fun conflictingSuffixReposDoNotAutoBind() {
        val library = mapOf(
            "org.app.fdroid" to GithubHint("one/repo"),
            "org.app.debug" to GithubHint("two/other"),
        )
        assertNull(PackageIdAliases.hint("org.app", library))
    }

    @Test
    fun expandFillsWantedWithoutClobberingExact() {
        val library = mapOf(
            "dev.imranr.obtainium.fdroid" to GithubHint("ImranR98/Obtainium", 1L, "1.0"),
            "org.exact" to GithubHint("exact/app", 2L, "2.0"),
        )
        val out = PackageIdAliases.expand(
            setOf("dev.imranr.obtainium", "org.exact"),
            library,
        )
        assertEquals("2.0", out["org.exact"]?.versionName)
        assertNull(out["dev.imranr.obtainium"]?.versionName)
        assertEquals("ImranR98/Obtainium", out["dev.imranr.obtainium"]?.ownerRepo)
    }
}
