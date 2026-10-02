package dev.jakubzika.befair.ui.templates

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.screen_item_detail_back
import be_fair.app.shared.generated.resources.screen_item_detail_event_remove
import be_fair.app.shared.generated.resources.screen_item_detail_event_repair
import be_fair.app.shared.generated.resources.screen_item_detail_event_use
import be_fair.app.shared.generated.resources.screen_item_detail_event_wash
import be_fair.app.shared.generated.resources.screen_item_detail_event_wear
import be_fair.app.shared.generated.resources.screen_item_detail_fact_category
import be_fair.app.shared.generated.resources.screen_item_detail_fact_paid
import be_fair.app.shared.generated.resources.screen_item_detail_fact_purchased
import be_fair.app.shared.generated.resources.screen_item_detail_history_empty
import be_fair.app.shared.generated.resources.screen_item_detail_history_title
import be_fair.app.shared.generated.resources.screen_item_detail_log_use
import be_fair.app.shared.generated.resources.screen_item_detail_log_wash
import be_fair.app.shared.generated.resources.screen_item_detail_log_wear
import be_fair.app.shared.generated.resources.screen_item_detail_remove_confirm
import be_fair.app.shared.generated.resources.screen_item_detail_remove_confirm_prompt
import be_fair.app.shared.generated.resources.screen_item_detail_remove_item
import be_fair.app.shared.generated.resources.screen_item_detail_remove_keep
import be_fair.app.shared.generated.resources.screen_item_detail_stat_months_owned
import be_fair.app.shared.generated.resources.screen_item_detail_stat_paid
import be_fair.app.shared.generated.resources.screen_item_detail_stat_per_month
import be_fair.app.shared.generated.resources.screen_item_detail_stat_per_wash
import be_fair.app.shared.generated.resources.screen_item_detail_stat_per_wear
import be_fair.app.shared.generated.resources.screen_item_detail_stat_uses
import be_fair.app.shared.generated.resources.screen_item_detail_stat_washes
import be_fair.app.shared.generated.resources.screen_item_detail_stat_wears
import be_fair.app.shared.generated.resources.item_row_stat_placeholder
import dev.jakubzika.befair.domain.model.ItemEventResponse
import dev.jakubzika.befair.domain.model.ItemEventType
import dev.jakubzika.befair.domain.model.ItemKind
import dev.jakubzika.befair.domain.model.ItemResponse
import dev.jakubzika.befair.domain.model.ItemStats
import dev.jakubzika.befair.ui.atoms.BeFairDimension
import dev.jakubzika.befair.ui.atoms.BeFairTheme
import dev.jakubzika.befair.ui.atoms.DestructiveButton
import dev.jakubzika.befair.ui.atoms.LocalBeFairExtendedColors
import dev.jakubzika.befair.ui.atoms.PrimaryButton
import dev.jakubzika.befair.ui.atoms.SecondaryButton
import dev.jakubzika.befair.util.formatDate
import dev.jakubzika.befair.util.formatDateFromMillis
import dev.jakubzika.befair.util.formatEuroCents
import dev.jakubzika.befair.util.formatEuroWhole
import org.jetbrains.compose.resources.stringResource

private val HeaderHeight = 56.dp
private val HitTarget = 44.dp
private val CardShape = RoundedCornerShape(BeFairDimension.Radius.small)

/** The history list shows only the newest entries; the screen asks the server for this many. */
const val ItemDetailHistoryLimit = 8

