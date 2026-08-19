package dev.foss.goldenpath.about

import android.content.Context
import org.json.JSONObject

data class DonationLink(val label: String, val url: String)

data class DonationsConfig(
    val enabled: Boolean,
    val message: String,
    val links: List<DonationLink>,
)

object DonationsLoader {
    fun load(context: Context): DonationsConfig {
        return try {
            val json = context.assets.open("donations.json").bufferedReader().use { it.readText() }
            val root = JSONObject(json)
            val enabled = root.optBoolean("enabled", false)
            val message = root.optString("message", "")
            val links = mutableListOf<DonationLink>()
            val arr = root.optJSONArray("links")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    links.add(DonationLink(item.optString("label"), item.optString("url")))
                }
            }
            DonationsConfig(enabled, message, links)
        } catch (_: Exception) {
            DonationsConfig(enabled = false, message = "", links = emptyList())
        }
    }
}
