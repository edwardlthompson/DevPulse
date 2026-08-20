package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RemoteReleaseCodecTest {
    @Test
    fun roundTripKeepsOffersDateAndVersion() {
        val pick = RemoteReleaseRollup.from(
            listOf(
                RemoteReleaseOffer(
                    source = RemoteReleasedSource.Fdroid,
                    ms = 1_700_000_000_000L,
                    versionName = "2.1.0",
                    pageUrl = "https://f-droid.org/packages/app.user/",
                ),
                RemoteReleaseOffer(
                    source = RemoteReleasedSource.Forge,
                    ms = 1_700_000_000_000L,
                    versionName = "2.1.0",
                    pageUrl = "https://github.com/user/app",
                ),
            ),
        )
        val decoded = RemoteReleaseCodec.decode(RemoteReleaseCodec.encode(mapOf("app.user" to pick)))
        assertEquals(pick, decoded.getValue("app.user"))
    }

    @Test
    fun oldFiveColumnRowsStillDecode() {
        val raw = "app.user\t1700000000000\tFdroid\t2.1.0\thttps://f-droid.org/packages/app.user/\n"
        val pick = RemoteReleaseCodec.decode(raw).getValue("app.user")
        assertEquals(1_700_000_000_000L, pick.ms)
        assertEquals(RemoteReleasedSource.Fdroid, pick.source)
        assertEquals("2.1.0", pick.versionName)
        assertEquals(1, pick.offers.size)
    }

    @Test
    fun unlistedFlagSurvivesRoundTrip() {
        val pick = RemoteReleaseRollup.from(
            listOf(
                RemoteReleaseOffer(RemoteReleasedSource.Play, 1L, "1.0", UpdateUrls.play("app.x"), listed = false),
                RemoteReleaseOffer(RemoteReleasedSource.Fdroid, 2L, "2.0", "https://f-droid.org/packages/app.x/"),
            ),
        )
        val decoded = RemoteReleaseCodec.decode(RemoteReleaseCodec.encode(mapOf("app.x" to pick)))
        assertFalse(decoded.getValue("app.x").offers.first { it.source == RemoteReleasedSource.Play }.listed)
        assertEquals("2.0", decoded.getValue("app.x").versionName)
    }

    @Test
    fun unknownPlayFlagSurvivesRoundTrip() {
        val pick = RemoteReleaseRollup.from(
            listOf(RemoteReleaseOffer(RemoteReleasedSource.Play, listed = false, known = false)),
        )
        val decoded = RemoteReleaseCodec.decode(
            RemoteReleaseCodec.encode(mapOf("com.instagram.android" to pick)),
        )
        val play = decoded.getValue("com.instagram.android").offers.single()
        assertFalse(play.listed)
        assertFalse(play.known)
    }

    @Test
    fun emptyAndJunkYieldEmpty() {
        assertEquals(emptyMap<String, RemoteReleasePick>(), RemoteReleaseCodec.decode(""))
        assertEquals(emptyMap<String, RemoteReleasePick>(), RemoteReleaseCodec.decode("not-a-row"))
    }
}
