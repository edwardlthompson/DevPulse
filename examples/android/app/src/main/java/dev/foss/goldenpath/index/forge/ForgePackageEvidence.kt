package dev.foss.goldenpath.index.forge

object ForgePackageEvidence {
    fun contains(packageName: String, candidate: ForgeCandidate): Boolean {
        val haystack = listOf(
            candidate.ownerRepo,
            candidate.title,
            candidate.description.orEmpty(),
            candidate.packageId.orEmpty(),
        ).joinToString("\u0000")
        return inText(packageName, haystack)
    }

    fun inText(packageName: String, haystack: String): Boolean {
        val needles = variants(packageName)
        if (needles.isEmpty()) return false
        return needles.any { needle -> haystack.contains(needle, ignoreCase = true) }
    }

    fun variants(packageName: String): List<String> {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return emptyList()
        return listOf(pkg, pkg.replace('.', '-'), pkg.replace('.', '_')).distinct()
    }
}
