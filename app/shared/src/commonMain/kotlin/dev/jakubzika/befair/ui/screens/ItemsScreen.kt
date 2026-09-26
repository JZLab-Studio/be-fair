package dev.jakubzika.befair.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import dev.jakubzika.befair.domain.AppResult
import dev.jakubzika.befair.domain.model.ItemKind
import dev.jakubzika.befair.ui.LocalAppContainer
import dev.jakubzika.befair.ui.templates.ItemsTab
import dev.jakubzika.befair.ui.templates.ItemsTemplate

@Composable
fun ItemsScreen(
    onNavToItemDetailScreen: (id: String) -> Unit,
    onNavToAddNewItemScreen: () -> Unit
) {
    val container = LocalAppContainer.current
    var selectedTab by remember { mutableStateOf(ItemsTab.CLOTHES) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val items by container.itemRepository.items.collectAsState()

    LaunchedEffect(Unit) {
        errorMessage = (container.itemRepository.refresh() as? AppResult.Error)?.message
    }

    val kind = when (selectedTab) {
        ItemsTab.CLOTHES -> ItemKind.CLOTHING
        ItemsTab.TOOLS -> ItemKind.TOOL
    }

    ItemsTemplate(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        onAddItem = onNavToAddNewItemScreen,
        items = items.filter { it.kind == kind },
        onItemClick = onNavToItemDetailScreen,
        errorMessage = errorMessage
    )
}
