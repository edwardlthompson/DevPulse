package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OneClickSignerTest {
    @Before
    fun reset() {
        UpdateArtifactMemory.clear()
        IgnoredUpdates.clear()
        AppliedUpdates.clear()
    }

    @Test
    fun signerClashKeepsFileAndDoesNotIgnore() {
        val artifact = UpdateArtifact("app.one", RemoteReleasedSource.Fdroid, "https://f-droid.org/repo/app.one_1.apk")
        val dir = File.createTempFile("apk", "dir").apply { delete(); mkdirs() }
        val filesDir = File.createTempFile("files", "dir").apply { delete(); mkdirs() }
        val result = OneClickUpdate.apply(
            OneClickKind.Direct(artifact),
            dir,
            fetch = { Result.success(byteArrayOf(9, 8, 7)) },
            install = { ApkInstallResult.Ok },
            openPlay = {},
            inspect = { ApkInspect("app.one", setOf("bb")) },
            installed = InstalledIdentity("app.one", setOf("aa")),
            filesDir = filesDir,
        )
        val failed = result as OneClickResult.Failed
        assertEquals(InstallWhy.Signing, failed.why)
        assertTrue(failed.files.isNotEmpty())
        assertTrue(failed.files.first().isFile)
        assertFalse(IgnoredUpdates.has("app.one", RemoteReleasedSource.Fdroid, artifact.versionName))
    }
}
