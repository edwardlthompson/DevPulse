package dev.foss.goldenpath.query

import dev.foss.goldenpath.inventory.AppOrigin
import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.scan.ScanItem
import dev.foss.goldenpath.staleness.Badge
import dev.foss.goldenpath.staleness.StalenessResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanQueryEngineTest {
    private val red = item("app.red", Badge.Red)
    private val green = item("app.green", Badge.Green)

    @Test
    fun pinHidesRedFromDefaultQuery() {
        val visible = ScanQueryEngine.filter(listOf(red, green), setOf("app.red"), ScanQuery())
        assertEquals(listOf("app.green"), visible.map { it.app.packageName })
        assertTrue(PinRules.hideFromRedList("app.red", Badge.Red, setOf("app.red")))
        assertFalse(PinRules.hideFromRedList("app.green", Badge.Green, setOf("app.green")))
    }

    @Test
    fun exportCsvAndHistoryCounts() {
        val csv = ScanExport.toCsv(listOf(red))
        assertTrue(csv.contains("app.red"))
        assertTrue(ScanExport.toJson(listOf(red)).contains("Red"))
        val history = ScanHistory.entry(10L, listOf(red, green))
        assertEquals(1, history.redCount)
        assertEquals(0, history.unknownCount)
    }

    private fun item(packageName: String, badge: Badge): ScanItem = ScanItem(
        app = InstalledApp(
            packageName = packageName,
            label = packageName,
            versionName = "1",
            versionCode = 1L,
            lastUpdateTimeMs = 1L,
            firstInstallTimeMs = 1L,
            minSdk = 26,
            targetSdk = 37,
            isSystemApp = false,
            origin = AppOrigin.Unknown,
        ),
        staleness = StalenessResult(null, if (badge == Badge.Red) 400 else 10, badge, 1L, false),
        repoFound = false,
    )
}
