package dev.foss.goldenpath.inventory

data class PulseHistoryView(
    val kind: String,
    val atMs: Long,
    val wallMs: Long,
    val count: Int,
    val locations: Int?,
    val outlets: List<Pair<String, Long>>,
    val notes: List<Pair<String, String>>,
)

object PulseHistoryFormat {
    private val skipOutlet = setOf("locations", "red", "unknown", "result", "installed", "downloaded", "failDl", "failIns")

    fun newestFirst(rows: List<PulseHistoryRow>): List<PulseHistoryRow> =
        rows.sortedByDescending { it.atMs }

    fun view(row: PulseHistoryRow): PulseHistoryView {
        val parts = row.extra.split(';').mapNotNull(::pair)
        val locations = parts.firstOrNull { it.first == "locations" }?.second?.toIntOrNull()
        val outlets = parts.mapNotNull { (key, value) ->
            if (key in skipOutlet) return@mapNotNull null
            val ms = value.toLongOrNull() ?: return@mapNotNull null
            key to ms
        }
        val notes = parts.filter { it.first in skipOutlet && it.first != "locations" }
        return PulseHistoryView(row.kind, row.atMs, row.wallMs, row.count, locations, outlets, notes)
    }

    private fun pair(bit: String): Pair<String, String>? {
        val eq = bit.indexOf('=')
        if (eq <= 0) return null
        val key = bit.substring(0, eq).trim()
        val value = bit.substring(eq + 1).trim()
        return if (key.isEmpty() || value.isEmpty()) null else key to value
    }
}
