package dev.jakubzika.befair.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.screen_items_add_item
import be_fair.app.shared.generated.resources.screen_items_empty_hint_clothes
import be_fair.app.shared.generated.resources.screen_items_empty_hint_tools
import be_fair.app.shared.generated.resources.screen_items_load_error
import be_fair.app.shared.generated.resources.screen_items_tab_clothes
import be_fair.app.shared.generated.resources.screen_items_tab_tools
import be_fair.app.shared.generated.resources.screen_items_title
import dev.jakubzika.befair.domain.model.ItemKind
import dev.jakubzika.befair.domain.model.ItemResponse
import dev.jakubzika.befair.domain.model.ItemStats
import dev.jakubzika.befair.ui.atoms.BeFairDimension
import dev.jakubzika.befair.ui.atoms.BeFairTheme
import dev.jakubzika.befair.ui.atoms.LocalBeFairExtendedColors
import dev.jakubzika.befair.ui.molecules.SegmentedControl
import dev.jakubzika.befair.ui.organisms.ItemRow
import org.jetbrains.compose.resources.stringResource

private val HeaderHeight = 56.dp

enum class ItemsTab {
    CLOTHES,
    TOOLS
}

// Items screen: fixed header with the "+" action, the Clothes/Tools segmented
// control, a hairline divider, then the item list for the selected tab. With no
// items the hint explaining +1 logging takes the list's place.
@Composable
fun ItemsTemplate(
    selectedTab: ItemsTab,
    onTabSelected: (ItemsTab) -> Unit,
    onAddItem: () -> Unit,
    items: List<ItemResponse>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onQuickLog: (ItemResponse) -> Unit = {},
    flashingItemId: String? = null,
    errorMessage: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(HeaderHeight)
        ) {
            Text(
                text = stringResource(Res.string.screen_items_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Center)
            )

            IconButton(
                onClick = onAddItem,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = BeFairDimension.Spacing.sm)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.screen_items_add_item),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        SegmentedControl(
            options = listOf(
                stringResource(Res.string.screen_items_tab_clothes),
                stringResource(Res.string.screen_items_tab_tools)
            ),
            selectedIndex = selectedTab.ordinal,
            onOptionSelected = { index -> onTabSelected(ItemsTab.entries[index]) },
            modifier = Modifier.padding(horizontal = BeFairDimension.Spacing.md)
        )

        HorizontalDivider(
            modifier = Modifier.padding(top = BeFairDimension.Spacing.md),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        if (errorMessage != null) {
            Text(
                text = stringResource(Res.string.screen_items_load_error, errorMessage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(BeFairDimension.Spacing.md)
            )
        }

        if (items.isEmpty()) {
            Text(
                text = when (selectedTab) {
                    ItemsTab.CLOTHES -> stringResource(Res.string.screen_items_empty_hint_clothes)
                    ItemsTab.TOOLS -> stringResource(Res.string.screen_items_empty_hint_tools)
                },
                style = MaterialTheme.typography.bodySmall,
                color = LocalBeFairExtendedColors.current.ink3,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(BeFairDimension.Spacing.md)
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items, key = { it.id }) { item ->
                    ItemRow(
                        item = item,
                        stats = item.stats,
                        flashing = item.id == flashingItemId,
                        onOpen = { onItemClick(item.id) },
                        onQuickLog = { onQuickLog(item) }
                    )
                }
            }
        }
    }
}

@Preview(backgroundColor = 0xF5F5F2, showBackground = true)
@Composable
private fun ItemsTemplateClothesPreview() {
    BeFairTheme {
        ItemsTemplate(
            selectedTab = ItemsTab.CLOTHES,
            onTabSelected = {},
            onAddItem = {},
            items = listOf(previewItem),
            onItemClick = {}
        )
    }
}

@Preview(backgroundColor = 0xF5F5F2, showBackground = true)
@Composable
private fun ItemsTemplateToolsPreview() {
    BeFairTheme {
        ItemsTemplate(
            selectedTab = ItemsTab.TOOLS,
            onTabSelected = {},
            onAddItem = {},
            items = emptyList(),
            onItemClick = {}
        )
    }
}

private val previewItem = ItemResponse(
    id = "5e6b4b7c-1f2a-4a3d-9c8e-0d1f2a3b4c5d",
    kind = ItemKind.CLOTHING,
    name = "Wool overcoat",
    category = "Outerwear",
    priceCents = 28000,
    currency = "EUR",
    purchasedOn = 19000,
    archivedAt = null,
    deletedAt = null,
    createdAt = 0,
    updatedAt = 0,
    stats = ItemStats(
        wearCount = 14,
        washCount = 2,
        useCount = 0,
        maintenanceCents = 0,
        lastEventAt = null,
        costPerUseCents = 2000,
        costPerMonthCents = null
    )
)
