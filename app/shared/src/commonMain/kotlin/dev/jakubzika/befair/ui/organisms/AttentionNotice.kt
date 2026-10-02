package dev.jakubzika.befair.ui.organisms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.jakubzika.befair.ui.atoms.BeFairDimension
import dev.jakubzika.befair.ui.atoms.BeFairTheme
import dev.jakubzika.befair.ui.atoms.LocalBeFairExtendedColors

private val NoticeShape = RoundedCornerShape(BeFairDimension.Radius.small)

/**
 * Advisory block per DESIGN.md "Notice": honey-yellow tint, a leading yellow dot and a
 * yellow-mixed border. Informational only — never error styling. Tapping it runs [onClick].
 */
@Composable
fun AttentionNotice(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalBeFairExtendedColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(NoticeShape)
            .background(colors.noticeTint)
            .border(
                BorderStroke(1.dp, lerp(colors.noticeTint, colors.notice, 0.4f)),
                NoticeShape
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(BeFairDimension.Spacing.sm + BeFairDimension.Spacing.xs)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(colors.notice)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onNotice,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(backgroundColor = 0xF5F5F2, showBackground = true)
@Composable
private fun AttentionNoticePreview() {
    BeFairTheme {
        AttentionNotice(text = "Wool overcoat is your highest cost per wear at €20.00.", onClick = {})
    }
}
