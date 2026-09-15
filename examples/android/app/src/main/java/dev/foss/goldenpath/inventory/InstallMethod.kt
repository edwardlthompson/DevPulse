package dev.foss.goldenpath.inventory

enum class InstallMethod {
    System,
    Session,
    Root,
    ;

    fun effective(canRequestInstalls: Boolean, rootAvailable: Boolean = true): InstallMethod =
        when {
            this == Root && !rootAvailable -> if (canRequestInstalls) Session else System
            this == Session && !canRequestInstalls -> System
            else -> this
        }

    fun onDevice(canRequestInstalls: Boolean): InstallMethod =
        effective(canRequestInstalls, RootPmInstall.available())

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
