package dev.jakubzika.befair.ui.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.tooling.preview.Preview
import dev.jakubzika.befair.ui.atoms.BeFairTheme
import dev.jakubzika.befair.ui.atoms.BeFairDimension
import dev.jakubzika.befair.ui.atoms.BeFairTextField
import dev.jakubzika.befair.ui.atoms.BrandMark
import dev.jakubzika.befair.ui.atoms.EmailTextField
import dev.jakubzika.befair.ui.atoms.LinkButton
import dev.jakubzika.befair.ui.atoms.LocalBeFairExtendedColors
import dev.jakubzika.befair.ui.atoms.PasswordTextField
import dev.jakubzika.befair.ui.atoms.PrimaryButton
import dev.jakubzika.befair.ui.atoms.SecondaryButton
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.auth_continue_with_apple
import be_fair.app.shared.generated.resources.auth_continue_with_google
import be_fair.app.shared.generated.resources.auth_divider_or
import be_fair.app.shared.generated.resources.auth_error_email_invalid
import be_fair.app.shared.generated.resources.auth_error_name_required
import be_fair.app.shared.generated.resources.auth_error_password_too_short
import be_fair.app.shared.generated.resources.auth_name_label
import be_fair.app.shared.generated.resources.auth_name_placeholder
import be_fair.app.shared.generated.resources.auth_password_placeholder_register
import be_fair.app.shared.generated.resources.auth_password_placeholder_sign_in
import be_fair.app.shared.generated.resources.auth_subtitle_register
import be_fair.app.shared.generated.resources.auth_subtitle_sign_in
import be_fair.app.shared.generated.resources.auth_title_create_account
import be_fair.app.shared.generated.resources.auth_title_sign_in
import be_fair.app.shared.generated.resources.auth_toggle_create_one
import be_fair.app.shared.generated.resources.auth_toggle_has_account
import be_fair.app.shared.generated.resources.auth_toggle_no_account
import org.jetbrains.compose.resources.stringResource

enum class AuthMode { SignIn, Register }

