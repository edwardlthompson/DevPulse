package dev.foss.goldenpath.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.InventoryCopy
import dev.foss.goldenpath.inventory.InventorySortMode
import dev.foss.goldenpath.inventory.InventorySourceFilter
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.ui.theme.SpacingMd

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InventorySortBar(
    sortMode: InventorySortMode,
    staleOnly: Boolean,
    updatesOnly: Boolean,
    sourceFilters: Set<RemoteReleasedSource>,
    canRankByUsage: Boolean,
    onSortMode: (InventorySortMode) -> Unit,
    onStaleOnlyChange: (Boolean) -> Unit,
    onUpdatesOnlyChange: (Boolean) -> Unit,
    onToggleSourceFilter: (RemoteReleasedSource) -> Unit,
    onOpenUsageAccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            SortChip(InventorySortMode.Oldest, sortMode, onSortMode, R.string.inventory_sort_oldest)
            SortChip(InventorySortMode.Newest, sortMode, onSortMode, R.string.inventory_sort_newest)
            SortChip(InventorySortMode.Name, sortMode, onSortMode, R.string.inventory_sort_name)
            SortChip(InventorySortMode.UsedAndStale, sortMode, onSortMode, R.string.inventory_sort_used)
            FilterChip(
                selected = staleOnly,
                onClick = { onStaleOnlyChange(!staleOnly) },
                label = { Text(stringResource(R.string.inventory_filter_stale)) },
            )
            FilterChip(
                selected = updatesOnly,
                onClick = { onUpdatesOnlyChange(!updatesOnly) },
                label = { Text(stringResource(R.string.inventory_filter_updates)) },
            )
            InventorySourceFilter.CHIPS.forEach { source ->
                FilterChip(
                    selected = source in sourceFilters,
                    onClick = { onToggleSourceFilter(source) },
                    label = {
                        Text(
                            stringResource(
                                R.string.inventory_filter_source,
                                stringResource(InventoryCopy.sourceRes(source)),
                            ),
                        )
                    },
                )
            }
        }
        if (sortMode == InventorySortMode.UsedAndStale && !canRankByUsage) {
            Text(text = stringResource(R.string.inventory_usage_grant_body))
            TextButton(onClick = onOpenUsageAccess) {
                Text(stringResource(R.string.inventory_usage_grant))
            }
        }
    }
}

@Composable
private fun SortChip(
    mode: InventorySortMode,
    selected: InventorySortMode,
    onSortMode: (InventorySortMode) -> Unit,
    labelRes: Int,
) {
    FilterChip(
        selected = selected == mode,
        onClick = { onSortMode(mode) },
        label = { Text(stringResource(labelRes)) },
    )
}
