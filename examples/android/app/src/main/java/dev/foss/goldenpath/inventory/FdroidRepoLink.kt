package dev.foss.goldenpath.inventory

object FdroidRepoLink {
    const val IZZY_FINGERPRINT = "3BF0D6ABFEAE2F401707B6D966BE743BF0EEE49C2561B9BA39041BB262C76DAE"
    const val GUARDIAN_FINGERPRINT = "B7C2EEFD8DAC7806AF67DFCD92EB18126BC08312A7F2D6F3862E46013C7A6135"
    const val CALYX_FINGERPRINT = "C44D58B4547DE5096138CB0B34A1CC99DAB3B4274412ED753FCCBFC11DC1B7B6"

    const val IZZY_HOST = "apt.izzysoft.de/fdroid/repo"
    const val GUARDIAN_HOST = "guardianproject.info/fdroid/repo"
    const val CALYX_HOST = "calyxos.gitlab.io/calyx-fdroid-repo/fdroid/repo"

    fun addUri(hostPath: String, fingerprint: String): String {
        val hex = fingerprintHex(fingerprint) ?: return ""
        val host = hostPath.trim().trimStart('/').trimEnd('/')
        if (host.isEmpty()) return ""
        return "fdroidrepo://$host?fingerprint=$hex"
    }

    fun fingerprintHex(raw: String): String? {
        val hex = raw.filter { !it.isWhitespace() }.uppercase()
        if (hex.length != 64 || hex.any { it !in '0'..'9' && it !in 'A'..'F' }) return null
        return hex
    }

    fun fingerprintOf(uri: String): String? {
        val q = uri.substringAfter("fingerprint=", "").substringBefore('&').trim()
        return fingerprintHex(q)
    }

    fun extraAddUri(repoId: String): String? = when (repoId) {
        "izzy" -> addUri(IZZY_HOST, IZZY_FINGERPRINT)
        "guardian" -> addUri(GUARDIAN_HOST, GUARDIAN_FINGERPRINT)
        "calyx" -> addUri(CALYX_HOST, CALYX_FINGERPRINT)
        else -> null
    }

    fun preferredClient(fdroidInstalled: Boolean, droidifyInstalled: Boolean): String? = when {
        fdroidInstalled -> "org.fdroid.fdroid"
        droidifyInstalled -> "com.looker.droidify"
        else -> null
    }
}
