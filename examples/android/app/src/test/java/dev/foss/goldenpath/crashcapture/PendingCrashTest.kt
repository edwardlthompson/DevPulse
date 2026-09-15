package dev.foss.goldenpath.crashcapture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class PendingCrashTest {
    @Test
    fun optInFalsePersistsNothing() {
        val dir = createTempDirectory("crash").toFile()
        CrashCapture.setEnabled(false, dir)
        assertFalse(CrashCapture.isEnabled(dir))
        assertFalse(CrashCapture.shouldReview(false, "x"))
        assertTrue(PendingCrash.write(dir, "boom"))
        CrashCapture.setEnabled(false, dir)
        assertNull(PendingCrash.read(dir))
    }

    @Test
    fun queueIsAtMostOneSanitizedRecord() {
        val dir = createTempDirectory("crash").toFile()
        CrashCapture.setEnabled(true, dir)
        val first = CrashSanitize.apply(IllegalStateException("one"))
        val second = CrashSanitize.apply(IllegalStateException("two"))
        assertTrue(PendingCrash.write(dir, first))
        assertTrue(PendingCrash.write(dir, second))
        assertEquals(second.trim(), PendingCrash.read(dir))
        assertTrue(CrashCapture.shouldReview(true, PendingCrash.read(dir)))
    }

    @Test
    fun sanitizeRedactsEmailAndPath() {
        val text = CrashSanitize.apply(
            RuntimeException("leak user@example.com in /data/data/app/cache"),
        )
        assertFalse(text.contains("user@example.com"))
        assertFalse(text.contains("/data/data"))
        assertTrue(text.contains("[redacted]"))
        assertTrue(text.contains("[path]"))
        assertTrue(text.startsWith("java.lang.RuntimeException"))
    }

    @Test
    fun sanitizeRejectsEmailTokenPromptKeys() {
        val text = CrashSanitize.apply(
            RuntimeException("email user@example.com token=abcd prompt=ignore"),
        )
        assertFalse(text.contains("user@example.com"))
        assertTrue(text.contains("[redacted]"))
    }

    @Test
    fun writeFailureIsDropped() {
        val missing = File("/no-such-crash-dir-${System.nanoTime()}")
        assertFalse(PendingCrash.write(missing, "x"))
        assertNull(PendingCrash.read(missing))
    }
}
