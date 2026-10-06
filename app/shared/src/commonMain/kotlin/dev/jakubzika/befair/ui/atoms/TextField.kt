package dev.jakubzika.befair.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.auth_hide_password
import be_fair.app.shared.generated.resources.auth_show_password
import org.jetbrains.compose.resources.stringResource

// Input specs per DESIGN.md "Components > Inputs": 48dp tall, 4dp radius,
// strong-hairline border that goes ink on focus and orange in the error
// state, with an orange helper message below. The label above the field is
// "label-caps" (labelLarge), uppercased at the call site.
private val TextFieldShape = RoundedCornerShape(BeFairDimension.Radius.small)
private val TextFieldHeight = 48.dp
private val TextFieldHorizontalPadding = 14.dp

// Trailing action per DESIGN.md: 44dp hit target inset 2dp from the border, so
// text gets 52dp of end padding and never runs under it.
private val TrailingTargetSize = 44.dp
private val TrailingInset = 2.dp
private val TextFieldTrailingPadding = 52.dp

// 22x22 outline eye, 1.5 stroke; the slash is added when `crossed`.
private fun eyeIcon(crossed: Boolean): ImageVector =
    ImageVector.Builder(
        name = if (crossed) "EyeCrossed" else "Eye",
        defaultWidth = 22.dp,
        defaultHeight = 22.dp,
        viewportWidth = 22f,
        viewportHeight = 22f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.5f,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(2.5f, 11f)
            curveTo(4.5f, 7.2f, 7.5f, 5.25f, 11f, 5.25f)
            curveTo(14.5f, 5.25f, 17.5f, 7.2f, 19.5f, 11f)
            curveTo(17.5f, 14.8f, 14.5f, 16.75f, 11f, 16.75f)
            curveTo(7.5f, 16.75f, 4.5f, 14.8f, 2.5f, 11f)
            close()
        }
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f) {
            // circle cx=11 cy=11 r=2.75
            moveTo(8.25f, 11f)
            arcTo(2.75f, 2.75f, 0f, true, true, 13.75f, 11f)
            arcTo(2.75f, 2.75f, 0f, true, true, 8.25f, 11f)
            close()
        }
        if (crossed) {
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f) {
                moveTo(4f, 18f)
                lineTo(18f, 4f)
            }
        }
    }.build()

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BeFairTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isEnabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    autofillContentType: ContentType? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        isFocused -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.outline
    }

    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(BeFairDimension.Spacing.xs))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(TextFieldHeight)
                .background(MaterialTheme.colorScheme.surface, TextFieldShape)
                .border(1.dp, borderColor, TextFieldShape)
                .onFocusChanged { isFocused = it.isFocused }
                .semantics {
                    contentDescription = label
                    if (autofillContentType != null) {
                        contentType = autofillContentType
                    }
                },
            enabled = isEnabled,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            singleLine = singleLine,
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(TextFieldHeight)
                            .padding(
                                start = TextFieldHorizontalPadding,
                                end = if (trailingContent != null) {
                                    TextFieldTrailingPadding
                                } else {
                                    TextFieldHorizontalPadding
                                }
                            ),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                    if (trailingContent != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = TrailingInset)
                                .size(TrailingTargetSize),
                            contentAlignment = Alignment.Center
                        ) {
                            trailingContent()
                        }
                    }
                }
            }
        )
        if (isError && errorMessage != null) {
            Spacer(modifier = Modifier.height(BeFairDimension.Spacing.xs))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EmailTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Email",
    placeholder: String = "name@example.com",
    isEnabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    BeFairTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        isEnabled = isEnabled,
        isError = isError,
        errorMessage = errorMessage,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            capitalization = KeyboardCapitalization.None
        ),
        autofillContentType = ContentType.EmailAddress
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PasswordTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Password",
    placeholder: String = "Your password",
    isEnabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    autofillContentType: ContentType = ContentType.Password
) {
    var visible by remember { mutableStateOf(false) }

    BeFairTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        isEnabled = isEnabled,
        isError = isError,
        errorMessage = errorMessage,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        autofillContentType = autofillContentType,
        trailingContent = {
            // The icon shows the current state and stays neutral, even in error.
            IconButton(
                onClick = { visible = !visible },
                modifier = Modifier.size(TrailingTargetSize)
            ) {
                Icon(
                    imageVector = eyeIcon(crossed = visible),
                    contentDescription = stringResource(
                        if (visible) Res.string.auth_hide_password else Res.string.auth_show_password
                    ),
                    tint = if (visible) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        LocalBeFairExtendedColors.current.ink3
                    }
                )
            }
        }
    )
}

@Preview(backgroundColor = 0xFFFFFF, showBackground = true)
@Composable
private fun EmailTextFieldPreview() {
    BeFairTheme {
        EmailTextField(value = "", onValueChange = {})
    }
}

@Preview(backgroundColor = 0xFFFFFF, showBackground = true)
@Composable
private fun PasswordTextFieldPreview() {
    BeFairTheme {
        PasswordTextField(value = "", onValueChange = {})
    }
}

@Preview(backgroundColor = 0xFFFFFF, showBackground = true)
@Composable
private fun TextFieldFilledPreview() {
    BeFairTheme {
        BeFairTextField(
            value = "Test",
            onValueChange = {},
            label = "Name",
            placeholder = "e.g. Navy chinos"
        )
    }
}

@Preview(backgroundColor = 0xFFFFFF, showBackground = true)
@Composable
private fun TextFieldErrorPreview() {
    BeFairTheme {
        BeFairTextField(
            value = "",
            onValueChange = {},
            label = "Email",
            placeholder = "name@example.com",
            isError = true,
            errorMessage = "Enter a valid email address"
        )
    }
}
