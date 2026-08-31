package dev.foss.goldenpath.inventory

object AppVersionRanking {
    fun rankAndCap(
        items: List<AppVersionItem>,
        installedVersion: String?,
        installedCode: Long = 0,
        maxCount: Int = 5,
    ): List<AppVersionItem> {
        val cleanInstalled = installedVersion?.trim().orEmpty()
        val unique = items
            .filter { it.versionName.isNotBlank() }
            .groupBy { VersionCompare.canonical(it.versionName) }
            .values
            .map { group ->
                group.maxWithOrNull(
                    compareBy<AppVersionItem> { it.downloadUrl != null }
                        .thenBy { it.releasedAtMs ?: 0L }
                        .thenBy { it.source != RemoteReleasedSource.None }
                ) ?: group.first()
            }
            .map { item ->
                item.copy(state = resolveState(item.versionName, item.versionCode, cleanInstalled, installedCode))
            }
            .sortedWith { a, b ->
                val cmp = VersionCompare.compare(a.versionName, b.versionName)
                if (cmp != 0) -cmp else (b.releasedAtMs ?: 0L).compareTo(a.releasedAtMs ?: 0L)
            }

        return unique.take(maxCount)
    }

    fun resolveState(
        candidateVersion: String?,
        candidateCode: Long?,
        installedVersion: String?,
        installedCode: Long = 0,
    ): AppVersionState {
        val cand = candidateVersion?.trim().orEmpty()
        val inst = installedVersion?.trim().orEmpty()
        if (cand.isEmpty()) return AppVersionState.Current
        if (inst.isEmpty()) return AppVersionState.Newer
        if (VersionCompare.compare(cand, inst) == 0 || cand == inst) {
            return AppVersionState.Current
        }
        if (VersionCompare.isNewer(cand, inst, installedCode)) {
            return AppVersionState.Newer
        }
        return AppVersionState.Rollback
    }
}
