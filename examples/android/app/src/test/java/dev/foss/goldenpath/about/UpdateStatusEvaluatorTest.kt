package dev.foss.goldenpath.about

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateStatusEvaluatorTest {
    @Test
    fun evaluate_returnsCurrentWhenNoTag() {
        assertEquals(UpdateStatusEvaluator.Result.Current, UpdateStatusEvaluator.evaluate("0.1.0", null))
    }

    @Test
    fun evaluate_returnsAvailableWhenNewer() {
        val result = UpdateStatusEvaluator.evaluate("0.1.0", "v0.2.0")
        assertTrue(result is UpdateStatusEvaluator.Result.Available)
        assertEquals("0.2.0", (result as UpdateStatusEvaluator.Result.Available).version)
    }

    @Test
    fun evaluate_returnsCurrentWhenSameVersion() {
        assertEquals(UpdateStatusEvaluator.Result.Current, UpdateStatusEvaluator.evaluate("1.0.0", "v1.0.0"))
    }
}
