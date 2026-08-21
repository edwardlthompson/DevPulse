package dev.foss.goldenpath.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.StoreClient
import dev.foss.goldenpath.inventory.StoreClientId
import dev.foss.goldenpath.inventory.StoreClients
import dev.foss.goldenpath.inventory.StoreUrlKind
import dev.foss.goldenpath.ui.theme.SpacingMd
import dev.foss.goldenpath.ui.theme.SpacingSm
import dev.foss.goldenpath.ui.theme.SpacingXl

@Composable
fun StoreClientSettings(modifier: Modifier = Modifier, showTitle: Boolean = true) {
    val context = LocalContext.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        if (showTitle) {
            Text(text = stringResource(R.string.store_clients_title), style = MaterialTheme.typography.titleMedium)
        }
        SettingsGroup {
            Text(
                text = stringResource(R.string.store_clients_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        StoreClients.all().forEach { client ->
            StoreClientCard(
                client = client,
                onOpen = { StoreClients.open(context, client) },
                onLink = { StoreClients.open(context, client, it) },
                installed = StoreClients.installed(context.packageManager, client.packageName),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StoreClientCard(
    client: StoreClient,
    onOpen: () -> Unit,
    onLink: (String) -> Unit,
    installed: Boolean,
) {
    val title = stringResource(titleRes(client.id))
    val actionLabel = stringResource(R.string.store_client_open)
    val status = stringResource(
        when {
            client.packageName.isNullOrBlank() -> R.string.store_client_status_site
            installed -> R.string.store_client_status_installed
            else -> R.string.store_client_status_missing
        },
    )
    val row = "$title · $actionLabel"
    SettingsGroup {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingMd),
        ) {
            StoreGlyph(icon = iconFor(client.id))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        FilledTonalButton(
            onClick = onOpen,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = row },
        ) { Text(actionLabel) }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingSm)) {
            client.urls.forEach { link ->
                val label = stringResource(linkRes(link.kind))
                AssistChip(
                    onClick = { onLink(link.url) },
                    label = { Text(label) },
                    modifier = Modifier.semantics { contentDescription = "$title $label" },
                )
            }
        }
    }
}

@Composable
private fun StoreGlyph(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(SpacingXl + SpacingSm)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

private fun iconFor(id: StoreClientId): ImageVector = when (id) {
    StoreClientId.Play -> Icons.Filled.Shop
    StoreClientId.ApkMirror -> Icons.Filled.PhotoLibrary
}

private fun titleRes(id: StoreClientId): Int = when (id) {
    StoreClientId.Play -> R.string.inventory_source_play
    StoreClientId.ApkMirror -> R.string.inventory_source_apkmirror
}

private fun linkRes(kind: StoreUrlKind): Int = when (kind) {
    StoreUrlKind.Play -> R.string.store_client_via_play
    StoreUrlKind.Web -> R.string.store_client_via_web
}
