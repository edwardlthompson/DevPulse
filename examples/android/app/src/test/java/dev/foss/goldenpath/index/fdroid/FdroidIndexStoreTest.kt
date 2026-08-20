package dev.foss.goldenpath.index.fdroid

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FdroidIndexStoreTest {
    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun loadReturnsFreshBytesAndExpiresAfterTtl() {
        val store = FdroidIndexStore(tmp.newFolder())
        val payload = """{"apps":[]}""".toByteArray()
        store.save("official", payload, nowMs = 1_000L)
        assertEquals(payload.toList(), store.load("official", 1_000L + FdroidCachePolicy.TTL_MS - 1)?.toList())
        assertNull(store.load("official", 1_000L + FdroidCachePolicy.TTL_MS))
    }
}
