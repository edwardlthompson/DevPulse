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
}
