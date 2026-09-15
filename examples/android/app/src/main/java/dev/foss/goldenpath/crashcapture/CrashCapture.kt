package dev.foss.goldenpath.crashcapture

import android.content.Context
import java.io.File

object CrashCapture {
    @Volatile
    private var entered = false

    @Volatile
    var enabled: Boolean = false
        private set

    fun flagFile(dir: File): File = File(dir, "crash_opt_in")

    fun isEnabled(dir: File): Boolean =
        runCatching { flagFile(dir).readText().trim() == "1" }.getOrDefault(false)

    fun setEnabled(on: Boolean, dir: File) {
        enabled = on
        runCatching { flagFile(dir).writeText(if (on) "1" else "0") }
        if (!on) PendingCrash.clear(dir)
    }

    fun shouldReview(on: Boolean, pending: String?): Boolean =
        on && !pending.isNullOrBlank()

    fun install(context: Context) {
        val dir = context.filesDir
        enabled = isEnabled(dir)
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            if (!entered && enabled) {
                entered = true
                runCatching { PendingCrash.write(dir, CrashSanitize.apply(error)) }
            }
            previous?.uncaughtException(thread, error)
        }
    }
}
