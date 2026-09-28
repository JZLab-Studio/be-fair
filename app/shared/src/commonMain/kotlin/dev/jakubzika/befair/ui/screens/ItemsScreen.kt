package dev.jakubzika.befair.ui.screens

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.screen_items_load_error
import be_fair.app.shared.generated.resources.screen_items_quick_log_error
import dev.jakubzika.befair.domain.AppResult
import dev.jakubzika.befair.domain.model.ItemEventType
import dev.jakubzika.befair.domain.model.ItemKind
import dev.jakubzika.befair.domain.model.ItemResponse
import dev.jakubzika.befair.ui.LocalAppContainer
import dev.jakubzika.befair.ui.templates.ItemsTab
import dev.jakubzika.befair.ui.templates.ItemsTemplate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

/** Matches ItemRow's `.w-row--flash` spec: the highlight holds for 700ms after a +1. */
private const val FlashDurationMillis = 700L

@Composable
fun ItemsScreen(
    onNavToItemDetailScreen: (id: String) -> Unit,
    onNavToAddNewItemScreen: () -> Unit
) {
    val container = LocalAppContainer.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(ItemsTab.CLOTHES) }
    val snackbarHostState = remember { SnackbarHostState() }
    var flashingItemId by remember { mutableStateOf<String?>(null) }

    val items by container.itemRepository.items.collectAsState()

    LaunchedEffect(Unit) {
        // The cached list stays visible on failure; just tell the user the download failed.
        (container.itemRepository.refresh() as? AppResult.Error)?.let {
            snackbarHostState.showSnackbar(getString(Res.string.screen_items_load_error, it.message))
        }
    }

    val kind = when (selectedTab) {
        ItemsTab.CLOTHES -> ItemKind.CLOTHING
        ItemsTab.TOOLS -> ItemKind.TOOL
    }

    fun onQuickLog(item: ItemResponse) {
        val eventType = when (item.kind) {
            ItemKind.CLOTHING -> ItemEventType.WEAR
            ItemKind.TOOL -> ItemEventType.USE
        }
        coroutineScope.launch {
            when (val result = container.itemRepository.quickLog(item.id, eventType)) {
                is AppResult.Success -> {
                    flashingItemId = item.id
                    delay(FlashDurationMillis)
                    if (flashingItemId == item.id) flashingItemId = null
                }
                is AppResult.Error ->
                    snackbarHostState.showSnackbar(getString(Res.string.screen_items_quick_log_error, result.message))
            }
        }
    }

    ItemsTemplate(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        onAddItem = onNavToAddNewItemScreen,
        items = items.filter { it.kind == kind },
        onItemClick = onNavToItemDetailScreen,
        onQuickLog = ::onQuickLog,
        flashingItemId = flashingItemId,
        snackbarHostState = snackbarHostState
    )
}
