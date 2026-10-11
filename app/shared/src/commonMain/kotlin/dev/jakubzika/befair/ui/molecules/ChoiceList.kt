package dev.jakubzika.befair.ui.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.jakubzika.befair.ui.atoms.BeFairDimension
import dev.jakubzika.befair.ui.atoms.BeFairTheme
import dev.jakubzika.befair.ui.atoms.RadioIndicator

private val RowMinHeight = 56.dp
private val FocusInset = 4.dp
private val ListShape = RoundedCornerShape(BeFairDimension.Radius.small)

/** One option of a [ChoiceList]: a [title] with a secondary [meta] line. */
data class ChoiceOption<T>(
    val value: T,
    val title: String,
    val meta: String,
)

/**
 * Single-choice list: a hairline-bordered surface card with a divider between rows. Each row is
 * at least 56dp tall — title and meta on the left, a neutral [RadioIndicator] on the right — and
 * is a selectable radio button, so it is reachable with Tab and works with Enter and Space.
 * [groupDescription] labels the group for assistive technology.
 */
@Composable
fun <T> ChoiceList(
    options: List<ChoiceOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    groupDescription: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(ListShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, ListShape)
            .selectableGroup()
            .semantics { contentDescription = groupDescription }
    ) {
        options.forEachIndexed { index, option ->
            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ChoiceRow(
                option = option,
                selected = option.value == selected,
                onClick = { onSelect(option.value) }
            )
        }
    }
}

@Composable
private fun <T> ChoiceRow(option: ChoiceOption<T>, selected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val ink = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = RowMinHeight)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.RadioButton,
                onClick = onClick
            )
            .then(
                if (focused) {
                    Modifier
                        .padding(FocusInset)
                        .border(1.dp, ink, RoundedCornerShape(BeFairDimension.Radius.small))
                        .padding(horizontal = BeFairDimension.Spacing.md - FocusInset)
                } else {
                    Modifier.padding(horizontal = BeFairDimension.Spacing.md)
                }
            )
            .padding(vertical = BeFairDimension.Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BeFairDimension.Spacing.md)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                color = ink
            )
            Text(
                text = option.meta,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        RadioIndicator(selected = selected)
    }
}

@Preview(backgroundColor = 0xF5F5F2, showBackground = true)
@Composable
private fun ChoiceListPreview() {
    BeFairTheme {
        ChoiceList(
            options = listOf(
                ChoiceOption("EUR", "Euro", "€12.50"),
                ChoiceOption("USD", "US dollar", "$12.50"),
                ChoiceOption("custom", "Custom", "Your own name, e.g. CHF"),
            ),
            selected = "EUR",
            onSelect = {},
            groupDescription = "Currency"
        )
    }
}
