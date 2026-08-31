package dev.foss.goldenpath.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.R
import dev.foss.goldenpath.about.ProductUpdate
import dev.foss.goldenpath.settings.SettingsHubRow
import dev.foss.goldenpath.settings.SettingsNav
import dev.foss.goldenpath.settings.SettingsPage
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.refresh.highRefreshScroll
import dev.foss.goldenpath.ui.theme.SpacingMd

@Composable
fun SettingsHub(
    onOpenPage: (SettingsPage) -> Unit,
    onAboutOpen: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .highRefreshScroll()
            .padding(SpacingMd)
            .bottomInsetPadding(),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(text = stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall)
        SettingsGroup {
            SettingsNav.hubRows().forEachIndexed { index, row ->
                if (index > 0) HorizontalDivider()
                val title = stringResource(row.titleRes)
                val summary = stringResource(row.summaryRes)
                val label = "$title. $summary"
                ListItem(
                    headlineContent = { Text(title) },
                    supportingContent = { Text(summary) },
                    leadingContent = {
                        Icon(imageVector = iconFor(row), contentDescription = null)
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = label }
                        .clickable(role = Role.Button) {
                            val page = row.page
                            if (page == null) onAboutOpen() else onOpenPage(page)
                        },
                )
            }
        }
        val uriHandler = LocalUriHandler.current
        val donate = stringResource(R.string.about_donate)
        TextButton(
            onClick = { runCatching { uriHandler.openUri(ProductUpdate.VENMO_URL) } },
            modifier = Modifier.semantics { contentDescription = donate },
        ) { Text(donate) }
        val close = stringResource(R.string.settings_close)
        TextButton(
            onClick = onClose,
            modifier = Modifier.semantics { contentDescription = close },
        ) { Text(close) }
    }
}

private fun iconFor(row: SettingsHubRow): ImageVector = when (row.page) {
    SettingsPage.Appearance -> Icons.Filled.Palette
    SettingsPage.Permissions -> Icons.Filled.Security
    SettingsPage.Inventory -> Icons.Filled.Apps
    SettingsPage.Ideas -> Icons.Filled.Lightbulb
    SettingsPage.History -> Icons.Filled.History
    SettingsPage.Updates -> Icons.Filled.SystemUpdate
    SettingsPage.Sources -> Icons.Filled.TravelExplore
    SettingsPage.Stores -> Icons.Filled.Store
    null -> Icons.Filled.Info
}
