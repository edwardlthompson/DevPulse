package dev.foss.goldenpath.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.StoreClientId
import dev.foss.goldenpath.inventory.StoreClients
import dev.foss.goldenpath.inventory.StoreUrlKind
import dev.foss.goldenpath.ui.theme.SpacingMd

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StoreClientSettings(modifier: Modifier = Modifier, showTitle: Boolean = true) {
    val context = LocalContext.current
    val pm = context.packageManager
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        if (showTitle) {
            Text(text = stringResource(R.string.store_clients_title), style = MaterialTheme.typography.titleMedium)
        }
        Text(text = stringResource(R.string.store_clients_body), style = MaterialTheme.typography.bodySmall)
        StoreClients.all().forEach { client ->
            val title = stringResource(titleRes(client.id))
            val games = client.id == StoreClientId.Aptoide && StoreClients.isAptoideGames(pm)
            val installed = StoreClients.installed(pm, client.packageName)
            val action = stringResource(
                when {
                    games -> R.string.store_client_wrong_aptoide
                    installed -> R.string.store_client_open
                    else -> R.string.store_client_install
                },
            )
            val row = "$title · $action"
            TextButton(
                onClick = { StoreClients.open(context, client) },
                modifier = Modifier.semantics { contentDescription = row },
            ) { Text(row) }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
                client.urls.forEach { link ->
                    val label = stringResource(linkRes(link.kind))
                    TextButton(
                        onClick = { StoreClients.open(context, client, link.url) },
                        modifier = Modifier.semantics { contentDescription = "$title $label" },
                    ) { Text(label) }
                }
            }
        }
    }
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
