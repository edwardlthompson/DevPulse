package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class InstalledDateResolverTest {
    private val now = 1_800_000_000_000L
    private val year1971 = 31_536_000_000L
    private val year2024 = 1_704_067_200_000L
    private val year2018 = 1_514_764_800_000L

    @Test
    fun rejectsZeroAnd1971() {
        assertFalse(InstalledDateResolver.isPlausible(0L, now))
        assertFalse(InstalledDateResolver.isPlausible(year1971, now))
        val resolved = InstalledDateResolver.resolve(year1971, 0L, 0L, now)
        assertNull(resolved.ms)
        assertEquals(InstalledDateSource.Unknown, resolved.source)
    }

    @Test
    fun firstInstallBeatsBogusLastUpdate() {
        val resolved = InstalledDateResolver.resolve(year1971, year2018, 0L, now)
        assertEquals(year2018, resolved.ms)
        assertEquals(InstalledDateSource.FirstInstall, resolved.source)
    }

    @Test
    fun apkMtimeWhenPackageManagerDatesAreJunk() {
        val resolved = InstalledDateResolver.resolve(0L, year1971, year2024, now)
        assertEquals(year2024, resolved.ms)
        assertEquals(InstalledDateSource.ApkMtime, resolved.source)
    }

    @Test
    fun futureTimestampIsUnknown() {
        val future = now + 10 * InstalledDateResolver.FUTURE_SLACK_MS
        val resolved = InstalledDateResolver.resolve(future, future, future, now)
        assertNull(resolved.ms)
        assertEquals(InstalledDateSource.Unknown, resolved.source)
    }

    @Test
    fun newestPlausibleWins() {
        val resolved = InstalledDateResolver.resolve(year2018, year2024, year2018, now)
        assertEquals(year2024, resolved.ms)
        assertEquals(InstalledDateSource.FirstInstall, resolved.source)
    }
}
