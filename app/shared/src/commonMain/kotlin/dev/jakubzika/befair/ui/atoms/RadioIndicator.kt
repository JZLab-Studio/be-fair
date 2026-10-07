package dev.jakubzika.befair.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val RadioSize = 20.dp
private val RadioDotSize = 10.dp
private val RadioRing = 1.5.dp

/**
 * Single-choice indicator: a 20dp ring with a 10dp dot when selected. Neutral ink only — selection
 * is never shown in green. Decorative: the row that hosts it carries the radio semantics.
 */
@Composable
fun RadioIndicator(selected: Boolean, modifier: Modifier = Modifier) {
    val ink = MaterialTheme.colorScheme.onSurface
    val ringColor = if (selected) ink else MaterialTheme.colorScheme.outline
    Box(
        modifier = modifier
            .size(RadioSize)
            .border(RadioRing, ringColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(RadioDotSize)
                    .background(ink, CircleShape)
            )
        }
    }
}

@Preview(backgroundColor = 0xFFFFFF, showBackground = true)
@Composable
private fun RadioIndicatorPreview() {
    BeFairTheme {
        RadioIndicator(selected = true)
    }
}
