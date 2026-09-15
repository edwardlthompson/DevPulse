package dev.foss.goldenpath.ui.inventory

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.InventoryCopy
import dev.foss.goldenpath.staleness.Staleness
import dev.foss.goldenpath.ui.theme.SpacingMd

@Composable
internal fun InventoryDetailAdvanced(
    app: InstalledApp,
    inventory: List<InstalledApp>,
) {
    val version = app.versionName ?: stringResource(R.string.inventory_version_unknown)
    val sdkRisk = Staleness.compatibilityWarning(app.targetSdk, Build.VERSION.SDK_INT)
    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        DetailCopy(app.packageName, app.signingSha1)
        InventoryDetailCallout(
            stringResource(R.string.inventory_callout_origin),
            stringResource(InventoryCopy.originRes(app)),
        )
        InventoryDetailCallout(stringResource(R.string.inventory_callout_installed), version)
        InventoryDetailCallout(
            stringResource(R.string.inventory_callout_installed_date),
            inventoryLocalDate(app.installedAtMs),
        )
        InventoryDetailCallout(stringResource(R.string.inventory_callout_latest), inventoryLatestText(app))
        InventoryDetailCallout(
            label = stringResource(R.string.inventory_callout_sdk),
            value = stringResource(R.string.inventory_sdk, app.minSdk, app.targetSdk),
            warn = sdkRisk,
        )
        if (sdkRisk) {
            Text(text = stringResource(R.string.inventory_sdk_risk), color = MaterialTheme.colorScheme.error)
        }
        DetailForget(app.packageName)
        DetailPasteRepo(app.packageName)
        DetailDirectApk(app.packageName)
        DetailGithubOpts(app.packageName)
        UpdateNotesSection(app.packageName)
        DownloadUpdateSection(app)
        VersionHistorySection(app)
        AlternativesSection(app = app, inventory = inventory)
    }
}
