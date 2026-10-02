package dev.jakubzika.befair.ui.atoms

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.brand_icon
import be_fair.app.shared.generated.resources.brand_wordmark
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val IconSize = 40.dp

// App icon (PNG, clipped to rounded corners) followed by the letter-spaced
// "BEFAIR" wordmark in ink.
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
    Image(
        painter = painterResource(Res.drawable.brand_icon),
        contentDescription = null,
        modifier = modifier
            .size(IconSize)
            .clip(RoundedCornerShape(BeFairDimension.Radius.medium))
    )
}

@Preview(backgroundColor = 0xFFFFFF, showBackground = true)
@Composable
private fun BrandMarkPreview() {
    BeFairTheme {
        BrandMark()
    }
}
