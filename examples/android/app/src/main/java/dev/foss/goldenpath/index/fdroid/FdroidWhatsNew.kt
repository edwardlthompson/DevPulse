package dev.foss.goldenpath.index.fdroid

import dev.foss.goldenpath.inventory.UpdateNotesText

object FdroidWhatsNew {
    private val field = Regex(""""whatsNew"\s*:\s*"((?:\\.|[^"\\])*)"""")

    fun parse(chunk: String): String? {
        val raw = field.find(chunk)?.groupValues?.get(1) ?: return null
        return UpdateNotesText.take(raw)
    }
}
