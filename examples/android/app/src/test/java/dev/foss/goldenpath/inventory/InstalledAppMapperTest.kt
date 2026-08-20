package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class InstalledAppMapperTest {
    @Test
    fun mapsSnapshotAndResolvesSideloadOrigin() {
        val app = InstalledAppMapper.fromSnapshot(
            PackageSnapshot(
                packageName = "app.devpulse",
                label = "DevPulse",
                versionName = "0.1.0",
                versionCode = 1L,
                lastUpdateTimeMs = 1_700_000_000_002L,
                firstInstallTimeMs = 1_700_000_000_001L,
                apkLastModifiedMs = 0L,
                minSdk = 26,
                targetSdk = 37,
                isSystemApp = false,
            ),
        )
        assertEquals("app.devpulse", app.packageName)
        assertEquals("DevPulse", app.label)
        assertEquals("0.1.0", app.versionName)
        assertEquals(1L, app.versionCode)
        assertEquals(1_700_000_000_002L, app.lastUpdateTimeMs)
        assertEquals(1_700_000_000_002L, app.installedAtMs)
        assertEquals(InstalledDateSource.LastUpdate, app.installedAtSource)
        assertEquals(26, app.minSdk)
        assertEquals(37, app.targetSdk)
        assertFalse(app.isSystemApp)
        assertEquals(AppOrigin.SideloadedUnknown, app.origin)
    }

    @Test
    fun playInstallerIsPlayOrigin() {
        val app = InstalledAppMapper.fromSnapshot(
            PackageSnapshot(
                packageName = "com.example.play",
                label = "Play",
                versionName = "1",
                versionCode = 1L,
                lastUpdateTimeMs = 1_700_000_000_002L,
                firstInstallTimeMs = 1_700_000_000_001L,
                minSdk = 26,
                targetSdk = 37,
                isSystemApp = false,
                installerPackageName = "com.android.vending",
            ),
        )
        assertEquals(AppOrigin.Play, app.origin)
    }

    @Test
    fun fakeCatalogFeedsFilter() {
        val catalog = PackageCatalog {
            listOf(
                sampleApp("app.user", "User"),
                sampleApp("app.sys", "Sys", isSystemApp = true),
            )
        }
        val visible = InventoryFilter.visibleApps(catalog.listInstalled(), includeSystem = false)
        assertEquals(listOf("app.user"), visible.map { it.packageName })
    }
}
