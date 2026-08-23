package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApkIdentityTest {
    private val artifact = UpdateArtifact(
        "app.one",
        RemoteReleasedSource.Fdroid,
        "https://f-droid.org/repo/app.one_1.apk",
        sha256 = "039058c6f2c0cb492c533b0a4d14ef77cc0f78abccced5287d84a1a2011cfb81",
        nativeCodes = setOf("arm64-v8a"),
    )
    private val inspect = ApkInspect("app.one", setOf("aa"), setOf("arm64-v8a"))
    private val installed = InstalledIdentity("app.one", setOf("aa"), setOf("arm64-v8a"))

    @Test
    fun digestIsSha256Hex() {
        assertEquals("039058c6f2c0cb492c533b0a4d14ef77cc0f78abccced5287d84a1a2011cfb81", ApkIdentity.digest(byteArrayOf(1, 2, 3)))
    }

    @Test
    fun missingExpectedHashStillMatches() {
        assertTrue(ApkIdentity.hashesMatch(null, "abc"))
        assertFalse(ApkIdentity.hashesMatch("abc", "def"))
    }

    @Test
    fun identityNeedsPackageSignerAndAbi() {
        assertTrue(ApkIdentity.identityReady(artifact, inspect, installed))
        assertFalse(ApkIdentity.identityReady(artifact, inspect.copy(packageName = "app.other"), installed))
        assertFalse(ApkIdentity.identityReady(artifact, inspect.copy(signers = setOf("bb")), installed))
        assertFalse(ApkIdentity.identityReady(artifact, inspect, installed.copy(abis = setOf("x86"))))
        assertFalse(ApkIdentity.identityReady(artifact, inspect.copy(signers = emptySet()), installed))
    }

    @Test
    fun signersMatchAllowsUnknownInstalled() {
        assertTrue(ApkIdentity.signersMatch(setOf("aa"), null))
        assertTrue(ApkIdentity.signersMatch(setOf("aa"), emptySet()))
        assertTrue(ApkIdentity.signersMatch(setOf("aa"), setOf("aa", "bb")))
        assertFalse(ApkIdentity.signersMatch(setOf("aa"), setOf("bb")))
        assertFalse(ApkIdentity.signersMatch(emptySet(), setOf("aa")))
    }
}
