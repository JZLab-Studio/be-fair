package dev.jakubzika.befair.ui.atoms

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val EnvelopeSize = 44.dp

// 44dp outline envelope (rectangle + V-shaped flap), 1.5 stroke, per DESIGN.md
// ".w-auth__sent-mark". Structural, not decorative: hairline-strong, no accent.
private fun envelopeIcon(): ImageVector =
    ImageVector.Builder(
        name = "Envelope",
        defaultWidth = EnvelopeSize,
        defaultHeight = EnvelopeSize,
        viewportWidth = 44f,
        viewportHeight = 44f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.5f,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(5f, 10f)
            lineTo(39f, 10f)
            lineTo(39f, 34f)
            lineTo(5f, 34f)
            close()
        }
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.5f,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(5f, 11f)
            lineTo(22f, 24f)
            lineTo(39f, 11f)
        }
    }.build()

@Composable
fun EnvelopeMark(modifier: Modifier = Modifier) {
    val icon = remember { envelopeIcon() }
    Icon(
        imageVector = icon,
        // Decorative for screen readers; the title and status text carry the meaning.
        contentDescription = null,
        modifier = modifier.size(EnvelopeSize),
        tint = MaterialTheme.colorScheme.outline
    )
}

@Preview(backgroundColor = 0xFFFFFF, showBackground = true)
@Composable
private fun EnvelopeMarkPreview() {
    BeFairTheme {
        EnvelopeMark()
    }
}
