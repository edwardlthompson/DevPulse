package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OneClickUpdateTest {
    @Before
    fun reset() {
        UpdateArtifactMemory.clear()
        IgnoredUpdates.clear()
        AppliedUpdates.clear()
    }

    @Test
    fun prefersDirectFileOverPlay() {
        UpdateArtifactMemory.add(
            UpdateArtifact("app.one", RemoteReleasedSource.Fdroid, "https://f-droid.org/repo/app.one_1.apk"),
        )
        val play = UpdateLink(RemoteReleasedSource.Play, "https://play.google.com/store/apps/details?id=app.one")
        val kind = OneClickUpdate.kind("app.one", listOf(play))
        assertTrue(kind is OneClickKind.Direct)
    }

    @Test
    fun playWhenNoFileUrl() {
        val play = UpdateLink(RemoteReleasedSource.Play, UpdateUrls.play("app.one"), listed = true)
        assertEquals(OneClickKind.Play("app.one"), OneClickUpdate.kind("app.one", listOf(play)))
        assertEquals("market://details?id=app.one", PlayStoreIntent.marketUri("app.one"))
    }

    @Test
    fun auroraDownloadThenFallsBackToPlay() {
        val dir = File.createTempFile("apk", "dir").apply { delete(); mkdirs() }
        val artifact = UpdateArtifact(
            "app.one",
            RemoteReleasedSource.Play,
            "https://redirector.gvt1.com/edgedl/android/market/app.one",
        )
        var installed = ""
        var opened = ""
        val hit = OneClickUpdate.apply(
            OneClickKind.Play("app.one"),
            dir,
            fetch = { Result.success(byteArrayOf(1, 2, 3)) },
            install = { file -> installed = file.name; ApkInstallResult.Ok },
            openPlay = { opened = it },
            inspect = { ApkInspect("app.one", setOf("aa")) },
            installed = InstalledIdentity("app.one", setOf("aa")),
            resolveAurora = { artifact },
        )
        assertEquals(OneClickResult.Installed, hit)
        assertEquals("", opened)
        val miss = OneClickUpdate.apply(
            OneClickKind.Play("app.one"),
            dir,
            fetch = { Result.success(byteArrayOf(1, 2, 3)) },
            install = { ApkInstallResult.Ok },
            openPlay = { opened = it },
            inspect = { ApkInspect("app.one", setOf("aa")) },
            installed = InstalledIdentity("app.one", setOf("aa")),
            resolveAurora = { null },
        )
        assertEquals(OneClickResult.PlayOpened, miss)
        assertEquals("app.one", opened)
    }

    @Test
    fun apkPureWhenListedAndNoFileUrl() {
        val pure = UpdateLink(RemoteReleasedSource.ApkPure, "https://apkpure.com/search?q=app.one", listed = true)
        assertEquals(OneClickKind.ApkPure("app.one"), OneClickUpdate.kind("app.one", listOf(pure)))
    }

    @Test
    fun apkPureDownloadsWhenResolveReturnsFile() {
        val artifact = UpdateArtifact(
            "app.one",
            RemoteReleasedSource.ApkPure,
            "https://d.apkpure.com/b/APK/app.one?versionCode=2",
        )
        val dir = File.createTempFile("apk", "dir").apply { delete(); mkdirs() }
        var installed = ""
        val result = OneClickUpdate.apply(
            OneClickKind.ApkPure("app.one"),
            dir,
            fetch = { Result.success(byteArrayOf(1, 2, 3)) },
            install = { file -> installed = file.name; ApkInstallResult.Ok },
            openPlay = {},
            resolveApkPure = { artifact },
            inspect = { ApkInspect("app.one", setOf("aa")) },
            installed = InstalledIdentity("app.one", setOf("aa")),
        )
        assertEquals(OneClickResult.Installed, result)
        assertTrue(installed.endsWith(".apk"))
    }

    @Test
    fun apkPureFailsWhenNoFileUrl() {
        val dir = File.createTempFile("apk", "dir").apply { delete(); mkdirs() }
        val result = OneClickUpdate.apply(
            OneClickKind.ApkPure("app.one"),
            dir,
            fetch = { Result.success(byteArrayOf(1, 2, 3)) },
            install = { ApkInstallResult.Ok },
            openPlay = {},
            resolveApkPure = { null },
            inspect = { ApkInspect("app.one", setOf("aa")) },
            installed = InstalledIdentity("app.one", setOf("aa")),
        )
        assertEquals(OneClickResult.FailedDownload, result)
    }

    @Test
    fun applyDownloadsThenInstalls() {
        val artifact = UpdateArtifact("app.one", RemoteReleasedSource.Fdroid, "https://f-droid.org/repo/app.one_1.apk")
        val dir = File.createTempFile("apk", "dir").apply { delete(); mkdirs() }
        var installed = ""
        val result = OneClickUpdate.apply(
            OneClickKind.Direct(artifact),
            dir,
            fetch = { Result.success(byteArrayOf(9, 8, 7)) },
            install = { file -> installed = file.name; ApkInstallResult.Ok },
            openPlay = {},
            inspect = { ApkInspect("app.one", setOf("aa")) },
            installed = InstalledIdentity("app.one", setOf("aa")),
        )
        assertEquals(OneClickResult.Installed, result)
        assertTrue(installed.endsWith(".apk"))
    }
}
