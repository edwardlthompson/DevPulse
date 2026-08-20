package dev.foss.goldenpath.index.apkpure

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApkPureScanTest {
    @Test
    fun listedAndUnknownStayHonest() {
        val json = checkNotNull(javaClass.classLoader?.getResourceAsStream("apkpure/update-ok.json"))
            .bufferedReader().use { it.readText() }
        val offers = ApkPureScan.offersFor(
            listOf("app.listed", "app.other"),
            ApkPureBatchFetcher { Result.success(json) },
        )
        assertTrue(offers.getValue("app.listed").listed)
        assertFalse(offers.getValue("app.other").listed)
        assertTrue(offers.getValue("app.other").known)
    }
}
