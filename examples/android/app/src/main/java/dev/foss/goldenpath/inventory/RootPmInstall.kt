package dev.foss.goldenpath.inventory

object RootPmInstall {
    private val unsafe = Regex("[\"'\\n\\r;|&$`]")

    fun args(apkPath: String): List<String>? {
        val path = apkPath.trim()
        if (path.isEmpty() || !path.endsWith(".apk", ignoreCase = true)) return null
        if (unsafe.containsMatchIn(path)) return null
        return listOf("su", "-c", "pm install -r --user 0 \"$path\"")
    }

    fun outcome(result: InstallShellResult): ApkInstallResult {
        val text = result.output
        if (text.contains("Success", ignoreCase = true)) return ApkInstallResult.Ok
        if (result.exitCode == 0 && text.isBlank()) return ApkInstallResult.Ok
        val reason = text.trim().ifEmpty { "exit ${result.exitCode}" }.take(200)
        return ApkInstallResult.Failed(reason)
    }
}

object ProcessInstallShell : InstallShell {
    override fun run(args: List<String>): InstallShellResult {
        val process = ProcessBuilder(args).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        val code = process.waitFor()
        return InstallShellResult(code, output)
    }
}
