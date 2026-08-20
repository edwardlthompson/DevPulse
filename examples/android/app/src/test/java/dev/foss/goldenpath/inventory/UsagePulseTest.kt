package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UsagePulseTest {
    private val now = 1_700_000_000_000L
    private val day = 86_400_000L

    @Test
    fun unknownAgeScoresZero() {
        val app = sampleApp("app.x", "X", lastUpdateTimeMs = 31_536_000_000L)
        val snapshot = UsageSnapshot("app.x", now, 8 * 3_600_000L)
        assertNull(UsagePulse.ageDays(app, now))
        assertEquals(0.0, UsagePulse.score(app, snapshot, now), 0.01)
    }

    @Test
    fun usesRemoteAgeWhenPresent() {
        val app = sampleApp(
            packageName = "app.x",
            label = "X",
            lastUpdateTimeMs = 31_536_000_000L,
            remoteReleasedAtMs = now - 10 * day,
            remoteReleasedSource = RemoteReleasedSource.Fdroid,
        )
        val snapshot = UsageSnapshot("app.x", now, 2 * 3_600_000L)
        assertEquals(10.0, UsagePulse.ageDays(app, now)!!, 0.01)
        assertEquals(20.0, UsagePulse.score(app, snapshot, now), 0.01)
    }

    @Test
    fun unusedOldSinks() {
        val app = sampleApp("app.x", "X", installedAtMs = now - 400 * day)
        assertEquals(0.0, UsagePulse.score(app, null, now), 0.01)
    }
}
