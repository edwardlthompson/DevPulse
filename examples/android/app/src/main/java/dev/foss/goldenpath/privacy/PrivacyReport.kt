package dev.foss.goldenpath.privacy

object PrivacyReport {
    fun text(): String = """
        DevPulse is local-only. No accounts, analytics, or crash SDKs.
        Optional lookups and save-crashes stay off until the user turns them on.
        Crashes are never sent automatically. GitHub Issues open only after a tap.
    """.trimIndent()
}
