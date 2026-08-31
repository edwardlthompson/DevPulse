package dev.foss.goldenpath.index.aurora

import android.content.Context
import dev.foss.goldenpath.inventory.RefreshTrace

object AuroraPlayWarm {
    fun session(context: Context, forceRefresh: Boolean = true) {
        val store = EncryptedAuroraAuthStore.create(context)
        val props = AuroraDeviceProps.json(context)
        val auth = if (forceRefresh) {
            AuroraAuth.refresh(store, props) ?: AuroraAuth.loadOrRefresh(store, props)
        } else {
            AuroraAuth.loadOrRefresh(store, props)
        }
        if (auth != null) {
            AuroraPlayLive.holdSession()
            RefreshTrace.line("aurora warm ok")
        } else {
            AuroraPlayLive.clearWhy()
            RefreshTrace.line("aurora warm fail")
        }
    }
}
