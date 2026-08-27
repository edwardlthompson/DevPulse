package dev.foss.goldenpath.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.index.forge.EncryptedForgeTokenStore
import dev.foss.goldenpath.ui.forge.ForgeTokenGuide
import dev.foss.goldenpath.ui.forge.ForgeTokenSave
import dev.foss.goldenpath.ui.forge.LeftoverTokenFields
import dev.foss.goldenpath.ui.theme.SpacingMd

@Composable
fun ForgeLookupSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(text = stringResource(R.string.forge_lookup_body), style = MaterialTheme.typography.bodySmall)
        Text(text = stringResource(R.string.forge_lookup_leftover_body), style = MaterialTheme.typography.bodySmall)
        Text(text = stringResource(R.string.forge_lookup_search_unknowns_body), style = MaterialTheme.typography.bodySmall)
        HostBackoffLine()
        ForgeTokenGuide()
        ForgeTokenSave(store = remember { EncryptedForgeTokenStore.wrap(context) })
        LeftoverTokenFields(remember { EncryptedForgeTokenStore.create(context) })
    }
}
