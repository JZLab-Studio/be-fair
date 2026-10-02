package dev.jakubzika.befair.ui.organisms

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.item_row_content_description
import be_fair.app.shared.generated.resources.item_row_meta_clothing
import be_fair.app.shared.generated.resources.item_row_meta_tool
import be_fair.app.shared.generated.resources.item_row_quick_log_use
import be_fair.app.shared.generated.resources.item_row_quick_log_wear
import be_fair.app.shared.generated.resources.item_row_stat_per_month
import be_fair.app.shared.generated.resources.item_row_stat_per_wear
import be_fair.app.shared.generated.resources.item_row_stat_placeholder
import dev.jakubzika.befair.domain.model.ItemKind
import dev.jakubzika.befair.domain.model.ItemResponse
import dev.jakubzika.befair.domain.model.ItemStats
import dev.jakubzika.befair.ui.atoms.BeFairDimension
import dev.jakubzika.befair.ui.atoms.BeFairTheme
import dev.jakubzika.befair.ui.atoms.LocalBeFairExtendedColors
import dev.jakubzika.befair.ui.atoms.QuickAddButton
import dev.jakubzika.befair.util.formatEuroCents
import dev.jakubzika.befair.util.formatMonthYear
import org.jetbrains.compose.resources.stringResource

private val RowMinHeight = 56.dp
private val RowHorizontalPaddingComfortable = BeFairDimension.Spacing.md
private val RowVerticalPaddingComfortable = 14.dp
private val RowHorizontalPaddingCompact = BeFairDimension.Spacing.sm + BeFairDimension.Spacing.xs
private val RowVerticalPaddingCompact = 9.dp
private val FlashDuration = 700

/**
 * A single row of the Items list — "Wool overcoat · 46 wears · 2 washes · €6.09 per wear · [+1]".
 * Tapping the row opens item detail; tapping +1 logs a wear/use without opening detail.
 * Per DESIGN.md "list-item" / "quick-add" tokens.
 */
@Composable
fun ItemRow(
    item: ItemResponse,
    stats: ItemStats?,
    flashing: Boolean,
    onOpen: () -> Unit,
    onQuickLog: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    reducedAccent: Boolean = false
) {
    val ink3 = LocalBeFairExtendedColors.current.ink3
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val flashColor = lerp(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.primary, 0.08f)
    val rowBackground by animateColorAsState(
        targetValue = when {
            flashing -> flashColor
            isPressed -> MaterialTheme.colorScheme.surfaceContainerLow
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 120),
        label = "itemRowBackground"
    )

    val metaText = itemMetaText(item, stats)
    val statValue = stats?.primaryValueCents(item.kind)?.let(::formatEuroCents)
        ?: stringResource(Res.string.item_row_stat_placeholder)
    val statLabel = when (item.kind) {
        ItemKind.CLOTHING -> stringResource(Res.string.item_row_stat_per_wear)
        ItemKind.TOOL -> stringResource(Res.string.item_row_stat_per_month)
    }
    val quickLogDescription = when (item.kind) {
        ItemKind.CLOTHING -> stringResource(Res.string.item_row_quick_log_wear)
        ItemKind.TOOL -> stringResource(Res.string.item_row_quick_log_use)
    }
    val rowDescription = stringResource(
        Res.string.item_row_content_description,
        item.name,
        metaText,
        statValue,
        statLabel
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = RowMinHeight)
            .background(rowBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onOpen
            )
            .clearAndSetSemantics { contentDescription = rowDescription }
            .padding(
                horizontal = if (compact) RowHorizontalPaddingCompact else RowHorizontalPaddingComfortable,
                vertical = if (compact) RowVerticalPaddingCompact else RowVerticalPaddingComfortable
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BeFairDimension.Spacing.sm + BeFairDimension.Spacing.xs)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = metaText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = statValue,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = statLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
                color = ink3
            )
        }

        QuickAddButton(
            contentDescription = quickLogDescription,
            reducedAccent = reducedAccent,
            onClick = onQuickLog
        )
    }
}

@Composable
private fun itemMetaText(item: ItemResponse, stats: ItemStats?): String = when (item.kind) {
    ItemKind.CLOTHING -> stringResource(
        Res.string.item_row_meta_clothing,
        stats?.wearCount ?: 0,
        stats?.washCount ?: 0
    )
    ItemKind.TOOL -> stringResource(
        Res.string.item_row_meta_tool,
        stats?.useCount ?: 0,
        formatMonthYear(item.purchasedOn)
    )
}

private fun ItemStats.primaryValueCents(kind: ItemKind): Long? = when (kind) {
    ItemKind.CLOTHING -> costPerUseCents
    ItemKind.TOOL -> costPerMonthCents
}

@Preview(backgroundColor = 0xFFFFFF, showBackground = true)
@Composable
private fun ItemRowClothingPreview() {
    BeFairTheme {
        ItemRow(
            item = ItemResponse(
                id = "1",
                kind = ItemKind.CLOTHING,
                name = "Wool overcoat",
                category = "Outerwear",
                priceCents = 28000,
                currency = "EUR",
                purchasedOn = 19000,
                archivedAt = null,
                deletedAt = null,
                createdAt = 0,
                updatedAt = 0
            ),
            stats = ItemStats(
                wearCount = 46,
                washCount = 2,
                useCount = 0,
                maintenanceCents = 0,
                lastEventAt = null,
                costPerUseCents = 609,
                costPerMonthCents = null
            ),
            flashing = false,
            onOpen = {},
            onQuickLog = {}
        )
    }
}

@Preview(backgroundColor = 0xFFFFFF, showBackground = true)
@Composable
private fun ItemRowToolPreview() {
    BeFairTheme {
        ItemRow(
            item = ItemResponse(
                id = "2",
                kind = ItemKind.TOOL,
                name = "Cordless drill",
                category = "Tools",
                priceCents = 12000,
                currency = "EUR",
                purchasedOn = 18000,
                archivedAt = null,
                deletedAt = null,
                createdAt = 0,
                updatedAt = 0
            ),
            stats = ItemStats(
                wearCount = 0,
                washCount = 0,
                useCount = 12,
                maintenanceCents = 0,
                lastEventAt = null,
                costPerUseCents = null,
                costPerMonthCents = 850
            ),
            flashing = false,
            onOpen = {},
            onQuickLog = {}
        )
    }
}

@Preview(backgroundColor = 0xFFFFFF, showBackground = true)
@Composable
private fun ItemRowFlashingPreview() {
    BeFairTheme {
        ItemRow(
            item = ItemResponse(
                id = "1",
                kind = ItemKind.CLOTHING,
                name = "Wool overcoat",
                category = "Outerwear",
                priceCents = 28000,
                currency = "EUR",
                purchasedOn = 19000,
                archivedAt = null,
                deletedAt = null,
                createdAt = 0,
                updatedAt = 0
            ),
            stats = ItemStats(
                wearCount = 47,
                washCount = 2,
                useCount = 0,
                maintenanceCents = 0,
                lastEventAt = null,
                costPerUseCents = 596,
                costPerMonthCents = null
            ),
            flashing = true,
            onOpen = {},
            onQuickLog = {}
        )
    }
}
