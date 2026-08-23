package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.forge.ForgeBackoff

object HostRetry {
    fun note(host: String, statusCode: Int, retryAfterSec: Long?) {
        ForgeBackoff.nextDelayMs(statusCode, 1, retryAfterSec)?.let { delay ->
            RefreshHostBackoff.note(host, delay)
        }
    }
}
