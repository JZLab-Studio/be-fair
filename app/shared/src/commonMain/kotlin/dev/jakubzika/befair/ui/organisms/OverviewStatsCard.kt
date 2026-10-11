package dev.jakubzika.befair.ui.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.item_row_stat_placeholder
import be_fair.app.shared.generated.resources.screen_overview_avg_per_wear_label
import be_fair.app.shared.generated.resources.screen_overview_invested_label
import be_fair.app.shared.generated.resources.screen_overview_tools_label
import dev.jakubzika.befair.domain.model.OverviewSummary
import dev.jakubzika.befair.ui.LocalAppSettings
import dev.jakubzika.befair.ui.per
import dev.jakubzika.befair.ui.atoms.BeFairDimension
import dev.jakubzika.befair.ui.atoms.BeFairTheme
import dev.jakubzika.befair.ui.atoms.LocalBeFairExtendedColors
import dev.jakubzika.befair.util.formatMoneyCents
import dev.jakubzika.befair.util.formatMoneyWhole
import org.jetbrains.compose.resources.stringResource

/**
 * Overview stats card per DESIGN.md "Cards & stats": the total invested as the display-scale hero,
 * then avg cost-per-wear and tools-per-month side by side. Neutral ink only — no color is needed.
 */
@Composable
fun OverviewStatsCard(
    summary: OverviewSummary,
    modifier: Modifier = Modifier
) {
    val settings = LocalAppSettings.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BeFairDimension.Radius.small))
            .background(MaterialTheme.colorScheme.surface)
            .padding(BeFairDimension.Spacing.md)
    ) {
        Stat(
            label = stringResource(Res.string.screen_overview_invested_label),
            value = formatMoneyWhole(summary.investedCents, settings),
            valueStyle = MaterialTheme.typography.displayLarge
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = BeFairDimension.Spacing.md),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Row(horizontalArrangement = Arrangement.spacedBy(BeFairDimension.Spacing.md)) {
            Stat(
                modifier = Modifier.weight(1f),
                label = stringResource(Res.string.screen_overview_avg_per_wear_label),
                value = summary.avgCostPerWearCents?.let { formatMoneyCents(it, settings) }
                    ?: stringResource(Res.string.item_row_stat_placeholder),
                valueStyle = MaterialTheme.typography.headlineLarge
            )
            Stat(
                modifier = Modifier.weight(1f),
                label = stringResource(Res.string.screen_overview_tools_label, settings.toolBasis.per()),
                value = formatMoneyCents(summary.toolsCostCents, settings),
                valueStyle = MaterialTheme.typography.headlineLarge
            )
        }
    }
}

@Composable
private fun Stat(
    label: String,
    value: String,
    valueStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(BeFairDimension.Spacing.xs)
    ) {
        Text(
            text = value,
            style = valueStyle.copy(fontFeatureSettings = "tnum 1"),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = LocalBeFairExtendedColors.current.ink3
        )
    }
}

@Preview(backgroundColor = 0xF5F5F2, showBackground = true)
@Composable
private fun OverviewStatsCardPreview() {
    BeFairTheme {
        OverviewStatsCard(
            summary = OverviewSummary(
                investedCents = 128000,
                avgCostPerWearCents = 609,
                toolsCostCents = 1850,
                attention = null
            )
        )
    }
}
