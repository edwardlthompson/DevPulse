package dev.foss.goldenpath.inventory

object AuroraPlayWhy {
    fun of(error: Throwable?): InstallWhy {
        var at = error
        while (at != null) {
            val blob = "${at.javaClass.simpleName} ${at.message.orEmpty()}"
            if (blob.contains("AppNotPurchased")) return InstallWhy.PlayPurchase
            at = at.cause
        }
        return InstallWhy.NoFile
    }

    fun headersBroken(error: Throwable?): Boolean {
        var at = error
        while (at != null) {
            val where = at.stackTrace.firstOrNull()?.className.orEmpty()
            if (at is NullPointerException && (
                    where.contains("HeaderProvider") ||
                        at.message.orEmpty().contains("getDefaultHeaders")
                    )
            ) {
                return true
            }
            at = at.cause
        }
        return false
    }
}
