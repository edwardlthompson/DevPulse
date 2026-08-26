package dev.foss.goldenpath.index.forge

object PackageIdAliases {
    val suffixes = listOf(".fdroid", ".debug", ".nightly")

    fun keys(packageName: String): List<String> {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || '.' !in pkg) return emptyList()
        val out = linkedSetOf(pkg)
        suffixes.forEach { out += pkg + it }
        strip(pkg)?.let { out += it }
        return out.toList()
    }

    fun strip(packageName: String): String? {
        val pkg = packageName.trim()
        val suffix = suffixes.firstOrNull { pkg.endsWith(it) && pkg.length > it.length } ?: return null
        val base = pkg.removeSuffix(suffix)
        return base.takeIf { '.' in it }
    }

    fun hint(packageName: String, library: Map<String, GithubHint>): GithubHint? {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return null
        library[pkg]?.let { return it }
        val repos = keys(pkg).drop(1).mapNotNull { key ->
            library[key]?.ownerRepo?.trim()?.takeIf { it.contains('/') }
        }.distinct()
        if (repos.size != 1) return null
        return GithubHint(repos.single())
    }

    fun expand(wanted: Set<String>, library: Map<String, GithubHint>): Map<String, GithubHint> {
        if (wanted.isEmpty()) return library
        val out = library.toMutableMap()
        wanted.forEach { pkg ->
            if (pkg in out) return@forEach
            hint(pkg, library)?.let { out[pkg] = it }
        }
        return out
    }
}
