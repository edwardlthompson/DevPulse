package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateAllTest {
    @Before
    fun reset() {
        UpdateArtifactMemory.clear()
    }

    @Test
    fun queueIsDirectArtifactsForNewerApps() {
        UpdateArtifactMemory.add(
            UpdateArtifact("app.one", RemoteReleasedSource.Fdroid, "https://f-droid.org/repo/app.one_2.apk"),
        )
        val newer = sampleApp("app.one", remoteVersionName = "2.0")
        val same = sampleApp("app.same", remoteVersionName = "1.0")
        assertEquals(1, UpdateAll.artifacts(listOf(newer, same)).size)
        assertEquals("app.one", UpdateAll.artifacts(listOf(newer)).single().packageName)
    }

    @Test
    fun downloadsAllThenInstallsOneByOne() {
        val first = UpdateArtifact("app.a", RemoteReleasedSource.Fdroid, "https://f-droid.org/repo/a.apk")
        val second = UpdateArtifact("app.b", RemoteReleasedSource.Izzy, "https://apt.izzysoft.de/fdroid/repo/b.apk")
        val dir = File.createTempFile("all", "dir").apply { delete(); mkdirs() }
        val order = mutableListOf<String>()
        val result = UpdateAll.run(
            artifacts = listOf(first, second),
            cacheDir = dir,
            fetch = { url ->
                order += "dl:${url.substringAfterLast('/')}"
                Result.success(byteArrayOf(1, 2, 3))
            },
            install = { file ->
                order += "ins:${file.name}"
                ApkInstallResult.Ok
            },
            inspect = { file ->
                val pkg = if (file.name.contains("app.b")) "app.b" else "app.a"
                ApkInspect(pkg, setOf("aa"))
            },
            installedOf = { pkg -> InstalledIdentity(pkg, setOf("aa")) },
            maxFiles = 16,
        )
        assertEquals(2, result.downloaded)
        assertEquals(2, result.installed)
        assertEquals(0, result.failedDownload)
        assertTrue(order[0].startsWith("dl:"))
        assertTrue(order[1].startsWith("dl:"))
        assertTrue(order[2].startsWith("ins:"))
        assertTrue(order[3].startsWith("ins:"))
    }

    private fun sampleApp(packageName: String, remoteVersionName: String): InstalledApp = InstalledApp(
        packageName = packageName,
        label = packageName,
        versionName = "1.0",
        versionCode = 1L,
        lastUpdateTimeMs = 1L,
        firstInstallTimeMs = 1L,
        minSdk = 26,
        targetSdk = 37,
        isSystemApp = false,
        remoteVersionName = remoteVersionName,
    )
}
