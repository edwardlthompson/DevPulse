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
        var opened = ""
        val result = OneClickUpdate.apply(
            OneClickKind.ApkPure("app.one"),
            dir,
            fetch = { Result.success(byteArrayOf(1, 2, 3)) },
            install = { file -> installed = file.name; ApkInstallResult.Ok },
            openPlay = {},
            openApkPure = { opened = it },
            resolveApkPure = { artifact },
            inspect = { ApkInspect("app.one", setOf("aa")) },
            installed = InstalledIdentity("app.one", setOf("aa")),
        )
        assertEquals(OneClickResult.Installed, result)
        assertEquals("", opened)
        assertTrue(installed.endsWith(".apk"))
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
