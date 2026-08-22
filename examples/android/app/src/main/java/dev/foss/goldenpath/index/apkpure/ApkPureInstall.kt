package dev.foss.goldenpath.index.apkpure

import android.content.Context
import dev.foss.goldenpath.inventory.InstallMethod
import dev.foss.goldenpath.inventory.ListingInstallLive
import dev.foss.goldenpath.inventory.OneClickResult
import dev.foss.goldenpath.inventory.RemoteReleasedSource

object ApkPureInstall {
    fun run(context: Context, packageName: String, method: InstallMethod): OneClickResult =
        ListingInstallLive.run(context, packageName, RemoteReleasedSource.ApkPure, null, method)
}
