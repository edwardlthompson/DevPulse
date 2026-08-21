package dev.foss.goldenpath.index.fdroid

object FdroidPackageParser {
    private val suggestedCode = Regex(""""suggestedVersionCode"\s*:\s*(\d+)""")
    private val versionPair = Regex(
        """"versionName"\s*:\s*"([^"]+)"\s*,\s*"versionCode"\s*:\s*(\d+)""",
    )

    fun parse(packageName: String, json: String, repoId: String): FdroidAppRecord? {
        val body = json.trim()
        if (body.isEmpty() || body == "{}" || !body.contains(packageName)) return null
        val suggested = suggestedCode.find(body)?.groupValues?.get(1)
        val pairs = versionPair.findAll(body).map { it.groupValues[1] to it.groupValues[2] }.toList()
        val name = pairs.firstOrNull { it.second == suggested }?.first
            ?: pairs.firstOrNull()?.first
        return FdroidAppRecord(
            packageName = packageName,
            lastUpdatedMs = null,
            sourceCode = null,
            repoId = repoId,
            suggestedVersionName = name,
        )
    }
}
