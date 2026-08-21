package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MigratingForgeTokenStoreTest {
    @Test
    fun movesPlaintextIntoSecureAndClears() {
        val secure = MemoryTokenStore()
        val plain = MemoryTokenStore().also { it.setToken("ghp_old") }
        val store = MigratingForgeTokenStore(secure, plain)
        assertEquals("ghp_old", store.getToken())
        assertEquals("ghp_old", secure.getToken())
        assertNull(plain.getToken())
    }

    @Test
    fun setWritesSecureOnly() {
        val secure = MemoryTokenStore()
        val plain = MemoryTokenStore().also { it.setToken("left") }
        MigratingForgeTokenStore(secure, plain).setToken("ghp_new")
        assertEquals("ghp_new", secure.getToken())
        assertNull(plain.getToken())
    }

    private class MemoryTokenStore : ForgeTokenStore {
        private var value: String? = null
        override fun getToken(): String? = value
        override fun setToken(token: String?) {
            value = token?.trim()?.ifEmpty { null }
        }
    }
}
