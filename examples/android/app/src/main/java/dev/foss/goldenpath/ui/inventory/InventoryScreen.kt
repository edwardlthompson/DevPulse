package dev.foss.goldenpath.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import dev.foss.goldenpath.R
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.theme.SpacingMd

@Composable
fun InventoryScreen(
    model: InventoryUiModel,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    if (!model.canScan) {
        InventoryRationale(
            skipped = model.rationaleSkipped,
            onAcknowledge = model.onAcknowledge,
            onSkip = model.onSkip,
            modifier = modifier,
        )
        return
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        if (model.showSearch) {
            OutlinedTextField(
                value = model.query,
                onValueChange = model.onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.inventory_search)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        keyboard?.hide()
                    },
                ),
            )
        }
        if (model.showFilters) {
            InventorySortBar(
                sortMode = model.sortMode,
                staleOnly = model.staleOnly,
                updatesOnly = model.updatesOnly,
                sourceFilters = model.sourceFilters,
                canRankByUsage = model.canRankByUsage,
                onSortMode = model.onSortMode,
                onStaleOnlyChange = model.onStaleOnlyChange,
                onUpdatesOnlyChange = model.onUpdatesOnlyChange,
                onToggleSourceFilter = model.onToggleSourceFilter,
                onOpenUsageAccess = model.onOpenUsageAccess,
            )
        }
        if (model.showUsageWalkthrough) {
            Text(text = stringResource(R.string.inventory_usage_title), style = MaterialTheme.typography.titleSmall)
            Text(text = stringResource(R.string.inventory_usage_body))
            TextButton(onClick = model.onDismissUsage) {
                Text(stringResource(R.string.inventory_usage_seen))
            }
        }
        if (model.apps.isEmpty()) {
            Text(text = stringResource(R.string.inventory_empty))
        } else {
            LazyColumn(modifier = Modifier.weight(1f), state = listState) {
                items(model.apps, key = { it.packageName }) { app ->
                    InventoryRow(
                        app = app,
                        onOpen = {
                            focusManager.clearFocus()
                            keyboard?.hide()
                            model.onSelect(app.packageName)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun InventoryRationale(
    skipped: Boolean,
    onAcknowledge: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(
            text = stringResource(R.string.inventory_rationale_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(
                if (skipped) R.string.inventory_blocked else R.string.inventory_rationale_body,
            ),
        )
        Button(onClick = onAcknowledge) {
            Text(stringResource(R.string.inventory_rationale_ack))
        }
        TextButton(onClick = onSkip, modifier = Modifier.bottomInsetPadding()) {
            Text(stringResource(R.string.inventory_rationale_skip))
        }
    }
}
