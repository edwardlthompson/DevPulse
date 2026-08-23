package dev.foss.goldenpath.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import dev.foss.goldenpath.index.forge.EncryptedForgeTokenStore
import dev.foss.goldenpath.inventory.InventoryPreferences
import dev.foss.goldenpath.ui.forge.ForgeTokenGuide
import dev.foss.goldenpath.ui.forge.ForgeTokenSave
import dev.foss.goldenpath.ui.forge.LeftoverTokenFields
import dev.foss.goldenpath.ui.theme.SpacingMd
import kotlinx.coroutines.launch

@Composable
fun ForgeLookupSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { InventoryPreferences(context) }
    val scope = rememberCoroutineScope()
    val enabled by prefs.forgeLookupEnabled.collectAsStateWithLifecycle(true)
    val searchUnknowns by prefs.forgeLookupSearchUnknowns.collectAsStateWithLifecycle(false)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(text = stringResource(R.string.forge_lookup_title), style = MaterialTheme.typography.titleMedium)
        Text(text = stringResource(R.string.forge_lookup_body), style = MaterialTheme.typography.bodySmall)
        PreferenceSwitch(
            label = stringResource(R.string.forge_lookup_enable),
            checked = enabled,
            onCheckedChange = { value -> scope.launch { prefs.setForgeLookupEnabled(value) } },
        )
        Text(text = stringResource(R.string.forge_lookup_leftover_body), style = MaterialTheme.typography.bodySmall)
        Text(text = stringResource(R.string.forge_lookup_search_unknowns_body), style = MaterialTheme.typography.bodySmall)
        PreferenceSwitch(
            label = stringResource(R.string.forge_lookup_search_unknowns),
            checked = searchUnknowns,
            onCheckedChange = { value -> scope.launch { prefs.setForgeLookupSearchUnknowns(value) } },
        )
        HostBackoffLine()
        ForgeTokenGuide()
        ForgeTokenSave(store = remember { EncryptedForgeTokenStore.wrap(context) })
        LeftoverTokenFields(remember { EncryptedForgeTokenStore.create(context) })
    }
}
