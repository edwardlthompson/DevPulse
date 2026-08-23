package dev.foss.goldenpath.index.aptoide

object AptoideCatalog {
    const val STORE = "apps"
    const val GAMES = "aptoide-games"

    @Volatile
    var name: String = STORE

    fun pick(games: Boolean) {
        name = if (games) GAMES else STORE
    }

    fun storeName(): String = name.ifBlank { STORE }

    fun reset() {
        name = STORE
    }
}
