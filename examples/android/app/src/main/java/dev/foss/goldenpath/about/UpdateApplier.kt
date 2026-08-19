package dev.foss.goldenpath.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object UpdateApplier {
    fun buildInstallIntent(context: Context, apkFile: File): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile,
        )
        return buildInstallIntent(context, uri)
    }

    /** Testable intent builder without FileProvider (Robolectric-safe on Windows). */
    internal fun buildInstallIntent(context: Context, apkUri: Uri): Intent =
        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }

    fun launchApkInstall(context: Context, apkFile: File) {
        context.startActivity(buildInstallIntent(context, apkFile))
    }
}
