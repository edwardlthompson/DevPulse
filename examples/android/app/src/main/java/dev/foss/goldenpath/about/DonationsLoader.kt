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
            parse(context.assets.open("donations.json").bufferedReader().use { it.readText() })
        } catch (_: Exception) {
            DonationsConfig(enabled = false, message = "", links = emptyList())
        }
    }

    fun parse(json: String): DonationsConfig {
        val root = JSONObject(json)
        val links = mutableListOf<DonationLink>()
        val arr = root.optJSONArray("links")
        if (arr != null) {
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                links.add(DonationLink(item.optString("label"), item.optString("url")))
            }
        }
        return DonationsConfig(root.optBoolean("enabled", false), root.optString("message", ""), links)
    }
}
