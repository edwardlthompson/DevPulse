package dev.foss.goldenpath.feedback

object FeedbackCompose {
    fun markdown(kind: String, body: String, extra: String = ""): String {
        val cleanKind = kind.trim().ifEmpty { "bug" }
        val cleanBody = body.trim()
        val extraBlock = extra.trim().takeIf { it.isNotEmpty() }?.let { "\n\n$it" }.orEmpty()
        return "## $cleanKind\n\n$cleanBody$extraBlock"
    }
}
