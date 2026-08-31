package dev.foss.goldenpath.inventory

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.foss.goldenpath.clearPreferenceDataStores
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class InventoryPreferencesTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun resetDataStore() {
        context.clearPreferenceDataStores()
    }

    @Test
    fun defaultsBlockScanAndHideSystemApps() = runBlocking {
        val prefs = InventoryPreferences(context)
        assertFalse(prefs.queryAllPackagesAcknowledged.first())
        assertFalse(prefs.includeSystemApps.first())
        assertEquals(UsageStatsConsent.NotOffered, prefs.usageStatsConsent.first())
        assertEquals(InventorySortMode.Oldest, prefs.sortMode.first())
        assertFalse(prefs.staleOnly.first())
        assertFalse(prefs.updatesOnly.first())
        assertTrue(prefs.sourceFilters.first().isEmpty())
        assertFalse(prefs.aptoideLookupEnabled.first())
        assertFalse(prefs.apkMirrorLookupEnabled.first())
        assertFalse(prefs.apkPureLookupEnabled.first())
        assertTrue(prefs.playLookupEnabled.first())
        assertTrue(prefs.auroraPlayEnabled.first())
        assertTrue(prefs.forgeLookupEnabled.first())
        assertFalse(prefs.forgeLookupSearchUnknowns.first())
        assertEquals(ScanInterval.OnDemand, prefs.scanInterval.first())
        assertEquals(null, prefs.lastScanAtMs.first())
        assertEquals(InstallMethod.System, prefs.installMethod.first())
        assertFalse(prefs.updatePrefetchEnabled.first())
    }

    @Test
    fun persistsAcknowledgeAndSystemToggle() = runBlocking {
        val prefs = InventoryPreferences(context)
        prefs.setQueryAllPackagesAcknowledged(true)
        prefs.setIncludeSystemApps(true)
        prefs.setUsageStatsConsent(UsageStatsConsent.WalkthroughSeen)
        prefs.setSortMode(InventorySortMode.Newest)
        prefs.setStaleOnly(true)
        prefs.setUpdatesOnly(true)
        prefs.setSourceFilters(setOf(RemoteReleasedSource.Guardian, RemoteReleasedSource.Forge))
        prefs.setAptoideLookupEnabled(true)
        prefs.setApkMirrorLookupEnabled(true)
        prefs.setApkPureLookupEnabled(true)
        prefs.setPlayLookupEnabled(false)
        prefs.setAuroraPlayEnabled(true)
        prefs.setForgeLookupEnabled(false)
        prefs.setForgeLookupSearchUnknowns(true)
        prefs.setScanInterval(ScanInterval.Weekly)
        prefs.setLastScanAtMs(99L)
        prefs.setInstallMethod(InstallMethod.Root)
        prefs.setUpdatePrefetchEnabled(true)
        assertTrue(prefs.queryAllPackagesAcknowledged.first())
        assertTrue(prefs.includeSystemApps.first())
        assertEquals(UsageStatsConsent.WalkthroughSeen, prefs.usageStatsConsent.first())
        assertEquals(InventorySortMode.Newest, prefs.sortMode.first())
        assertTrue(prefs.staleOnly.first())
        assertTrue(prefs.updatesOnly.first())
        assertEquals(setOf(RemoteReleasedSource.Guardian, RemoteReleasedSource.Forge), prefs.sourceFilters.first())
        assertTrue(prefs.aptoideLookupEnabled.first())
        assertTrue(prefs.apkMirrorLookupEnabled.first())
        assertTrue(prefs.apkPureLookupEnabled.first())
        assertFalse(prefs.playLookupEnabled.first())
        assertTrue(prefs.auroraPlayEnabled.first())
        assertFalse(prefs.forgeLookupEnabled.first())
        assertTrue(prefs.forgeLookupSearchUnknowns.first())
        assertEquals(ScanInterval.Weekly, prefs.scanInterval.first())
        assertEquals(99L, prefs.lastScanAtMs.first())
        assertEquals(InstallMethod.Root, prefs.installMethod.first())
        assertTrue(prefs.updatePrefetchEnabled.first())
    }
}
