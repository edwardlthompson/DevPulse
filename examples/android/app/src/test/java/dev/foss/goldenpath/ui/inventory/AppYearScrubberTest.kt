package dev.foss.goldenpath.ui.inventory

import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class AppYearScrubberTest {

    private fun makeApp(pkg: String, year: Int, remoteSource: RemoteReleasedSource = RemoteReleasedSource.Play): InstalledApp {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 15)
        }
        val ms = cal.timeInMillis
        return InstalledApp(
            packageName = pkg,
            label = "App $pkg",
            versionName = "1.0",
            versionCode = 100L,
            lastUpdateTimeMs = ms,
            firstInstallTimeMs = ms,
            minSdk = 26,
            targetSdk = 34,
            isSystemApp = false,
            remoteReleasedAtMs = ms,
            remoteReleasedSource = remoteSource,
        )
    }

    @Test
    fun extractYearFromApp() {
        val app2024 = makeApp("app.a", 2024)
        val app2021 = makeApp("app.b", 2021)
        assertEquals(2024, AppYearScrubber.appYear(app2024))
        assertEquals(2021, AppYearScrubber.appYear(app2021))
        assertEquals("2024", AppYearScrubber.yearLabel(app2024))
        assertEquals("2021", AppYearScrubber.yearLabel(app2021))
    }

    @Test
    fun fallbackToInstalledDateWhenRemoteSourceNone() {
        val app = makeApp("app.c", 2023, remoteSource = RemoteReleasedSource.None)
        assertEquals("2023", AppYearScrubber.yearLabel(app))
    }

    @Test
    fun findYearKeypointsAcrossSortedList() {
        val list = listOf(
            makeApp("app.1", 2026),
            makeApp("app.2", 2026),
            makeApp("app.3", 2025),
            makeApp("app.4", 2025),
            makeApp("app.5", 2024),
            makeApp("app.6", 2023),
        )
        val keypoints = AppYearScrubber.findYearKeypoints(list)
        assertEquals(4, keypoints.size)
        assertEquals("2026", keypoints[0].year)
        assertEquals(0, keypoints[0].index)
        assertEquals(0f, keypoints[0].fraction, 0.001f)

        assertEquals("2025", keypoints[1].year)
        assertEquals(2, keypoints[1].index)

        assertEquals("2024", keypoints[2].year)
        assertEquals(4, keypoints[2].index)

        assertEquals("2023", keypoints[3].year)
        assertEquals(5, keypoints[3].index)
        assertEquals(1f, keypoints[3].fraction, 0.001f)
    }

    @Test
    fun targetIndexForFractionCalculatesCorrectIndex() {
        assertEquals(0, AppYearScrubber.targetIndexForFraction(0f, 100))
        assertEquals(50, AppYearScrubber.targetIndexForFraction(0.5f, 101))
        assertEquals(99, AppYearScrubber.targetIndexForFraction(1f, 100))
        assertEquals(0, AppYearScrubber.targetIndexForFraction(-0.5f, 100))
        assertEquals(99, AppYearScrubber.targetIndexForFraction(1.5f, 100))
    }
}
