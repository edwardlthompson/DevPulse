package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.aurora.AuroraPlayLive
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class OneClickPlayPurchaseTest {
    @Before
    fun reset() {
        IgnoredUpdates.clear()
        AppliedUpdates.clear()
        AuroraPlayLive.clearWhy()
        AuroraPlayLive.releaseSession()
    }

    @After
    fun release() {
        AuroraPlayLive.releaseSession()
    }

    @Test
    fun appNotPurchasedOpensPlayAndIsNotIgnored() {
        val dir = File.createTempFile("apk", "dir").apply { delete(); mkdirs() }
        var opened = ""
        AuroraPlayLive.markWhy("app.paid", InstallWhy.PlayPurchase)
        val paid = OneClickUpdate.apply(
            OneClickKind.Play("app.paid"),
            dir,
            fetch = { Result.success(byteArrayOf(1, 2, 3)) },
            install = { ApkInstallResult.Ok },
            openPlay = { opened = it },
            inspect = { ApkInspect("app.paid", setOf("aa")) },
            installed = InstalledIdentity("app.paid", setOf("aa")),
            resolveAurora = { null },
            filesDir = dir,
        )
        assertEquals(OneClickResult.Failed(InstallWhy.PlayPurchase), paid)
        assertEquals("app.paid", opened)
        assertFalse(IgnoredUpdates.has("app.paid", RemoteReleasedSource.Play, null))
    }
}
