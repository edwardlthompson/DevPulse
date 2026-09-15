package dev.foss.goldenpath.feedback

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object FeedbackGithub {
    const val REPO = "edwardlthompson/DevPulse"

    fun issueUrl(title: String, body: String, label: String = "bug"): String {
        val enc = StandardCharsets.UTF_8.name()
        val q = buildString {
            append("https://github.com/$REPO/issues/new")
            append("?title=").append(URLEncoder.encode(title.trim().ifEmpty { "DevPulse" }, enc))
            append("&body=").append(URLEncoder.encode(body.trim(), enc))
            append("&labels=").append(URLEncoder.encode(label.trim().ifEmpty { "bug" }, enc))
        }
        return q
    }
}
