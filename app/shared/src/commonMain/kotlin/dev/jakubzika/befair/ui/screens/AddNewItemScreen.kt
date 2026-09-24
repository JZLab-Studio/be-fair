package dev.jakubzika.befair.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.jakubzika.befair.domain.model.ClothingStats
import dev.jakubzika.befair.domain.model.Item
import dev.jakubzika.befair.domain.model.ItemKind
import dev.jakubzika.befair.domain.model.ToolStats
import dev.jakubzika.befair.ui.LocalAppContainer
import dev.jakubzika.befair.ui.templates.AddNewItemTemplate
import dev.jakubzika.befair.util.currentTimeMillis
import dev.jakubzika.befair.util.todayIso
import kotlinx.coroutines.launch

@Composable
fun AddNewItemScreen(
    onCreateItem: () -> Unit,
    onBack: () -> Unit
) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val todayDate = remember { todayIso() }

    AddNewItemTemplate(
        todayDate = todayDate,
        onBack = onBack,
        onSubmit = { kind, name, category, price, date ->
            val item = Item(
                id = "i" + currentTimeMillis(),
                kind = kind,
                name = name,
                category = category,
                price = price,
                purchased = date,
                stats = when (kind) {
                    ItemKind.CLOTHING -> ClothingStats()
                    ItemKind.TOOL -> ToolStats()
                }
            )
            scope.launch {
                container.itemRepository.addItem(item)
                onCreateItem()
            }
        }
    )
}
