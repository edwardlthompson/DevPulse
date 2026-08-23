package dev.foss.goldenpath.ui.inventory

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.ListingFit
import dev.foss.goldenpath.inventory.ListingNewer
import dev.foss.goldenpath.inventory.UpdateLink

@Composable
internal fun ListingExtrasLine(
    link: UpdateLink,
    installedVersion: String?,
    deviceSdk: Int = 0,
    deviceAbis: Set<String> = emptySet(),
) {
    if (!ListingNewer.allow(link.versionName, installedVersion) && link.listed) {
        Text(
            text = stringResource(R.string.about_update_current),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
    val size = link.sizeBytes?.takeIf { it > 0L }?.toString()
    if (size != null) {
        Text(
            text = stringResource(R.string.inventory_listing_line, link.versionName ?: size, size),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    if (!ListingFit.sdkOk(link.minSdk, deviceSdk) && link.minSdk != null) {
        Text(
            text = stringResource(R.string.inventory_sdk, link.minSdk, deviceSdk),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
    if (!ListingFit.abiOk(link.nativeCodes, deviceAbis)) {
        Text(
            text = link.nativeCodes.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
    if (link.antiFeatures.isNotEmpty()) {
        Text(
            text = link.antiFeatures.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
