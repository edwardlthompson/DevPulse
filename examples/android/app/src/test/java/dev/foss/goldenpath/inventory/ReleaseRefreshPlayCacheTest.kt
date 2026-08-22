package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.play.PlayCachePolicy
import dev.foss.goldenpath.index.play.PlayPageClient
import dev.foss.goldenpath.index.play.PlayPageResponse
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ReleaseRefreshPlayCacheTest {
    @Before
    fun reset() {
        RemoteReleaseMemory.clear()
    }

    @Test
    fun delistedMissSkipsHttpPastListedTtl() {
        val fetches = AtomicInteger(0)
        val now = 1_720_000_000_000L
        RemoteReleaseMemory.putAll(
            mapOf(
                "app.gone" to RemoteReleaseRollup.from(
                    listOf(
                        RemoteReleaseOffer(
                            RemoteReleasedSource.Play,
                            listed = false,
                            known = true,
                            fetchedAtMs = now,
                        ),
                    ),
                ),
            ),
        )
        val offer = ReleaseRefreshProbes.play(
            "app.gone",
            PlayPageClient {
                fetches.incrementAndGet()
                PlayPageResponse(200, "")
            },
            nowMs = now + PlayCachePolicy.TTL_MS + 1_000L,
        )
        assertEquals(0, fetches.get())
        assertEquals(false, offer.listed)
        assertEquals(true, offer.known)
    }

    @Test
    fun unknownPlayIsNotReused() {
        val fetches = AtomicInteger(0)
        val client = PlayPageClient {
            fetches.incrementAndGet()
            PlayPageResponse(403, "")
        }
        val first = ReleaseRefreshProbes.play("app.x", client, nowMs = 1_000L)
        val second = ReleaseRefreshProbes.play("app.x", client, nowMs = 2_000L)
        assertEquals(2, fetches.get())
        assertEquals(false, first.known)
        assertEquals(false, second.known)
    }
}
