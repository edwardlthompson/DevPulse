package dev.foss.goldenpath.inventory

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UpdateNotes(
    val text: String,
    val source: RemoteReleasedSource,
)

object UpdateNotesText {
    fun unescape(raw: String): String =
        raw.replace("\\n", "\n").replace("\\t", "\t").replace("\\\"", "\"").replace("\\\\", "\\").trim()

    fun take(raw: String?): String? = raw?.let(::unescape)?.takeIf { it.isNotEmpty() }
}

object UpdateNotesMemory {
    @Volatile
    var byPackage: Map<String, UpdateNotes> = emptyMap()
        private set

    private val lock = Any()
    private val revisionState = MutableStateFlow(0)
    val revision: StateFlow<Int> = revisionState.asStateFlow()

    fun put(packageName: String, notes: UpdateNotes) {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || notes.text.isBlank()) return
        synchronized(lock) {
            byPackage = byPackage + (pkg to notes)
            revisionState.value += 1
        }
    }

    fun putIfAbsent(packageName: String, notes: UpdateNotes) {
        if (get(packageName) == null) put(packageName, notes)
    }

    fun get(packageName: String): UpdateNotes? = byPackage[packageName]

    fun clear() {
        synchronized(lock) {
            byPackage = emptyMap()
            revisionState.value += 1
        }
    }
}
