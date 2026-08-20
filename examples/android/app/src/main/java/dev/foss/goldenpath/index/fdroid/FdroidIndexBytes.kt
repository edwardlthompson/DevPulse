package dev.foss.goldenpath.index.fdroid

object FdroidIndexBytes {
    fun ascii(text: String): ByteArray = text.toByteArray(Charsets.US_ASCII)

    fun indexOf(hay: ByteArray, needle: String, from: Int = 0): Int =
        indexOf(hay, ascii(needle), from)

    fun indexOf(hay: ByteArray, needle: ByteArray, from: Int = 0): Int {
        if (needle.isEmpty() || from > hay.size - needle.size) return -1
        val first = needle[0]
        val last = hay.size - needle.size
        var i = from
        while (i <= last) {
            if (hay[i] == first) {
                var j = 1
                while (j < needle.size && hay[i + j] == needle[j]) j++
                if (j == needle.size) return i
            }
            i++
        }
        return -1
    }

    fun indexOfByte(hay: ByteArray, value: Byte, from: Int): Int {
        var i = from
        while (i < hay.size) {
            if (hay[i] == value) return i
            i++
        }
        return -1
    }

    fun readJsonString(hay: ByteArray, from: Int): String? {
        val colon = indexOfByte(hay, ':'.code.toByte(), from)
        if (colon < 0 || colon > from + 4) return null
        val open = indexOfByte(hay, '"'.code.toByte(), colon)
        if (open < 0 || open > colon + 4) return null
        val close = indexOfByte(hay, '"'.code.toByte(), open + 1)
        if (close < 0) return null
        return utf8(hay, open + 1, close)
    }

    fun utf8(hay: ByteArray, from: Int, to: Int): String {
        val start = from.coerceIn(0, hay.size)
        val end = to.coerceIn(start, hay.size)
        return String(hay, start, end - start, Charsets.UTF_8)
    }

    /** Enclosing JSON object for `"packageName"` at [nameAt]; stops before the next app key. */
    fun objectSlice(hay: ByteArray, nameAt: Int): String {
        val open = objectOpen(hay, nameAt)
        val next = indexOf(hay, "\"packageName\"", nameAt + 13)
        val limit = if (next < 0) hay.size else next
        return utf8(hay, open, objectClose(hay, open, limit))
    }

    private fun objectOpen(hay: ByteArray, from: Int): Int {
        var i = from
        var depth = 0
        var inStr = false
        while (i > 0) {
            i--
            when {
                escaped(hay, i) -> continue
                hay[i] == QUOTE -> inStr = !inStr
                inStr -> continue
                hay[i] == CLOSE -> depth++
                hay[i] == OPEN && depth == 0 -> return i
                hay[i] == OPEN -> depth--
            }
        }
        return 0
    }

    private fun objectClose(hay: ByteArray, open: Int, limit: Int): Int {
        var i = open
        var depth = 0
        var inStr = false
        var escape = false
        while (i < limit) {
            val c = hay[i]
            when {
                escape -> escape = false
                inStr && c == SLASH -> escape = true
                c == QUOTE -> inStr = !inStr
                !inStr && c == OPEN -> depth++
                !inStr && c == CLOSE -> {
                    depth--
                    if (depth == 0) return i + 1
                }
            }
            i++
        }
        return limit
    }

    private fun escaped(hay: ByteArray, i: Int): Boolean {
        var n = 0
        var j = i
        while (j > 0 && hay[j - 1] == SLASH) {
            n++
            j--
        }
        return n and 1 == 1
    }

    private val OPEN = '{'.code.toByte()
    private val CLOSE = '}'.code.toByte()
    private val QUOTE = '"'.code.toByte()
    private val SLASH = '\\'.code.toByte()
}