private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AuthTemplate(
    onSubmit: (mode: AuthMode, name: String?, email: String, password: String) -> Unit,
    onContinueWithGoogle: () -> Unit,
    onContinueWithApple: () -> Unit,
    isLoading: Boolean = false,
    serverError: String? = null,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(AuthMode.SignIn) }

    var name by remember(mode) { mutableStateOf("") }
    var email by remember(mode) { mutableStateOf("") }
    var password by remember(mode) { mutableStateOf("") }
    var nameError by remember(mode) { mutableStateOf<String?>(null) }
    var emailError by remember(mode) { mutableStateOf<String?>(null) }
    var passwordError by remember(mode) { mutableStateOf<String?>(null) }

    val nameRequiredError = stringResource(Res.string.auth_error_name_required)
    val emailInvalidError = stringResource(Res.string.auth_error_email_invalid)
    val passwordTooShortError = stringResource(Res.string.auth_error_password_too_short)

    fun validateAndSubmit() {
        val isRegister = mode == AuthMode.Register
        val nextNameError = if (isRegister && name.isBlank()) nameRequiredError else null
        val nextEmailError = if (!emailRegex.matches(email)) emailInvalidError else null
        val nextPasswordError = if (password.length < 8) passwordTooShortError else null

        nameError = nextNameError
        emailError = nextEmailError
        passwordError = nextPasswordError

        if (nextNameError == null && nextEmailError == null && nextPasswordError == null) {
            onSubmit(mode, if (isRegister) name.trim() else null, email.trim(), password)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(BeFairDimension.Spacing.md)
    ) {
        BrandMark()

        Spacer(modifier = Modifier.height(BeFairDimension.Spacing.xl))

        Text(
            text = if (mode == AuthMode.SignIn) {
                stringResource(Res.string.auth_title_sign_in)
            } else {
                stringResource(Res.string.auth_title_create_account)
            },
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(BeFairDimension.Spacing.xs))

        Text(
            text = if (mode == AuthMode.SignIn) {
                stringResource(Res.string.auth_subtitle_sign_in)
            } else {
                stringResource(Res.string.auth_subtitle_register)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(BeFairDimension.Spacing.lg))

        if (mode == AuthMode.Register) {
            BeFairTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(Res.string.auth_name_label),
                placeholder = stringResource(Res.string.auth_name_placeholder),
                isEnabled = !isLoading,
                isError = nameError != null,
                errorMessage = nameError,
                autofillContentType = ContentType.PersonFullName
            )
            Spacer(modifier = Modifier.height(BeFairDimension.Spacing.sm))
        }

        EmailTextField(
            value = email,
            onValueChange = { email = it },
            isEnabled = !isLoading,
            isError = emailError != null,
            errorMessage = emailError
        )

        Spacer(modifier = Modifier.height(BeFairDimension.Spacing.sm))

        // key(mode) resets the field's visibility toggle when switching modes.
        key(mode) {
            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = if (mode == AuthMode.Register) {
                    stringResource(Res.string.auth_password_placeholder_register)
                } else {
                    stringResource(Res.string.auth_password_placeholder_sign_in)
                },
                isEnabled = !isLoading,
                isError = passwordError != null,
                errorMessage = passwordError,
                autofillContentType = if (mode == AuthMode.Register) {
                    ContentType.NewPassword
                } else {
                    ContentType.Password
                }
            )
        }

        if (serverError != null) {
            Spacer(modifier = Modifier.height(BeFairDimension.Spacing.sm))
            Text(
                text = serverError,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(BeFairDimension.Spacing.lg))

        PrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            title = if (mode == AuthMode.SignIn) {
                stringResource(Res.string.auth_title_sign_in)
            } else {
                stringResource(Res.string.auth_title_create_account)
            },
            isEnabled = !isLoading,
            onClick = ::validateAndSubmit
        )

        if (isLoading) {
            Spacer(modifier = Modifier.height(BeFairDimension.Spacing.md))
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        Spacer(modifier = Modifier.height(BeFairDimension.Spacing.lg))

        AuthDivider()

        Spacer(modifier = Modifier.height(BeFairDimension.Spacing.lg))

        SecondaryButton(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(Res.string.auth_continue_with_google),
            onClick = onContinueWithGoogle
        )

        Spacer(modifier = Modifier.height(BeFairDimension.Spacing.sm))

        SecondaryButton(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(Res.string.auth_continue_with_apple),
            onClick = onContinueWithApple
        )

        Spacer(modifier = Modifier.height(BeFairDimension.Spacing.xl))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BeFairDimension.Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (mode == AuthMode.SignIn) {
                    stringResource(Res.string.auth_toggle_no_account)
                } else {
                    stringResource(Res.string.auth_toggle_has_account)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinkButton(
                text = if (mode == AuthMode.SignIn) {
                    stringResource(Res.string.auth_toggle_create_one)
                } else {
                    stringResource(Res.string.auth_title_sign_in)
                },
                onClick = {
                    mode = if (mode == AuthMode.SignIn) AuthMode.Register else AuthMode.SignIn
                }
            )
        }
    }
}

// "or" divider per DESIGN.md ".w-auth__divider": hairline rule with centered
// "or" in ink-3. Single-use, kept local to this template.
@Composable
private fun AuthDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Box(
            modifier = Modifier.padding(horizontal = BeFairDimension.Spacing.sm)
        ) {
            Text(
                text = stringResource(Res.string.auth_divider_or),
                style = MaterialTheme.typography.bodySmall,
                color = LocalBeFairExtendedColors.current.ink3
            )
        }
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Preview(backgroundColor = 0xFFFFFF, showBackground = true)
@Composable
private fun AuthTemplatePreview() {
    BeFairTheme {
        AuthTemplate(
            onSubmit = { _, _, _, _ -> },
            onContinueWithGoogle = {},
            onContinueWithApple = {}
        )
    }
}
