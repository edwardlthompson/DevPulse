package dev.foss.goldenpath.inventory

import android.content.Context
import android.content.Intent
import android.net.Uri

object PlayStoreIntent {
    const val STORE_PACKAGE = "com.android.vending"

    fun marketUri(packageName: String): String = "market://details?id=$packageName"

    fun viewIntent(packageName: String): Intent =
        Intent(Intent.ACTION_VIEW, Uri.parse(marketUri(packageName)))
            .setPackage(STORE_PACKAGE)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun open(context: Context, packageName: String) {
        runCatching { context.startActivity(viewIntent(packageName)) }.onFailure {
            val web = UpdateUrls.play(packageName)
            runCatching {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(web)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
        }
    }
}
