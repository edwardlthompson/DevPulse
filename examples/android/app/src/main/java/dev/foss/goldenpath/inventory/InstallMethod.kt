package dev.foss.goldenpath.inventory

enum class InstallMethod {
    System,
    Session,
    Root,
    ;

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
