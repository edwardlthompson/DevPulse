package dev.foss.goldenpath.inventory

import java.io.InputStream

internal object ApkStreamCopy {
    const val BUF = 16 * 1024
    const val STEP = 64L * 1024
    const val LOG = 8L * 1024 * 1024

    fun run(
        input: InputStream,
        total: Long,
        onProgress: ((Long, Long) -> Unit)?,
        write: (ByteArray, Int) -> Unit,
        cancelled: () -> Boolean = { UpdateAllCancel.requested() },
    ) {
        val buf = ByteArray(BUF)
        var read = 0L
        var reported = -1L
        var logged = 0L
        onProgress?.invoke(0L, total)
        reported = 0L
        while (true) {
            if (cancelled()) error("apk cancelled")
            val n = input.read(buf)
            if (n < 0) break
            if (ApkHttpFetcher.MAX_BYTES > 0L && read + n > ApkHttpFetcher.MAX_BYTES) error("apk too large")
            write(buf, n)
            read += n
            if (read - reported >= STEP || (total > 0L && read == total)) {
                onProgress?.invoke(read, total)
                reported = read
            }
            if (read - logged >= LOG) {
                RefreshTrace.line("apk ${read}B")
                logged = read
            }
        }
        if (read != reported) onProgress?.invoke(read, total)
    }
}
