package dev.foss.goldenpath.about

/**
 * Pure update-status evaluation for About screen (network-free; unit-testable).
 */
object UpdateStatusEvaluator {
    sealed class Result {
        data object Current : Result()
        data class Available(val version: String) : Result()
    }

    fun evaluate(currentVersion: String, latestTag: String?): Result {
        val latest = latestTag?.let { UpdateChecker.parseVersion(it) } ?: return Result.Current
        return if (UpdateChecker.isNewer(currentVersion, latest)) {
            Result.Available(latest)
        } else {
            Result.Current
        }
    }
}
