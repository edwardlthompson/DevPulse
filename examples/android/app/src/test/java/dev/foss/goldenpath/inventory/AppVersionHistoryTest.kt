package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.fdroid.FdroidPackageVersions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppVersionHistoryTest {
    @Test
    fun resolveStateDistinguishesInstalledNewerRollback() {
        assertEquals(AppVersionState.Current, AppVersionHistory.resolveState("1.2.0", 120, "1.2.0", 120))
        assertEquals(AppVersionState.Current, AppVersionHistory.resolveState("v1.2.0", null, "1.2.0", 120))
        assertEquals(AppVersionState.Newer, AppVersionHistory.resolveState("1.3.0", 130, "1.2.0", 120))
        assertEquals(AppVersionState.Rollback, AppVersionHistory.resolveState("1.1.0", 110, "1.2.0", 120))
        assertEquals(AppVersionState.Rollback, AppVersionHistory.resolveState("1.0.5", null, "1.2.0", 120))
    }

    @Test
    fun rankAndCapDeduplicatesSortsAndCapsAt5() {
        val items = listOf(
            AppVersionItem("1.0.0", 100, 1000L, RemoteReleasedSource.Fdroid, "https://f-droid.org/1.apk", AppVersionState.Rollback),
            AppVersionItem("1.1.0", 110, 2000L, RemoteReleasedSource.Fdroid, "https://f-droid.org/2.apk", AppVersionState.Rollback),
            AppVersionItem("1.2.0", 120, 3000L, RemoteReleasedSource.Fdroid, "https://f-droid.org/3.apk", AppVersionState.Current),
            AppVersionItem("1.3.0", 130, 4000L, RemoteReleasedSource.Fdroid, "https://f-droid.org/4.apk", AppVersionState.Newer),
            AppVersionItem("1.4.0", 140, 5000L, RemoteReleasedSource.Fdroid, "https://f-droid.org/5.apk", AppVersionState.Newer),
            AppVersionItem("1.5.0", 150, 6000L, RemoteReleasedSource.Fdroid, "https://f-droid.org/6.apk", AppVersionState.Newer),
            AppVersionItem("v1.2.0", null, 3000L, RemoteReleasedSource.Forge, "https://github.com/apk", AppVersionState.Current),
        )

        val capped = AppVersionHistory.rankAndCap(items, installedVersion = "1.2.0", installedCode = 120, maxCount = 5)
        assertEquals(5, capped.size)
        assertEquals("1.5.0", capped[0].versionName)
        assertEquals(AppVersionState.Newer, capped[0].state)
        assertEquals("1.4.0", capped[1].versionName)
        assertEquals("1.3.0", capped[2].versionName)
        assertEquals("1.2.0", capped[3].versionName)
        assertEquals(AppVersionState.Current, capped[3].state)
        assertEquals("1.1.0", capped[4].versionName)
        assertEquals(AppVersionState.Rollback, capped[4].state)
    }

    @Test
    fun fdroidPackageVersionsAllInParsesMultipleVersions() {
        val jsonArray = """
            [
              {"versionName":"2.1.0","versionCode":21,"apkName":"app_21.apk","added":1700000000000,"hash":"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"},
              {"versionName":"2.0.0","versionCode":20,"apkName":"app_20.apk","added":1690000000000,"hash":"bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"},
              {"versionName":"1.9.0","versionCode":19,"apkName":"app_19.apk","added":1680000000000}
            ]
        """.trimIndent()

        val parsed = FdroidPackageVersions.allIn(jsonArray)
        assertEquals(3, parsed.size)
        assertEquals("2.1.0", parsed[0].versionName)
        assertEquals(21L, parsed[0].versionCode)
        assertEquals("app_21.apk", parsed[0].apkName)
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", parsed[0].sha256)
        assertEquals("2.0.0", parsed[1].versionName)
        assertEquals(20L, parsed[1].versionCode)
        assertEquals("1.9.0", parsed[2].versionName)
    }
}
