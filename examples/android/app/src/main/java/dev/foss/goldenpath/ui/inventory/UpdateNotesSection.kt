package dev.foss.goldenpath.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.InventoryCopy
import dev.foss.goldenpath.inventory.UpdateNotesMemory
import dev.foss.goldenpath.ui.theme.SpacingSm

@Composable
fun UpdateNotesSection(packageName: String, modifier: Modifier = Modifier) {
    val revision by UpdateNotesMemory.revision.collectAsStateWithLifecycle()
    val notes = remember(packageName, revision) { UpdateNotesMemory.get(packageName) } ?: return
    var expanded by remember(packageName) { mutableStateOf(false) }
    val action = stringResource(if (expanded) R.string.update_notes_collapse else R.string.update_notes_expand)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
        Text(text = stringResource(R.string.update_notes_title), style = MaterialTheme.typography.titleMedium)
        Text(
            text = if (expanded) notes.text else notes.text.lineSequence().first().trim(),
            style = MaterialTheme.typography.bodyMedium,
        )
        TextButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.semantics { contentDescription = action },
        ) {
            Text(action)
        }
        if (expanded) {
            Text(
                text = stringResource(R.string.update_notes_from, stringResource(InventoryCopy.sourceRes(notes.source))),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
