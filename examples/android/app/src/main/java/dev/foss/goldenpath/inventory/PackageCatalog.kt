package dev.foss.goldenpath.inventory

fun interface PackageCatalog {
    fun listInstalled(): List<InstalledApp>
}
