package dev.foss.goldenpath.inventory

import android.content.Context
import android.content.pm.ApplicationInfo
import java.io.File

object SignerReplaceLive {
    fun clash(context: Context, packageName: String, files: List<File>): Boolean {
        val apk = files.firstOrNull()?.takeIf { it.isFile } ?: return false
        val inspect = ApkArchiveIdentity.inspect(context.packageManager, apk)
        val installed = ApkArchiveIdentity.installed(context.packageManager, packageName) ?: return false
        return SignerClash.offer(
            packageName,
            inspect.packageName,
            inspect.signers,
            installed.signers,
            systemApp(context, packageName),
        )
    }

    fun systemApp(context: Context, packageName: String): Boolean = runCatching {
        context.packageManager.getApplicationInfo(packageName, 0).flags and ApplicationInfo.FLAG_SYSTEM != 0
    }.getOrDefault(true)
}
