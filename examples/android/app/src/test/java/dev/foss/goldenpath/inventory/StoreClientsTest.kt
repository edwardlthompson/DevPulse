package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.apkpure.ApkPureLink
import dev.foss.goldenpath.index.aptoide.AptoideLink
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreClientsTest {
    @Test
    fun catalogCoversEveryScannedSource() {
        val ids = StoreClients.all().map { it.id }.toSet()
        assertTrue(ids.containsAll(enumValues<StoreClientId>().toSet()))
        val aptoide = StoreClients.all().first { it.id == StoreClientId.Aptoide }
        assertEquals(AptoideLink.STORE_PACKAGE, aptoide.packageName)
        assertTrue(aptoide.urls.any { it.url == AptoideLink.INSTALL_PAGE })
        val pure = StoreClients.all().first { it.id == StoreClientId.ApkPure }
        assertEquals(ApkPureLink.STORE_PACKAGE, pure.packageName)
    }

    @Test
    fun actionPrefersOpenWhenInstalledOrSiteOnly() {
        assertEquals(StoreClientAction.Open, StoreClients.action(installed = true, games = false, hasPackage = true))
        assertEquals(StoreClientAction.Open, StoreClients.action(installed = false, games = false, hasPackage = false))
        assertEquals(StoreClientAction.Install, StoreClients.action(installed = false, games = false, hasPackage = true))
        assertEquals(StoreClientAction.ReplaceAptoide, StoreClients.action(installed = true, games = true, hasPackage = true))
    }

    @Test
    fun aptoideOfficialApkIsNotASecondWebsiteChip() {
        val aptoide = StoreClients.all().first { it.id == StoreClientId.Aptoide }
        assertEquals(0, aptoide.urls.count { it.kind == StoreUrlKind.Web })
        assertTrue(aptoide.urls.any { it.kind == StoreUrlKind.Apk && it.url == AptoideLink.INSTALL_PAGE })
    }

    @Test
    fun aptoideGamesUsesOfficialActivityHint() {
        assertTrue(AptoideLink.isGamesClient("com.aptoide.android.aptoidegames.MainActivity"))
        assertFalse(AptoideLink.isGamesClient("cm.aptoide.pt.view.MainActivity"))
        assertFalse(AptoideLink.isGamesClient(null))
    }
}
