package dev.foss.goldenpath.index.forge

object GitHubReleaseJsonHelper {
    fun splitObjects(body: String): List<String> {
        val list = mutableListOf<String>()
        var depth = 0
        var inStr = false
        var escape = false
        var start = -1
        for (i in body.indices) {
            val c = body[i]
            when {
                escape -> escape = false
                inStr && c == '\\' -> escape = true
                c == '"' -> inStr = !inStr
                !inStr && c == '{' -> {
                    if (depth == 0) start = i
                    depth++
                }
                !inStr && c == '}' -> {
                    depth--
                    if (depth == 0 && start >= 0) {
                        list += body.substring(start, i + 1)
                        start = -1
                    }
                }
            }
        }
        return list
    }
}
