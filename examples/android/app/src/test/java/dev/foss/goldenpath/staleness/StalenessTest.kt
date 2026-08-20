package dev.foss.goldenpath.staleness

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StalenessTest {
    private val now = 1_700_000_000_000L

    @Test
    fun recentRemoteIsGreen() {
        val result = Staleness.evaluate(datedPlay(daysAgo = 10), now)
        assertEquals(Badge.Green, result.badge)
        assertEquals(10, result.daysSinceActivity)
    }

    @Test
    fun amberAndRedBoundaries() {
        assertEquals(Badge.Amber, Staleness.evaluate(datedPlay(daysAgo = 180), now).badge)
        assertEquals(Badge.Amber, Staleness.evaluate(datedPlay(daysAgo = 365), now).badge)
        assertEquals(Badge.Red, Staleness.evaluate(datedPlay(daysAgo = 366), now).badge)
        assertEquals(Badge.Green, Staleness.evaluate(datedPlay(daysAgo = 179), now).badge)
    }

    @Test
    fun installedTimeDoesNotPaintGreenWhenRemotesMissing() {
        val input = StalenessInput(
            remotes = listOf(RemoteSignal(RemoteSource.Play, RemoteLookup.SuccessMissing)),
            installedLastUpdateMs = now - Staleness.MS_PER_DAY,
            targetSdk = 37,
        )
        val result = Staleness.evaluate(input, now)
        assertEquals(Badge.Red, result.badge)
        assertEquals(now - Staleness.MS_PER_DAY, result.installedLastUpdateMs)
        assertNull(result.newestRemoteActivityMs)
    }

    @Test
    fun failedLookupIsUnknownNotRed() {
        val input = StalenessInput(
            remotes = listOf(RemoteSignal(RemoteSource.Play, RemoteLookup.Failed)),
            installedLastUpdateMs = now,
            targetSdk = 37,
        )
        assertEquals(Badge.Unknown, Staleness.evaluate(input, now).badge)
    }

    @Test
    fun failedPlusMissingStaysUnknown() {
        val input = StalenessInput(
            remotes = listOf(
                RemoteSignal(RemoteSource.Play, RemoteLookup.Failed),
                RemoteSignal(RemoteSource.Fdroid, RemoteLookup.SuccessMissing),
            ),
            installedLastUpdateMs = now,
            targetSdk = 37,
        )
        assertEquals(Badge.Unknown, Staleness.evaluate(input, now).badge)
    }

    @Test
    fun archivedForgeDateDoesNotCount() {
        val input = StalenessInput(
            remotes = listOf(
                RemoteSignal(
                    source = RemoteSource.Forge,
                    lookup = RemoteLookup.SuccessDated,
                    activityAtMs = now - Staleness.MS_PER_DAY,
                    countsAsActivity = false,
                ),
            ),
            installedLastUpdateMs = now,
            targetSdk = 37,
        )
        assertEquals(Badge.Unknown, Staleness.evaluate(input, now).badge)
        assertNull(resultNewest(input))
    }

    @Test
    fun compatibilityWarningDoesNotChangeBadge() {
        val input = datedPlay(daysAgo = 10).copy(targetSdk = 33)
        val result = Staleness.evaluate(input, now)
        assertEquals(Badge.Green, result.badge)
        assertTrue(result.compatibilityWarning)
        assertFalse(Staleness.compatibilityWarning(34))
        assertTrue(Staleness.compatibilityWarning(33))
    }

    private fun resultNewest(input: StalenessInput): Long? = Staleness.evaluate(input, now).newestRemoteActivityMs

    private fun datedPlay(daysAgo: Int): StalenessInput = StalenessInput(
        remotes = listOf(
            RemoteSignal(
                source = RemoteSource.Play,
                lookup = RemoteLookup.SuccessDated,
                activityAtMs = now - daysAgo * Staleness.MS_PER_DAY,
            ),
        ),
        installedLastUpdateMs = now,
        targetSdk = 37,
    )
}
