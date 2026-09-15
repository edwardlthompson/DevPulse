package dev.foss.goldenpath.crashcapture

object CrashSanitize {
    private val email = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
    private val path = Regex("(/[\\w.-]+)+")

    fun apply(error: Throwable, maxChars: Int = 4000): String {
        val type = error::class.java.name
        val stack = error.stackTraceToString()
            .replace(email, "[redacted]")
            .replace(path, "[path]")
        val body = "$type\n$stack"
        return if (body.length <= maxChars) body else body.take(maxChars)
    }
}
