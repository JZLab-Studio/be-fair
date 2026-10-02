package dev.jakubzika.befair.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.brand_mark
import be_fair.app.shared.generated.resources.brand_wordmark
import org.jetbrains.compose.resources.stringResource

private val IconSize = 40.dp
private val BarWidth = 8.dp
private val BarHeight = 3.dp

// App icon tile per the design proposal: dark ink square with a white "B" and
// two green bars (the "=") — same design as the launcher icon — followed by the
// letter-spaced "BEFAIR" wordmark in ink.
@Composable
fun BrandMark(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        BrandIcon()
        Spacer(modifier = Modifier.width(BeFairDimension.Spacing.md))
        Text(
            text = stringResource(Res.string.brand_wordmark),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BrandIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(IconSize)
            .background(MaterialTheme.colorScheme.inverseSurface, RoundedCornerShape(BeFairDimension.Radius.medium)),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.brand_mark),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.inverseOnSurface
            )
            Spacer(modifier = Modifier.width(2.dp))
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .width(BarWidth)
                            .height(BarHeight)
                            .background(MaterialTheme.colorScheme.inversePrimary)
                    )
                }
            }
        }
    }
}

@Preview(backgroundColor = 0xFFFFFF, showBackground = true)
@Composable
private fun BrandMarkPreview() {
    BeFairTheme {
        BrandMark()
    }
}
