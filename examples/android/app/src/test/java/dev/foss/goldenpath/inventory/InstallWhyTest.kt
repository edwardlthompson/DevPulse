package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InstallWhyTest {
    @Test
    fun installFailureMapsToWhy() {
        assertEquals(OneClickResult.Failed(InstallWhy.Timeout), ApkInstallResult.Failed("ui").toClick())
        assertEquals(OneClickResult.Failed(InstallWhy.NoFile), ApkInstallResult.Failed("missing").toClick())
        assertEquals(OneClickResult.Failed(InstallWhy.Permission), ApkInstallResult.Failed("blocked").toClick())
        assertEquals(OneClickResult.Installed, ApkInstallResult.Launched.toClick())
    }

    @Test
    fun listingFailNotesSigningThenNoFile() {
        ListingFail.signing()
        assertEquals(InstallWhy.Signing, ListingFail.why)
        ListingFail.none()
        assertEquals(InstallWhy.NoFile, ListingFail.why)
        ListingFail.older()
        assertEquals(InstallWhy.Older, ListingFail.why)
        ListingFail.sdk()
        assertEquals(InstallWhy.Sdk, ListingFail.why)
        ListingFail.space()
        assertEquals(InstallWhy.NoSpace, ListingFail.why)
        ListingFail.playPurchase()
        assertEquals(InstallWhy.PlayPurchase, ListingFail.why)
    }

    @Test
    fun listingFailWhyStaysOnItsThread() {
        val fromOther = java.util.concurrent.atomic.AtomicReference<InstallWhy>()
        val done = java.util.concurrent.CountDownLatch(1)
        ListingFail.signing()
        Thread {
            ListingFail.older()
            fromOther.set(ListingFail.why)
            done.countDown()
        }.start()
        assertTrue(done.await(2, java.util.concurrent.TimeUnit.SECONDS))
        assertEquals(InstallWhy.Older, fromOther.get())
        assertEquals(InstallWhy.Signing, ListingFail.why)
    }
}