// Item detail: fixed header with back chevron, then a scrolling column — stats card
// (hero cost + three type-specific stats), facts block, log actions, history, and the
// two-step remove flow. Green is the hero stat and primary buttons only; orange is the
// remove path only.
@Composable
fun ItemDetailTemplate(
    item: ItemResponse,
    history: List<ItemEventResponse>,
    monthsOwned: Int,
    confirmingRemove: Boolean,
    onBack: () -> Unit,
    onLog: (ItemEventType) -> Unit,
    onRemoveEvent: (ItemEventResponse) -> Unit,
    onRemoveItemClick: () -> Unit,
    onRemoveItemConfirm: () -> Unit,
    onRemoveItemCancel: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val isClothing = item.kind == ItemKind.CLOTHING

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Header(title = item.name, onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(BeFairDimension.Spacing.md),
                verticalArrangement = Arrangement.spacedBy(BeFairDimension.Spacing.md)
            ) {
                StatsCard(item = item, monthsOwned = monthsOwned)
                FactsBlock(item = item)

                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(
                        if (isClothing) Res.string.screen_item_detail_log_wear else Res.string.screen_item_detail_log_use
                    ),
                    onClick = { onLog(if (isClothing) ItemEventType.WEAR else ItemEventType.USE) }
                )
                if (isClothing) {
                    SecondaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(Res.string.screen_item_detail_log_wash),
                        onClick = { onLog(ItemEventType.WASH) }
                    )
                }

                History(history = history, onRemoveEvent = onRemoveEvent)

                RemoveItem(
                    confirming = confirmingRemove,
                    onClick = onRemoveItemClick,
                    onConfirm = onRemoveItemConfirm,
                    onCancel = onRemoveItemCancel
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun Header(title: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HeaderHeight)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = BeFairDimension.Spacing.sm)
                .size(HitTarget)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.screen_item_detail_back),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = HeaderHeight)
        )
    }
}

@Composable
private fun StatsCard(item: ItemResponse, monthsOwned: Int) {
    val stats = item.stats
    val placeholder = stringResource(Res.string.item_row_stat_placeholder)
    val isClothing = item.kind == ItemKind.CLOTHING
    val heroCents = if (isClothing) stats?.costPerUseCents else stats?.costPerMonthCents
    val heroLabel = stringResource(
        if (isClothing) Res.string.screen_item_detail_stat_per_wear else Res.string.screen_item_detail_stat_per_month
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, CardShape)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), CardShape)
            .padding(BeFairDimension.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(BeFairDimension.Spacing.md)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(BeFairDimension.Spacing.xs)) {
            Text(
                text = heroCents?.let(::formatEuroCents) ?: placeholder,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = heroLabel,
                style = MaterialTheme.typography.bodySmall,
                color = LocalBeFairExtendedColors.current.ink3
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(BeFairDimension.Spacing.sm)) {
            if (isClothing) {
                val totalCents = item.priceCents + (stats?.maintenanceCents ?: 0)
                val washes = stats?.washCount ?: 0
                Stat(
                    value = if (washes > 0) formatEuroCents(totalCents / washes) else placeholder,
                    label = stringResource(Res.string.screen_item_detail_stat_per_wash),
                    modifier = Modifier.weight(1f)
                )
                Stat(
                    value = (stats?.wearCount ?: 0).toString(),
                    label = stringResource(Res.string.screen_item_detail_stat_wears),
                    modifier = Modifier.weight(1f)
                )
                Stat(
                    value = washes.toString(),
                    label = stringResource(Res.string.screen_item_detail_stat_washes),
                    modifier = Modifier.weight(1f)
                )
            } else {
                Stat(
                    value = monthsOwned.toString(),
                    label = stringResource(Res.string.screen_item_detail_stat_months_owned),
                    modifier = Modifier.weight(1f)
                )
                Stat(
                    value = (stats?.useCount ?: 0).toString(),
                    label = stringResource(Res.string.screen_item_detail_stat_uses),
                    modifier = Modifier.weight(1f)
                )
                Stat(
                    value = formatEuroWhole(item.priceCents),
                    label = stringResource(Res.string.screen_item_detail_stat_paid),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = LocalBeFairExtendedColors.current.ink3
        )
    }
}

@Composable
private fun FactsBlock(item: ItemResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, CardShape)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), CardShape)
    ) {
        Fact(stringResource(Res.string.screen_item_detail_fact_paid), formatEuroCents(item.priceCents))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Fact(stringResource(Res.string.screen_item_detail_fact_purchased), formatDate(item.purchasedOn))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Fact(stringResource(Res.string.screen_item_detail_fact_category), item.category)
    }
}

@Composable
private fun Fact(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HitTarget)
            .padding(horizontal = BeFairDimension.Spacing.md, vertical = BeFairDimension.Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = BeFairDimension.Spacing.md)
        )
    }
}

