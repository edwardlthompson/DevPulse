package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Test

class InventorySortTest {
    private val now = 1_700_000_000_000L
    private val day = 86_400_000L
    private val old = dated("app.old", "Old", now - 400 * day)
    private val mid = dated("app.mid", "Mid", now - 10 * day)
    private val unknown = sampleApp(
        "app.unknown",
        "Unknown",
        installedAtMs = now - 5 * day,
    )
    private val remoteOld = sampleApp(
        packageName = "app.remote",
        label = "Remote",
        lastUpdateTimeMs = 31_536_000_000L,
        remoteReleasedAtMs = now - 200 * day,
        remoteReleasedSource = RemoteReleasedSource.Aptoide,
    )

    @Test
    fun oldestPutsUnknownLast() {
        val sorted = InventorySort.apply(listOf(mid, unknown, old), InventorySortMode.Oldest, nowMs = now)
        assertEquals(listOf("app.old", "app.mid", "app.unknown"), sorted.map { it.packageName })
    }

    @Test
    fun installDateAloneStillSortsAsUnknown() {
        val recentInstall = sampleApp("app.fresh", "Fresh", installedAtMs = now - 2 * day)
        val sorted = InventorySort.apply(listOf(recentInstall, mid, old), InventorySortMode.Oldest, nowMs = now)
        assertEquals(listOf("app.old", "app.mid", "app.fresh"), sorted.map { it.packageName })
    }

    @Test
    fun unknownsSortByInstallDate() {
        val olderInstall = sampleApp("app.oldunk", "OldUnk", installedAtMs = now - 80 * day)
        val newerInstall = sampleApp("app.newunk", "NewUnk", installedAtMs = now - 3 * day)
        val oldest = InventorySort.apply(
            listOf(newerInstall, mid, olderInstall, old),
            InventorySortMode.Oldest,
            nowMs = now,
        )
        assertEquals(
            listOf("app.old", "app.mid", "app.oldunk", "app.newunk"),
            oldest.map { it.packageName },
        )
        val newest = InventorySort.apply(
            listOf(olderInstall, old, newerInstall, mid),
            InventorySortMode.Newest,
            nowMs = now,
        )
        assertEquals(
            listOf("app.mid", "app.old", "app.newunk", "app.oldunk"),
            newest.map { it.packageName },
        )
    }

    @Test
    fun newestPutsUnknownLast() {
        val sorted = InventorySort.apply(listOf(old, unknown, mid), InventorySortMode.Newest, nowMs = now)
        assertEquals(listOf("app.mid", "app.old", "app.unknown"), sorted.map { it.packageName })
    }

    @Test
    fun remoteDateBeats1971Local() {
        val sorted = InventorySort.apply(listOf(mid, remoteOld), InventorySortMode.Oldest, nowMs = now)
        assertEquals(listOf("app.remote", "app.mid"), sorted.map { it.packageName })
    }

    @Test
    fun usedAndStaleRanksHighUseOldFirst() {
        val usage = mapOf(
            "app.old" to UsageSnapshot("app.old", now, 10 * 3_600_000L),
            "app.mid" to UsageSnapshot("app.mid", now, 20 * 3_600_000L),
        )
        val sorted = InventorySort.apply(
            listOf(mid, old),
            InventorySortMode.UsedAndStale,
            usage,
            now,
        )
        assertEquals(listOf("app.old", "app.mid"), sorted.map { it.packageName })
    }

    @Test
    fun usedAndStaleWithoutUsageFallsBackToOldest() {
        val sorted = InventorySort.apply(listOf(mid, old), InventorySortMode.UsedAndStale, emptyMap(), now)
        assertEquals(listOf("app.old", "app.mid"), sorted.map { it.packageName })
    }

    @Test
    fun nameSort() {
        val sorted = InventorySort.apply(listOf(old, mid), InventorySortMode.Name, nowMs = now)
        assertEquals(listOf("app.mid", "app.old"), sorted.map { it.packageName })
    }

    private fun dated(packageName: String, label: String, ms: Long) = sampleApp(
        packageName = packageName,
        label = label,
        installedAtMs = ms,
        remoteReleasedAtMs = ms,
        remoteReleasedSource = RemoteReleasedSource.Fdroid,
    )
}
