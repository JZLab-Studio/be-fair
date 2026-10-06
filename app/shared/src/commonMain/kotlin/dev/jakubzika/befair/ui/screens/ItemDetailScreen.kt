package dev.jakubzika.befair.ui.screens

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.screen_item_detail_error
import dev.jakubzika.befair.domain.AppResult
import dev.jakubzika.befair.domain.model.ItemEventResponse
import dev.jakubzika.befair.domain.model.ItemEventType
import dev.jakubzika.befair.ui.LocalAppContainer
import dev.jakubzika.befair.ui.LocalAppSettings
import dev.jakubzika.befair.ui.templates.ItemDetailHistoryLimit
import dev.jakubzika.befair.ui.templates.ItemDetailTemplate
import dev.jakubzika.befair.util.periodsOwned
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

@Composable
fun ItemDetailScreen(
    id: String,
    onBack: () -> Unit
) {
    val container = LocalAppContainer.current
    val repository = container.itemRepository
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var history by remember { mutableStateOf<List<ItemEventResponse>>(emptyList()) }
    var confirmingRemove by remember { mutableStateOf(false) }

    val items by repository.items.collectAsState()
    // Render nothing while the item is missing (e.g. just removed, before navigating back).
    val item = items.firstOrNull { it.id == id } ?: return

    suspend fun showError(result: AppResult.Error) {
        snackbarHostState.showSnackbar(getString(Res.string.screen_item_detail_error, result.message))
    }

    suspend fun reloadHistory() {
        when (val result = repository.events(id, ItemDetailHistoryLimit)) {
            is AppResult.Success -> history = result.data
            is AppResult.Error -> showError(result)
        }
    }

    LaunchedEffect(id) { reloadHistory() }

    ItemDetailTemplate(
        item = item,
        history = history,
        periodsOwned = periodsOwned(item.purchasedOn, LocalAppSettings.current.toolBasis),
        confirmingRemove = confirmingRemove,
        onBack = onBack,
        onLog = { type ->
            coroutineScope.launch {
                when (val result = repository.quickLog(id, type)) {
                    is AppResult.Success -> reloadHistory()
                    is AppResult.Error -> showError(result)
                }
            }
        },
        onRemoveEvent = { event ->
            coroutineScope.launch {
                when (val result = repository.deleteEvent(id, event.id)) {
                    is AppResult.Success -> reloadHistory()
                    is AppResult.Error -> showError(result)
                }
            }
        },
        onRemoveItemClick = { confirmingRemove = true },
        onRemoveItemConfirm = {
            coroutineScope.launch {
                when (val result = repository.deleteItem(id)) {
                    is AppResult.Success -> onBack()
                    is AppResult.Error -> showError(result)
                }
            }
        },
        onRemoveItemCancel = { confirmingRemove = false },
        snackbarHostState = snackbarHostState
    )
}
