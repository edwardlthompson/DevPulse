package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Test

class InventoryPresentTest {
    @Test
    fun updatesOnlyHidesCurrentApps() {
        val current = sampleApp("app.ok", "Ok", remoteVersionName = "1.0")
        val update = sampleApp("app.up", "Up", remoteVersionName = "2.0")
        val visible = InventoryPresent.visible(
            apps = listOf(current, update),
            includeSystem = false,
            query = "",
            staleOnly = false,
            updatesOnly = true,
            sortMode = InventorySortMode.Name,
            usageByPackage = emptyMap(),
            nowMs = 1_700_000_000_000L,
        )
        assertEquals(listOf("app.up"), visible.map { it.packageName })
    }

    @Test
    fun githubOnlyKeepsListedForge() {
        val listed = sampleApp(
            "app.git",
            "Git",
            latestListings = listOf(
                UpdateLink(RemoteReleasedSource.Forge, url = "https://github.com/a/b/releases", listed = true, known = true),
            ),
        )
        val other = sampleApp("app.other", "Other")
        val visible = InventoryPresent.visible(
            apps = listOf(listed, other),
            includeSystem = false,
            query = "",
            staleOnly = false,
            updatesOnly = false,
            githubOnly = true,
            sortMode = InventorySortMode.Name,
            usageByPackage = emptyMap(),
            nowMs = 1_700_000_000_000L,
        )
        assertEquals(listOf("app.git"), visible.map { it.packageName })
    }

    @Test
    fun sourceFiltersKeepGuardianOrArchive() {
        val guardian = sampleApp(
            "app.guard",
            "Guard",
            latestListings = listOf(
                UpdateLink(RemoteReleasedSource.Guardian, listed = true, known = true),
            ),
        )
        val archive = sampleApp(
            "app.arch",
            "Arch",
            latestListings = listOf(
                UpdateLink(RemoteReleasedSource.Archive, listed = true, known = true),
            ),
        )
        val other = sampleApp("app.other", "Other")
        val visible = InventoryPresent.visible(
            apps = listOf(guardian, archive, other),
            includeSystem = false,
            query = "",
            staleOnly = false,
            updatesOnly = false,
            sourceFilters = setOf(RemoteReleasedSource.Guardian, RemoteReleasedSource.Archive),
            sortMode = InventorySortMode.Name,
            usageByPackage = emptyMap(),
            nowMs = 1_700_000_000_000L,
        )
        assertEquals(listOf("app.arch", "app.guard"), visible.map { it.packageName })
    }
}
