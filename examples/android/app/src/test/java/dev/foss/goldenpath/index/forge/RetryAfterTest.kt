package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RetryAfterTest {
    @Test
    fun deltaSecondsOnly() {
        assertEquals(12L, RetryAfter.seconds("12"))
        assertEquals(12L, RetryAfter.seconds(" 12 "))
        assertNull(RetryAfter.seconds("0"))
        assertNull(RetryAfter.seconds("Wed, 21 Oct 2015 07:28:00 GMT"))
        assertNull(RetryAfter.seconds("  "))
    }
}
