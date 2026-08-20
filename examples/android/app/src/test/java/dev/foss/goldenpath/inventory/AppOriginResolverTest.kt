package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Test

class AppOriginResolverTest {
    @Test
    fun installerMapsKnownStores() {
        assertEquals(AppOrigin.Play, AppOriginResolver.fromInstaller("com.android.vending"))
        assertEquals(AppOrigin.Play, AppOriginResolver.fromInstaller("com.aurora.store"))
        assertEquals(AppOrigin.Fdroid, AppOriginResolver.fromInstaller("org.fdroid.fdroid"))
        assertEquals(AppOrigin.ExtraRepo, AppOriginResolver.fromInstaller("com.looker.droidify"))
        assertEquals(AppOrigin.SideloadedUnknown, AppOriginResolver.fromInstaller(null))
        assertEquals(AppOrigin.SideloadedUnknown, AppOriginResolver.fromInstaller("com.android.shell"))
    }

    @Test
    fun refineNeverLeavesUnknown() {
        assertEquals(AppOrigin.Play, AppOriginResolver.refine(AppOrigin.Play, RemoteReleasedSource.Fdroid))
        assertEquals(AppOrigin.Fdroid, AppOriginResolver.refine(AppOrigin.SideloadedUnknown, RemoteReleasedSource.Fdroid))
        assertEquals(AppOrigin.ExtraRepo, AppOriginResolver.refine(AppOrigin.Unknown, RemoteReleasedSource.Aptoide))
        assertEquals(AppOrigin.SideloadedUnknown, AppOriginResolver.refine(AppOrigin.Unknown, RemoteReleasedSource.None))
    }
}
