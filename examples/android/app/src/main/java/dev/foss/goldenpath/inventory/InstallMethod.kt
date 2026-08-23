package dev.foss.goldenpath.inventory

enum class InstallMethod {
    System,
    Session,
    Root,
    ;

    fun effective(canRequestInstalls: Boolean): InstallMethod =
        if (this == Session && !canRequestInstalls) System else this

    companion object {
        fun parse(raw: String?): InstallMethod =
            entries.firstOrNull { it.name == raw } ?: System
    }
}

data class InstallShellResult(
    val exitCode: Int,
    val output: String,
)

fun interface InstallShell {
    fun run(args: List<String>): InstallShellResult
}

sealed class ApkInstallResult {
    data object Ok : ApkInstallResult()
    data object Launched : ApkInstallResult()
    data class Failed(val reason: String) : ApkInstallResult()
}
