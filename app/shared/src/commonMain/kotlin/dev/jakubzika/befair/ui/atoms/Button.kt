package dev.jakubzika.befair.ui.atoms

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Button specs per DESIGN.md "Components > Buttons": 48dp tall, 4dp radius
// (rounded.md), 24dp horizontal padding, Bold 15sp label (label-action /
// titleLarge). The system is functionally flat, so no elevation/shadow is
// applied to buttons.
private val ButtonShape = RoundedCornerShape(BeFairDimension.Radius.small)
private val ButtonHeight = 48.dp
private val ButtonContentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)

@Composable
private fun flatElevation() = ButtonDefaults.buttonElevation(
    defaultElevation = 0.dp,
    pressedElevation = 0.dp,
    disabledElevation = 0.dp
)

@Composable
fun PrimaryButton(
    modifier: Modifier = Modifier,
    title: String,
    isEnabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    Button(
        onClick = onClick ?: {},
        modifier = modifier.heightIn(min = ButtonHeight),
        enabled = isEnabled,
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        contentPadding = ButtonContentPadding,
        elevation = flatElevation()
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun SecondaryButton(
    modifier: Modifier = Modifier,
    title: String,
    isEnabled: Boolean = true,
    leadingContent: (@Composable RowScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Button(
        onClick = onClick ?: {},
        modifier = modifier.heightIn(min = ButtonHeight),
        enabled = isEnabled,
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        contentPadding = ButtonContentPadding,
        elevation = flatElevation()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingContent != null) {
                leadingContent()
                Spacer(modifier = Modifier.width(BeFairDimension.Spacing.xs))
            }
            Text(text = title, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun DestructiveButton(
    modifier: Modifier = Modifier,
    title: String,
    isEnabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    Button(
        onClick = onClick ?: {},
        modifier = modifier.heightIn(min = ButtonHeight),
        enabled = isEnabled,
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.error
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
        contentPadding = ButtonContentPadding,
        elevation = flatElevation()
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
    }
}

@Preview(backgroundColor = 0xFFFFFF, showBackground = true)
@Composable
private fun PrimaryButtonPreview() {
    BeFairTheme {
        PrimaryButton(title = "Sign in")
    }
}

@Preview(backgroundColor = 0xFFFFFF, showBackground = true)
@Composable
private fun SecondaryButtonPreview() {
    BeFairTheme {
        SecondaryButton(
            title = "Continue with Google",
            leadingContent = {
                Text(text = "G", style = MaterialTheme.typography.titleLarge)
            }
        )
    }
}

@Preview(backgroundColor = 0xFFFFFF, showBackground = true)
@Composable
private fun DestructiveButtonPreview() {
    BeFairTheme {
        DestructiveButton(title = "Delete Item")
    }
}

// "Quick-add (+1)" per DESIGN.md "Components": 44dp circular leaf-green
// control, scales to 0.94 on press. The only color in an ItemRow. Under
// reduced-accent mode it becomes a neutral outlined circle (surface fill,
// ink label, hairline-strong border).
private val QuickAddSize = 44.dp
private val QuickAddPressScale = 0.94f

@Composable
fun QuickAddButton(
    contentDescription: String,
    modifier: Modifier = Modifier,
    reducedAccent: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) QuickAddPressScale else 1f,
        animationSpec = tween(durationMillis = 80),
        label = "quickAddScale"
    )
    val fillColor = if (reducedAccent) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
    val borderColor = if (reducedAccent) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
    val labelColor = if (reducedAccent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = modifier
            .size(QuickAddSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(fillColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Text(text = "+1", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = labelColor)
    }
}

@Preview(backgroundColor = 0xFFFFFF, showBackground = true)
@Composable
private fun QuickAddButtonPreview() {
    BeFairTheme {
        QuickAddButton(contentDescription = "Log a wear", onClick = {})
    }
}

@Preview(backgroundColor = 0xFFFFFF, showBackground = true)
@Composable
private fun QuickAddButtonReducedAccentPreview() {
    BeFairTheme {
        QuickAddButton(contentDescription = "Log a wear", reducedAccent = true, onClick = {})
    }
}
