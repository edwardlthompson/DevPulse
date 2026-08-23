package dev.foss.goldenpath.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.UpdateAllResume
import dev.foss.goldenpath.inventory.WelcomeHome
import dev.foss.goldenpath.ui.refresh.highRefreshScroll
import dev.foss.goldenpath.ui.theme.SpacingMd

@Composable
fun InventoryScreen(
    model: InventoryUiModel,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    when (model.welcomeHome) {
        WelcomeHome.Welcome -> {
            WelcomeScreen(
                onContinue = model.onAcknowledge,
                onSkip = model.onSkip,
                modifier = modifier,
            )
            return
        }
        WelcomeHome.Splash -> {
            SplashImage(modifier = modifier)
            return
        }
        WelcomeHome.Inventory -> Unit
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
        val context = LocalContext.current
        val selected = remember { mutableStateOf(UpdateAllResume.load(context.filesDir).toSet()) }
        InstallPermissionBanner()
        UpdateAllButton(apps = model.apps, selected = selected.value)
        if (model.apps.isEmpty()) {
            Text(text = stringResource(R.string.inventory_empty))
        } else {
            LazyColumn(modifier = Modifier.weight(1f).highRefreshScroll(), state = listState) {
                items(model.apps, key = { it.packageName }) { app ->
                    InventoryRow(
                        app = app,
                        selected = app.packageName in selected.value,
                        onToggleSelect = {
                            selected.value = if (app.packageName in selected.value) {
                                selected.value - app.packageName
                            } else {
                                selected.value + app.packageName
                            }
                        },
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
