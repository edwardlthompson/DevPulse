package dev.foss.goldenpath.inventory

import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HostRetryTest {
    @Before
    fun reset() {
        RefreshHostBackoff.clear()
    }

    @Test
    fun play429UsesRetryAfter() {
        HostRetry.note("play", 429, retryAfterSec = 15)
        val remain = RefreshHostBackoff.active()["play"] ?: 0L
        assertTrue(remain in 1_000L..15_000L)
    }

    @Test
    fun successDoesNotBackOff() {
        HostRetry.note("apkmirror", 200, 30)
        assertTrue(RefreshHostBackoff.active().isEmpty())
    }
}
