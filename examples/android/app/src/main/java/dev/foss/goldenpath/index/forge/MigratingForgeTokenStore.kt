package dev.foss.goldenpath.index.forge

class MigratingForgeTokenStore(
    private val secure: ForgeTokenStore,
    private val plaintext: ForgeTokenStore,
) : ForgeTokenStore {
    override fun getToken(): String? {
        secure.getToken()?.let { return it }
        val old = plaintext.getToken() ?: return null
        secure.setToken(old)
        plaintext.setToken(null)
        return old
    }

    override fun setToken(token: String?) {
        secure.setToken(token)
        plaintext.setToken(null)
    }
}
