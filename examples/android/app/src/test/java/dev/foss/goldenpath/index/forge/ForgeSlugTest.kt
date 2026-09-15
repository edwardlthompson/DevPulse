package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ForgeSlugTest {
    @Test
    fun labelAndPackageMiddleMatchRepo() {
        assertTrue(ForgeSlug.matches("owner/continuum-calendar", "Continuum Calendar", "org.continuumcalendar.app"))
        assertFalse(ForgeSlug.matches("someone/WiseTimer", "Wipe Files", "com.example.wipefiles"))
    }

    @Test
    fun shortRepoNameDoesNotMatch() {
        assertFalse(ForgeSlug.matches("a/app", "App", "org.app"))
    }
}

class GitHubLeftoverNoiseTest {
    @Test
    fun skipsWebApkAndSelf() {
        assertTrue(GitHubLeftoverNoise.skip("app.devpulse"))
        assertTrue(GitHubLeftoverNoise.skip("org.chromium.webapk.aeac9f541e8d31baa_v2"))
        assertFalse(GitHubLeftoverNoise.skip("org.continuumcalendar.app"))
    }
}

class GithubPackageEncodedTest {
    @Test
    fun ioGithubBecomesOwnerRepo() {
        assertEquals("CyberTimon/RapidRAW", PackageIdAliases.encoded("io.github.CyberTimon.RapidRAW"))
        assertEquals("CyberTimon/RapidRAW", PackageIdAliases.hint("io.github.CyberTimon.RapidRAW", emptyMap())?.ownerRepo)
        assertNull(PackageIdAliases.encoded("org.continuumcalendar.app"))
    }
}
