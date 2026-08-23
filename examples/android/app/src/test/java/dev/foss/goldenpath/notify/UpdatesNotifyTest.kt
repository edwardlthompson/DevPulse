package dev.foss.goldenpath.notify

import dev.foss.goldenpath.inventory.InstalledApp
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdatesNotifyTest {
    @Test
    fun countIsZeroWhenNoUsableUpdate() {
        val app = InstalledApp(
            packageName = "app.x",
            label = "X",
            versionName = "2.0",
            versionCode = 2L,
            lastUpdateTimeMs = 1L,
            firstInstallTimeMs = 1L,
            minSdk = 26,
            targetSdk = 34,
            isSystemApp = false,
        )
        assertEquals(0, UpdatesNotify.count(listOf(app)))
    }
}
