package dev.jakubzika.befair.ui.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.item_row_content_description
import be_fair.app.shared.generated.resources.item_row_stat_per_month
import be_fair.app.shared.generated.resources.item_row_stat_per_wear
import be_fair.app.shared.generated.resources.item_row_stat_placeholder
import be_fair.app.shared.generated.resources.screen_item_detail_event_repair
import be_fair.app.shared.generated.resources.screen_item_detail_event_use
import be_fair.app.shared.generated.resources.screen_item_detail_event_wash
import be_fair.app.shared.generated.resources.screen_item_detail_event_wear
import be_fair.app.shared.generated.resources.screen_overview_activity_meta
import dev.jakubzika.befair.domain.model.ActivityEntry
import dev.jakubzika.befair.domain.model.ItemEventResponse
import dev.jakubzika.befair.domain.model.ItemEventType
import dev.jakubzika.befair.domain.model.ItemKind
import dev.jakubzika.befair.domain.model.ItemResponse
import dev.jakubzika.befair.domain.model.ItemStats
import dev.jakubzika.befair.ui.atoms.BeFairDimension
import dev.jakubzika.befair.ui.atoms.BeFairTheme
import dev.jakubzika.befair.ui.atoms.LocalBeFairExtendedColors
import dev.jakubzika.befair.util.formatDateFromMillis
import dev.jakubzika.befair.util.formatEuroCents
import org.jetbrains.compose.resources.stringResource

private val RowMinHeight = 56.dp
private val TickSize = 10.dp

/**
 * One row of Overview "Recent activity": status tick + item name + "Worn · 2 Oct 2026" meta and the
 * item's current cost figure, trailing. Tapping opens the item. Neutral ink only.
 */
@Composable
fun ActivityRow(
    entry: ActivityEntry,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val item = entry.item
    val verb = when (entry.event.type) {
        ItemEventType.WEAR -> stringResource(Res.string.screen_item_detail_event_wear)
        ItemEventType.WASH -> stringResource(Res.string.screen_item_detail_event_wash)
        ItemEventType.USE -> stringResource(Res.string.screen_item_detail_event_use)
        ItemEventType.REPAIR -> stringResource(Res.string.screen_item_detail_event_repair)
    }
    val meta = stringResource(
        Res.string.screen_overview_activity_meta,
        verb,
        formatDateFromMillis(entry.event.occurredAt)
    )
    val statCents = when (item.kind) {
        ItemKind.CLOTHING -> item.stats?.costPerUseCents
        ItemKind.TOOL -> item.stats?.costPerMonthCents
    }
    val value = statCents?.let(::formatEuroCents) ?: stringResource(Res.string.item_row_stat_placeholder)
    val unit = when (item.kind) {
        ItemKind.CLOTHING -> stringResource(Res.string.item_row_stat_per_wear)
        ItemKind.TOOL -> stringResource(Res.string.item_row_stat_per_month)
    }
    val description = stringResource(Res.string.item_row_content_description, item.name, meta, value, unit)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = RowMinHeight)
            .clickable(role = Role.Button, onClick = onOpen)
            .clearAndSetSemantics { contentDescription = description }
            .padding(horizontal = BeFairDimension.Spacing.md, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BeFairDimension.Spacing.sm + BeFairDimension.Spacing.xs)
    ) {
        StatusTick(entry.event.type)

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
                text = meta,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
                color = LocalBeFairExtendedColors.current.ink3
            )
        }
    }
}

// Variants differ by shape/fill rather than color, so they stay legible with color stripped:
// wear = solid dot, wash = outlined ring, use/repair = solid square.
@Composable
private fun StatusTick(type: ItemEventType) {
    val ink = MaterialTheme.colorScheme.onSurface
    val base = Modifier.size(TickSize)
    when (type) {
        ItemEventType.WEAR -> Box(base.clip(CircleShape).background(ink))
        ItemEventType.WASH -> Box(base.border(1.5.dp, ink, CircleShape))
        ItemEventType.USE, ItemEventType.REPAIR ->
            Box(base.clip(RoundedCornerShape(2.dp)).background(ink))
    }
}

@Preview(backgroundColor = 0xFFFFFF, showBackground = true)
@Composable
private fun ActivityRowPreview() {
    BeFairTheme {
        ActivityRow(
            entry = ActivityEntry(
                item = ItemResponse(
                    id = "1", kind = ItemKind.CLOTHING, name = "Wool overcoat", category = "Outerwear",
                    priceCents = 28000, currency = "EUR", purchasedOn = 19000, archivedAt = null,
                    deletedAt = null, createdAt = 0, updatedAt = 0,
                    stats = ItemStats(46, 2, 0, 0, null, 609, null)
                ),
                event = ItemEventResponse("e", "1", ItemEventType.WEAR, 1_760_000_000_000, null, null, 0)
            ),
            onOpen = {}
        )
    }
}
