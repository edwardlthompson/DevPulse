package dev.foss.goldenpath.inventory

import java.io.File
import java.io.RandomAccessFile
import kotlin.math.min

/** Cheap ZIP checks so we never hand a truncated download to PackageManager. */
object ApkZip {
    private const val EOCD_MIN = 22
    private const val EOCD_MAX = 65_557

    fun ok(file: File): Boolean {
        if (!file.isFile || file.length() < EOCD_MIN.toLong()) return false
        if (!file.inputStream().use { it.read() == 0x50 && it.read() == 0x4B }) return false
        return eocd(file)
    }

    fun dropIfInvalid(file: File): Boolean {
        if (ok(file)) return false
        file.delete()
        return true
    }

    private fun eocd(file: File): Boolean {
        val len = file.length()
        val n = min(len, EOCD_MAX.toLong()).toInt()
        val buf = ByteArray(n)
        RandomAccessFile(file, "r").use { raf ->
            raf.seek(len - n)
            raf.readFully(buf)
        }
        for (i in buf.size - EOCD_MIN downTo 0) {
            if (buf[i] == 0x50.toByte() && buf[i + 1] == 0x4B.toByte() &&
                buf[i + 2] == 5.toByte() && buf[i + 3] == 6.toByte()
            ) {
                return true
            }
        }
        return false
    }
}
