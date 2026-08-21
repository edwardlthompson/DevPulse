package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreClientsTest {
    @Test
    fun catalogIsPlayFallbackAndApkMirrorOnly() {
        val ids = StoreClients.all().map { it.id }
        assertEquals(listOf(StoreClientId.Play, StoreClientId.ApkMirror), ids)
        assertEquals(enumValues<StoreClientId>().toList(), ids)
        val play = StoreClients.all().first { it.id == StoreClientId.Play }
        assertEquals(PlayStoreIntent.STORE_PACKAGE, play.packageName)
        val mirror = StoreClients.all().first { it.id == StoreClientId.ApkMirror }
        assertTrue(mirror.urls.any { it.kind == StoreUrlKind.Web && it.url.contains("apkmirror.com") })
    }

    @Test
    fun actionIsAlwaysOpenInAppExceptPlayAndMirror() {
        assertEquals(StoreClientAction.Open, StoreClients.action(installed = true, hasPackage = true))
        assertEquals(StoreClientAction.Open, StoreClients.action(installed = false, hasPackage = false))
    }
}
