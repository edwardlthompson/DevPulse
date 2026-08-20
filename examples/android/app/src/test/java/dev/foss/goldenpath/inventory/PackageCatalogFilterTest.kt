package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Test

class PackageCatalogFilterTest {
    private val user = sampleApp("app.user", "Alpha", isSystemApp = false)
    private val system = sampleApp("app.system", "Zebra", isSystemApp = true)
    private val catalog = PackageCatalog { listOf(system, user) }

    @Test
    fun fakeCatalogHidesSystemApps() {
        val visible = InventoryFilter.visibleApps(catalog.listInstalled(), includeSystem = false)
        assertEquals(listOf(user), visible)
    }

    @Test
    fun fakeCatalogIncludesSystemAppsWhenToggled() {
        val visible = InventoryFilter.visibleApps(catalog.listInstalled(), includeSystem = true)
        assertEquals(listOf(system, user), visible)
    }

    @Test
    fun fakeCatalogSortsVisibleUserAppsByLabel() {
        val extra = sampleApp("app.beta", "beta", isSystemApp = false)
        val mixed = PackageCatalog { listOf(extra, system, user) }
        val visible = InventoryFilter.sortedByLabel(
            InventoryFilter.visibleApps(mixed.listInstalled(), includeSystem = false),
        )
        assertEquals(listOf(user, extra), visible)
    }
}
