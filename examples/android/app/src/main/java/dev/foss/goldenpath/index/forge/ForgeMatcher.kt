package dev.foss.goldenpath.index.forge

object ForgeMatcher {
    @Suppress("UNUSED_PARAMETER")
    fun rank(packageName: String, label: String, candidates: List<ForgeCandidate>): ForgeMatch? {
        val usable = candidates.filter { !it.archived }
        exact(packageName, usable, MatchConfidence.ExactPackage)?.let { return it }
        exact(gradleId(packageName), usable, MatchConfidence.GradleId)?.let { return it }
        val evidence = usable.firstOrNull { ForgePackageEvidence.contains(packageName, it) } ?: return null
        return ForgeMatch(evidence, MatchConfidence.PackageInRepo)
    }

    private fun exact(
        id: String,
        candidates: List<ForgeCandidate>,
        confidence: MatchConfidence,
    ): ForgeMatch? {
        val hit = candidates.firstOrNull { it.packageId.equals(id, ignoreCase = true) } ?: return null
        return ForgeMatch(hit, confidence)
    }

    private fun gradleId(packageName: String): String = packageName.replace('.', ':')
}

object ForgeBackoff {
    fun nextDelayMs(statusCode: Int, attempt: Int): Long? {
        if (statusCode != 403 && statusCode != 429) return null
        val step = attempt.coerceAtLeast(1)
        return 1_000L * step * step
    }
}
