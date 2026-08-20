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
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storefront
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
import dev.foss.goldenpath.inventory.StoreClientAction
import dev.foss.goldenpath.inventory.StoreClientId
import dev.foss.goldenpath.inventory.StoreClients
import dev.foss.goldenpath.inventory.StoreUrlKind
import dev.foss.goldenpath.ui.theme.SpacingMd
import dev.foss.goldenpath.ui.theme.SpacingSm
import dev.foss.goldenpath.ui.theme.SpacingXl

@Composable
fun StoreClientSettings(modifier: Modifier = Modifier, showTitle: Boolean = true) {
    val context = LocalContext.current
    val pm = context.packageManager
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
                games = client.id == StoreClientId.Aptoide && StoreClients.isAptoideGames(pm),
                installed = StoreClients.installed(pm, client.packageName),
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
    games: Boolean,
    installed: Boolean,
) {
    val title = stringResource(titleRes(client.id))
    val action = StoreClients.action(installed, games, !client.packageName.isNullOrBlank())
    val actionLabel = stringResource(
        when (action) {
            StoreClientAction.ReplaceAptoide -> R.string.store_client_install_store
            StoreClientAction.Open -> R.string.store_client_open
            StoreClientAction.Install -> R.string.store_client_install
        },
    )
    val status = stringResource(
        when {
            games -> R.string.store_client_status_games
            client.packageName.isNullOrBlank() -> R.string.store_client_status_site
            installed -> R.string.store_client_status_installed
            else -> R.string.store_client_status_missing
        },
    )
    val row = if (games) {
        "$title · ${stringResource(R.string.store_client_wrong_aptoide)}"
    } else {
        "$title · $actionLabel"
    }
    SettingsGroup {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingMd),
        ) {
            StoreGlyph(icon = iconFor(client.id, games))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (games) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
        FilledTonalButton(
            onClick = onOpen,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = row },
        ) { Text(actionLabel) }
        if (client.urls.isNotEmpty()) {
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
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

private fun iconFor(id: StoreClientId, games: Boolean): ImageVector = when (id) {
    StoreClientId.Play -> Icons.Filled.Shop
    StoreClientId.Fdroid -> Icons.Filled.Android
    StoreClientId.Droidify -> Icons.Filled.PhoneAndroid
    StoreClientId.Izzy -> Icons.Filled.Extension
    StoreClientId.Guardian -> Icons.Filled.Shield
    StoreClientId.Calyx -> Icons.Filled.Eco
    StoreClientId.Aptoide -> if (games) Icons.Filled.SportsEsports else Icons.Filled.Storefront
    StoreClientId.ApkMirror -> Icons.Filled.PhotoLibrary
    StoreClientId.ApkPure -> Icons.Filled.GetApp
    StoreClientId.GitHub -> Icons.Filled.Code
}

private fun titleRes(id: StoreClientId): Int = when (id) {
    StoreClientId.Play -> R.string.inventory_source_play
    StoreClientId.Fdroid -> R.string.inventory_source_fdroid
    StoreClientId.Droidify -> R.string.store_client_droidify
    StoreClientId.Izzy -> R.string.inventory_source_izzy
    StoreClientId.Guardian -> R.string.inventory_source_guardian
    StoreClientId.Calyx -> R.string.inventory_source_calyx
    StoreClientId.Aptoide -> R.string.inventory_source_aptoide
    StoreClientId.ApkMirror -> R.string.inventory_source_apkmirror
    StoreClientId.ApkPure -> R.string.inventory_source_apkpure
    StoreClientId.GitHub -> R.string.inventory_source_forge
}

private fun linkRes(kind: StoreUrlKind): Int = when (kind) {
    StoreUrlKind.Play -> R.string.store_client_via_play
    StoreUrlKind.Fdroid -> R.string.store_client_via_fdroid
    StoreUrlKind.Apk -> R.string.store_client_via_apk
    StoreUrlKind.Web -> R.string.store_client_via_web
    StoreUrlKind.Repo -> R.string.store_client_via_repo
}
