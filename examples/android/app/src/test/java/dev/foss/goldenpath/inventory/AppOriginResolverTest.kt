package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AppOriginResolverTest {
    @Before
    fun reset() {
        RemoteReleaseMemory.clear()
    }

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
        assertEquals(
            AppOrigin.Play,
            AppOriginResolver.refine(AppOrigin.SideloadedUnknown, RemoteReleasedSource.ApkPure, playListed = true),
        )
    }

    @Test
    fun mergeTreatsPlayListedSideloadInstallerAsPlay() {
        RemoteReleaseMemory.putAll(
            mapOf(
                "com.google.android.keep" to RemoteReleaseRollup.from(
                    listOf(
                        RemoteReleaseOffer(
                            source = RemoteReleasedSource.Play,
                            listed = true,
                            versionName = "5.26.361.02.90",
                            versionCode = 220675001,
                        ),
                        RemoteReleaseOffer(
                            source = RemoteReleasedSource.ApkPure,
                            listed = true,
                            versionName = "5.26.365.00.90",
                            versionCode = 220675766,
                        ),
                    ),
                ),
            ),
        )
        val merged = RemoteReleaseMemory.merge(
            sampleApp("com.google.android.keep", "Keep", origin = AppOrigin.SideloadedUnknown),
        )
        assertEquals(AppOrigin.Play, merged.origin)
    }
}
