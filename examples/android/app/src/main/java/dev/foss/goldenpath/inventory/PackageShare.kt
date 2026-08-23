package dev.foss.goldenpath.inventory

object PackageShare {
    fun line(packageName: String, signingSha1: String?): String =
        listOf(packageName.trim(), signingSha1?.trim().orEmpty())
            .filter { it.isNotEmpty() }
            .joinToString(" ")
}
