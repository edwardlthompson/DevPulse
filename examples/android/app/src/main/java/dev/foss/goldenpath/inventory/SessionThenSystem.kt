package dev.foss.goldenpath.inventory

import android.content.Context
import java.io.File

object SessionThenSystem {
    const val POLL_MS = 300L

    fun finish(sessionOk: Boolean, awaitOk: Boolean, system: () -> Boolean): Boolean =
        if (sessionOk && awaitOk) true else sessionOk && system()

    fun run(context: Context, files: List<File>): Boolean {
        InstallAwait.arm()
        val sessionOk = ListingInstallLive.install(context, files, InstallMethod.Session) ==
            OneClickResult.Installed
        if (sessionOk && waitSilent()) return true
        if (UpdateAllCancel.requested()) return false
        // Session already showed a confirm UI — do not open a second System installer.
        if (InstallAwait.pending) return noteUserAbort()
        RefreshTrace.line("update all session fallback")
        abandonOwnSessions(context)
        if (RootPmInstall.available() && RootSessionInstall.run(files)) {
            RefreshTrace.line("update all root install")
            return true
        }
        return finish(sessionOk, false) {
            ListingInstallLive.install(context, files, InstallMethod.System) ==
                OneClickResult.Installed
        }
    }

    internal fun timedOut(elapsedMs: Long, timeoutMs: Long = InstallAwait.TIMEOUT_MS): Boolean =
        elapsedMs >= timeoutMs

    /** User Cancel / Session failure stops the whole Update All queue. */
    internal fun noteUserAbort(): Boolean {
        UpdateAllCancel.request()
        return false
    }

    private fun waitSilent(): Boolean {
        val start = System.currentTimeMillis()
        while (true) {
            if (InstallAwait.await(POLL_MS)) return true
            if (InstallAwait.settled()) return noteUserAbort()
            if (UpdateAllCancel.requested()) return false
            if (timedOut(System.currentTimeMillis() - start)) return false
        }
    }

    private fun abandonOwnSessions(context: Context) {
        val installer = context.packageManager.packageInstaller
        installer.mySessions.forEach { info ->
            runCatching { installer.abandonSession(info.sessionId) }
        }
    }
}