@Composable
private fun History(history: List<ItemEventResponse>, onRemoveEvent: (ItemEventResponse) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(BeFairDimension.Spacing.sm)) {
        Text(
            text = stringResource(Res.string.screen_item_detail_history_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (history.isEmpty()) {
            Text(
                text = stringResource(Res.string.screen_item_detail_history_empty),
                style = MaterialTheme.typography.bodySmall,
                color = LocalBeFairExtendedColors.current.ink3
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, CardShape)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), CardShape)
            ) {
                history.take(ItemDetailHistoryLimit).forEachIndexed { index, event ->
                    if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    HistoryRow(event = event, onRemove = { onRemoveEvent(event) })
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(event: ItemEventResponse, onRemove: () -> Unit) {
    val verb = stringResource(
        when (event.type) {
            ItemEventType.WEAR -> Res.string.screen_item_detail_event_wear
            ItemEventType.WASH -> Res.string.screen_item_detail_event_wash
            ItemEventType.USE -> Res.string.screen_item_detail_event_use
            ItemEventType.REPAIR -> Res.string.screen_item_detail_event_repair
        }
    )
    val date = formatDateFromMillis(event.occurredAt)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HitTarget)
            .padding(start = BeFairDimension.Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BeFairDimension.Spacing.sm)
    ) {
        // Status tick: a glyph rather than a color, so the row stays legible with color stripped.
        Text(
            text = "✓",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = verb,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = date,
            style = MaterialTheme.typography.bodySmall,
            color = LocalBeFairExtendedColors.current.ink3,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRemove, modifier = Modifier.size(HitTarget)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(Res.string.screen_item_detail_event_remove, verb, date),
                tint = LocalBeFairExtendedColors.current.ink3
            )
        }
    }
}

@Composable
private fun RemoveItem(
    confirming: Boolean,
    onClick: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    if (!confirming) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.heightIn(min = HitTarget)
        ) {
            Text(
                text = stringResource(Res.string.screen_item_detail_remove_item),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error,
                textDecoration = TextDecoration.Underline
            )
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(BeFairDimension.Spacing.sm)) {
        Text(
            text = stringResource(Res.string.screen_item_detail_remove_confirm_prompt),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(horizontalArrangement = Arrangement.spacedBy(BeFairDimension.Spacing.sm)) {
            DestructiveButton(
                modifier = Modifier.weight(1f),
                title = stringResource(Res.string.screen_item_detail_remove_confirm),
                onClick = onConfirm
            )
            SecondaryButton(
                modifier = Modifier.weight(1f),
                title = stringResource(Res.string.screen_item_detail_remove_keep),
                onClick = onCancel
            )
        }
    }
}

@Preview(backgroundColor = 0xF5F5F2, showBackground = true)
@Composable
private fun ItemDetailTemplateClothingPreview() {
    BeFairTheme {
        ItemDetailTemplate(
            item = previewClothing,
            history = listOf(
                ItemEventResponse("e1", previewClothing.id, ItemEventType.WEAR, 1_700_000_000_000, null, null, 0),
                ItemEventResponse("e2", previewClothing.id, ItemEventType.WASH, 1_699_000_000_000, null, null, 0)
            ),
            monthsOwned = 0,
            confirmingRemove = false,
            onBack = {},
            onLog = {},
            onRemoveEvent = {},
            onRemoveItemClick = {},
            onRemoveItemConfirm = {},
            onRemoveItemCancel = {}
        )
    }
}

@Preview(backgroundColor = 0xF5F5F2, showBackground = true)
@Composable
private fun ItemDetailTemplateToolConfirmPreview() {
    BeFairTheme {
        ItemDetailTemplate(
            item = previewClothing.copy(
                kind = ItemKind.TOOL,
                name = "Cordless drill",
                category = "Power tool",
                stats = previewClothing.stats?.copy(useCount = 12, costPerMonthCents = 850)
            ),
            history = emptyList(),
            monthsOwned = 14,
            confirmingRemove = true,
            onBack = {},
            onLog = {},
            onRemoveEvent = {},
            onRemoveItemClick = {},
            onRemoveItemConfirm = {},
            onRemoveItemCancel = {}
        )
    }
}

private val previewClothing = ItemResponse(
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
