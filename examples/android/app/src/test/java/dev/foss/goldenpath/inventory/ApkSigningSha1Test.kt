package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApkSigningSha1Test {
    @Test
    fun formatsColonUpperHex() {
        assertEquals(
            "A9:99:3E:36:47:06:81:6A:BA:3E:25:71:78:50:C2:6C:9C:D0:D8:9D",
            ApkSigningSha1.of("abc".toByteArray()),
        )
    }

    @Test
    fun blankCertIsNull() {
        assertNull(ApkSigningSha1.of(null))
        assertNull(ApkSigningSha1.of(ByteArray(0)))
    }
}
