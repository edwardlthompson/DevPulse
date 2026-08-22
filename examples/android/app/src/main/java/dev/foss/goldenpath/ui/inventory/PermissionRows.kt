package dev.foss.goldenpath.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.WelcomeNeed
import dev.foss.goldenpath.inventory.WelcomeRow
import dev.foss.goldenpath.ui.theme.SpacingMd

@Composable
fun PermissionRows(
    rows: List<WelcomeRow>,
    retap: Boolean,
    onClick: (WelcomeNeed) -> Unit,
) {
    rows.forEach { row ->
        val title = when (row.need) {
            WelcomeNeed.Apps -> stringResource(R.string.inventory_title)
            WelcomeNeed.Notifications -> stringResource(R.string.refresh_notify_channel)
            WelcomeNeed.Install -> stringResource(R.string.install_method_title)
            WelcomeNeed.Usage -> stringResource(R.string.inventory_usage_grant)
        }
        Text(
            text = if (row.granted) "✓ $title" else title,
            color = if (row.granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = retap || !row.granted) { onClick(row.need) }
                .padding(vertical = SpacingMd),
        )
    }
}
