package dev.foss.goldenpath.feedback

object FeedbackDeepLink {
    const val SCHEME = "devpulse"
    const val HOST = "feedback"

    fun matches(scheme: String?, host: String?): Boolean =
        scheme == SCHEME && host == HOST
}
