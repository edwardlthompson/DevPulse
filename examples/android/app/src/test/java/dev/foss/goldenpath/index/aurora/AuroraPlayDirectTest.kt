package dev.foss.goldenpath.index.aurora

import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.UpdateArtifactMemory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuroraPlayDirectTest {
    @Before
    fun reset() {
        UpdateArtifactMemory.clear()
    }

    @Test
    fun emptyPackageIsNull() {
        assertNull(AuroraPlayDirect.resolve("  ", AuroraPlayFiles { emptyList() }))
    }

    @Test
    fun blankOrRejectedUrlIsNull() {
        assertNull(AuroraPlayDirect.resolve("app.one", AuroraPlayFiles { listOf(AuroraPlayFile("")) }))
        assertNull(
            AuroraPlayDirect.resolve(
                "app.one",
                AuroraPlayFiles { listOf(AuroraPlayFile("ftp://evil.example/app.apk")) },
            ),
        )
    }

    @Test
    fun playCdnBecomesArtifact() {
        val artifact = AuroraPlayDirect.resolve(
            "app.one",
            AuroraPlayFiles {
                listOf(AuroraPlayFile("https://redirector.gvt1.com/edgedl/android/market/app.one", "2.0", 20))
            },
        )
        assertEquals("app.one", artifact?.packageName)
        assertEquals(RemoteReleasedSource.Play, artifact?.source)
        assertEquals("https://redirector.gvt1.com/edgedl/android/market/app.one", artifact?.downloadUrl)
        assertEquals("2.0", artifact?.versionName)
        assertEquals(20L, artifact?.versionCode)
    }

    @Test
    fun prefersBaseOverSplit() {
        val artifact = AuroraPlayDirect.resolve(
            "app.one",
            AuroraPlayFiles {
                listOf(
                    AuroraPlayFile("https://redirector.gvt1.com/edgedl/android/market/split", base = false),
                    AuroraPlayFile("https://redirector.gvt1.com/edgedl/android/market/base", "2.0", 20, base = true),
                )
            },
        )
        assertEquals("https://redirector.gvt1.com/edgedl/android/market/base", artifact?.downloadUrl)
    }
}

class AuroraAuthTest {
    @Test
    fun emailOfIgnoresBlankAndMissing() {
        assertNull(AuroraAuth.emailOf(""))
        assertNull(AuroraAuth.emailOf("{}"))
        assertEquals("anon@example.com", AuroraAuth.emailOf("""{"email":"anon@example.com","authToken":"x"}"""))
        assertNull(AuroraAuth.parse("{}"))
        assertNull(AuroraAuth.parse(""))
    }
}
