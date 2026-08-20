package dev.foss.goldenpath.inventory

object AppOriginResolver {
    private val playInstallers = setOf(
        "com.android.vending",
        "com.google.android.feedback",
        "com.aurora.store",
    )
    private val fdroidInstallers = setOf(
        "org.fdroid.fdroid",
        "org.fdroid.basic",
        "org.fdroid.nearby",
    )
    private val extraInstallers = setOf(
        "org.fdroid.fdroid.privileged",
        "nya.kitsunyan.foxydroid",
        "com.aurora.adroid",
        "com.looker.droidify",
        "com.machiav3lli.fdroid",
    )

    fun fromInstaller(installerPackageName: String?): AppOrigin {
        val installer = installerPackageName?.trim().orEmpty()
        return when {
            installer in playInstallers -> AppOrigin.Play
            installer in fdroidInstallers -> AppOrigin.Fdroid
            installer in extraInstallers -> AppOrigin.ExtraRepo
            else -> AppOrigin.SideloadedUnknown
        }
    }

    fun refine(local: AppOrigin, remote: RemoteReleasedSource?): AppOrigin {
        if (local == AppOrigin.Play) return AppOrigin.Play
        if (local == AppOrigin.Fdroid) return AppOrigin.Fdroid
        return when (remote) {
            RemoteReleasedSource.Fdroid -> AppOrigin.Fdroid
            RemoteReleasedSource.ExtraRepo,
            RemoteReleasedSource.Izzy,
            RemoteReleasedSource.Guardian,
            RemoteReleasedSource.Calyx,
            RemoteReleasedSource.Archive,
            RemoteReleasedSource.Aptoide,
            -> AppOrigin.ExtraRepo
            else -> if (local == AppOrigin.Unknown) AppOrigin.SideloadedUnknown else local
        }
    }
}
