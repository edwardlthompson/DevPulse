package dev.foss.goldenpath.index.forge

object RetryAfter {
    fun seconds(raw: String?): Long? {
        val token = raw?.trim().orEmpty()
        if (token.isEmpty()) return null
        return token.toLongOrNull()?.takeIf { it > 0L }
    }
}
