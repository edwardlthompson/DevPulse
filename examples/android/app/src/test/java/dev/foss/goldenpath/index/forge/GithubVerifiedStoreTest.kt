package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GithubVerifiedStoreTest {
    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun persistAndReloadOneRow() {
        val file = tmp.newFile("github_verified.tsv")
        val store = FileGithubVerifiedStore(file)
        store.put("uk.org.platitudes.wipefiles", "plat/wipefiles")
        val reloaded = FileGithubVerifiedStore(file).load()
        assertEquals(mapOf("uk.org.platitudes.wipefiles" to "plat/wipefiles"), reloaded)
    }
}
