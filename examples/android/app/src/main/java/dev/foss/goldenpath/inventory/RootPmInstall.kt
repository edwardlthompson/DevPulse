package dev.foss.goldenpath.inventory

object RootPmInstall {
    private val unsafe = Regex("[\"'\\n\\r;|&$`]")

    @Volatile
    private var suCached: Boolean? = null

    fun available(shell: InstallShell = ProcessInstallShell): Boolean {
        suCached?.let { return it }
        val ok = shell.run(listOf("su", "-c", "id")).exitCode == 0
        suCached = ok
        return ok
    }

    fun resetAvailable() {
        suCached = null
    }

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
        return try {
            val process = ProcessBuilder(args).redirectErrorStream(true).start()
            val probe = args.getOrNull(2) == "id"
            if (probe) {
                val finished = process.waitFor(800, java.util.concurrent.TimeUnit.MILLISECONDS)
                val output = process.inputStream.bufferedReader().use { it.readText() }
                if (!finished) {
                    process.destroyForcibly()
                    return InstallShellResult(124, "timeout")
                }
                return InstallShellResult(process.exitValue(), output)
            }
            val output = process.inputStream.bufferedReader().use { it.readText() }
            InstallShellResult(process.waitFor(), output)
        } catch (e: Exception) {
            InstallShellResult(127, e.message.orEmpty().take(200))
        }
    }
}
