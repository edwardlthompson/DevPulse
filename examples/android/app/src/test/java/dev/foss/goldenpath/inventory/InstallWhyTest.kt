package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
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
    }
}
