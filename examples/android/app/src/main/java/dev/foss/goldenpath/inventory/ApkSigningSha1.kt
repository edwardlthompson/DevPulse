package dev.foss.goldenpath.inventory

import java.security.MessageDigest

object ApkSigningSha1 {
    fun of(cert: ByteArray?): String? {
        if (cert == null || cert.isEmpty()) return null
        val digest = MessageDigest.getInstance("SHA-1").digest(cert)
        return digest.joinToString(":") { byte -> "%02X".format(byte) }
    }
}
