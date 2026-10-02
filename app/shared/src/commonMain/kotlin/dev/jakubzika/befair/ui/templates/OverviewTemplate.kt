package dev.jakubzika.befair.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.screen_overview_all_items
import be_fair.app.shared.generated.resources.screen_overview_attention
import be_fair.app.shared.generated.resources.screen_overview_recent_activity
import be_fair.app.shared.generated.resources.screen_overview_title
import dev.jakubzika.befair.domain.model.ActivityEntry
import dev.jakubzika.befair.domain.model.ItemResponse
import dev.jakubzika.befair.domain.model.OverviewSummary
import dev.jakubzika.befair.ui.atoms.BeFairDimension
import dev.jakubzika.befair.ui.atoms.BeFairTheme
import dev.jakubzika.befair.ui.atoms.LinkButton
import dev.jakubzika.befair.ui.organisms.ActivityRow
import dev.jakubzika.befair.ui.organisms.AttentionNotice
import dev.jakubzika.befair.ui.organisms.OverviewStatsCard
import dev.jakubzika.befair.util.formatEuroCents
import org.jetbrains.compose.resources.stringResource

private val HeaderHeight = 56.dp

// Overview with data: fixed header, stats card, optional attention notice, then the
// "Recent activity" list. The notice and the list are removed (not zero-filled) when empty.
@Composable
fun OverviewTemplate(
    summary: OverviewSummary,
    recent: List<ActivityEntry>,
    onOpenItem: (id: String) -> Unit,
    onShowAllItems: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(HeaderHeight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(Res.string.screen_overview_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = BeFairDimension.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(BeFairDimension.Spacing.md)
        ) {
            OverviewStatsCard(summary = summary)

            summary.attention?.let { item ->
                AttentionNotice(
                    text = stringResource(
                        Res.string.screen_overview_attention,
                        item.name,
                        formatEuroCents(item.stats?.costPerUseCents ?: 0L)
                    ),
                    onClick = { onOpenItem(item.id) }
                )
            }

            if (recent.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.screen_overview_recent_activity),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    LinkButton(
                        text = stringResource(Res.string.screen_overview_all_items),
                        onClick = onShowAllItems
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(BeFairDimension.Radius.small))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    recent.forEachIndexed { index, entry ->
                        if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        ActivityRow(entry = entry, onOpen = { onOpenItem(entry.item.id) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(BeFairDimension.Spacing.sm))
        }
    }
}

@Preview(backgroundColor = 0xF5F5F2, showBackground = true)
@Composable
private fun OverviewTemplatePreview() {
    BeFairTheme {
        OverviewTemplate(
            summary = OverviewSummary(
                investedCents = 128000,
                avgCostPerWearCents = 609,
                toolsPerMonthCents = 1850,
                attention = null
            ),
            recent = emptyList(),
            onOpenItem = {},
            onShowAllItems = {}
        )
    }
}
