package dev.foss.goldenpath.inventory

import android.content.Context
import java.io.File

object SessionThenSystem {
    fun finish(sessionOk: Boolean, awaitOk: Boolean, system: () -> Boolean): Boolean =
        if (sessionOk && awaitOk) true else sessionOk && system()

    fun run(context: Context, files: List<File>): Boolean {
        InstallAwait.arm()
        val sessionOk = ListingInstallLive.install(context, files, InstallMethod.Session) ==
            OneClickResult.Installed
        val awaitOk = sessionOk && InstallAwait.await()
        return finish(sessionOk, awaitOk) {
            ListingInstallLive.install(context, files, InstallMethod.System) ==
                OneClickResult.Installed
        }
    }
}
