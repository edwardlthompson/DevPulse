package dev.foss.goldenpath.index.aurora

import android.content.Context
import dev.foss.goldenpath.inventory.RefreshTrace

object AuroraPlayWarm {
    fun session(context: Context) {
        val store = EncryptedAuroraAuthStore.create(context)
        val props = AuroraDeviceProps.json(context)
        val auth = AuroraAuth.refresh(store, props)
        if (auth != null) {
            AuroraPlayLive.holdSession()
            RefreshTrace.line("aurora warm ok")
        } else {
            AuroraAuth.loadOrRefresh(store, props)
            AuroraPlayLive.clearWhy()
            RefreshTrace.line("aurora warm fail")
        }
    }
}
