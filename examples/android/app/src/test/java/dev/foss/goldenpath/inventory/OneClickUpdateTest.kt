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
