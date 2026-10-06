package dev.jakubzika.befair.ui.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.jakubzika.befair.ui.atoms.BeFairTheme
import dev.jakubzika.befair.ui.atoms.BeFairDimension
import dev.jakubzika.befair.ui.atoms.BeFairTextField
import dev.jakubzika.befair.ui.atoms.BrandMark
import dev.jakubzika.befair.ui.atoms.EmailTextField
import dev.jakubzika.befair.ui.atoms.EnvelopeMark
import dev.jakubzika.befair.ui.atoms.LinkButton
import dev.jakubzika.befair.ui.atoms.LocalBeFairExtendedColors
import dev.jakubzika.befair.ui.atoms.PasswordTextField
import dev.jakubzika.befair.ui.atoms.PrimaryButton
import dev.jakubzika.befair.ui.atoms.SecondaryButton
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.auth_back_to_sign_in
import be_fair.app.shared.generated.resources.auth_change_email
import be_fair.app.shared.generated.resources.auth_continue_with_apple
import be_fair.app.shared.generated.resources.auth_continue_with_google
import be_fair.app.shared.generated.resources.auth_divider_or
import be_fair.app.shared.generated.resources.auth_error_email_invalid
import be_fair.app.shared.generated.resources.auth_error_name_required
import be_fair.app.shared.generated.resources.auth_error_password_too_short
import be_fair.app.shared.generated.resources.auth_error_reset_network
import be_fair.app.shared.generated.resources.auth_forgot_password
import be_fair.app.shared.generated.resources.auth_name_label
import be_fair.app.shared.generated.resources.auth_name_placeholder
import be_fair.app.shared.generated.resources.auth_password_placeholder_register
import be_fair.app.shared.generated.resources.auth_password_placeholder_sign_in
import be_fair.app.shared.generated.resources.auth_resend_cooldown
import be_fair.app.shared.generated.resources.auth_resend_cooldown_again
import be_fair.app.shared.generated.resources.auth_resend_link
import be_fair.app.shared.generated.resources.auth_resend_prompt
import be_fair.app.shared.generated.resources.auth_send_reset_link
import be_fair.app.shared.generated.resources.auth_sending
import be_fair.app.shared.generated.resources.auth_sent_body
import be_fair.app.shared.generated.resources.auth_subtitle_register
import be_fair.app.shared.generated.resources.auth_subtitle_reset_password
import be_fair.app.shared.generated.resources.auth_subtitle_sign_in
import be_fair.app.shared.generated.resources.auth_title_check_email
import be_fair.app.shared.generated.resources.auth_title_create_account
import be_fair.app.shared.generated.resources.auth_title_reset_password
import be_fair.app.shared.generated.resources.auth_title_sign_in
import be_fair.app.shared.generated.resources.auth_toggle_create_one
import be_fair.app.shared.generated.resources.auth_toggle_has_account
import be_fair.app.shared.generated.resources.auth_toggle_no_account
import be_fair.app.shared.generated.resources.auth_toggle_remembered
import be_fair.app.shared.generated.resources.auth_toggle_wrong_address
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

enum class AuthMode { SignIn, Register, Forgot, Sent }

private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

private const val RESEND_COOLDOWN_SECONDS = 30

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AuthTemplate(
    onSubmit: (mode: AuthMode, name: String?, email: String, password: String) -> Unit,
    onContinueWithGoogle: () -> Unit,
    onContinueWithApple: () -> Unit,
    // Requests a reset email; [onResult] receives false only on a network failure.
    onSendReset: (email: String, onResult: (success: Boolean) -> Unit) -> Unit,
    isLoading: Boolean = false,
    serverError: String? = null,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(AuthMode.SignIn) }

    // The typed email survives mode switches so the reset field is prefilled.
    var email by remember { mutableStateOf("") }
    var name by remember(mode) { mutableStateOf("") }
    var password by remember(mode) { mutableStateOf("") }
    var nameError by remember(mode) { mutableStateOf<String?>(null) }
    var emailError by remember(mode) { mutableStateOf<String?>(null) }
    var passwordError by remember(mode) { mutableStateOf<String?>(null) }

    var isSendingReset by remember { mutableStateOf(false) }
    var cooldown by remember { mutableStateOf(0) }
    var resent by remember { mutableStateOf(false) }

    // Ticks once per second; cancelled with the composition, so nothing leaks on dispose.
    LaunchedEffect(cooldown) {
        if (cooldown > 0) {
            delay(1000)
            cooldown -= 1
        }
    }

    val nameRequiredError = stringResource(Res.string.auth_error_name_required)
    val emailInvalidError = stringResource(Res.string.auth_error_email_invalid)
    val passwordTooShortError = stringResource(Res.string.auth_error_password_too_short)
    val resetNetworkError = stringResource(Res.string.auth_error_reset_network)

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

    fun sendReset() {
        if (isSendingReset) return
        val trimmed = email.trim()
        if (!emailRegex.matches(trimmed)) {
            emailError = emailInvalidError
            return
        }
        emailError = null
        isSendingReset = true
        onSendReset(trimmed) { success ->
            isSendingReset = false
            if (success) {
                email = trimmed
                resent = false
                cooldown = RESEND_COOLDOWN_SECONDS
                mode = AuthMode.Sent
            } else {
                emailError = resetNetworkError
            }
        }
    }

    fun resend() {
        if (cooldown > 0 || isSendingReset) return
        isSendingReset = true
        onSendReset(email.trim()) { success ->
            isSendingReset = false
            if (success) {
                resent = true
                cooldown = RESEND_COOLDOWN_SECONDS
            }
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

        if (mode == AuthMode.Sent) {
            EnvelopeMark()
            Spacer(modifier = Modifier.height(BeFairDimension.Spacing.md))
        }

        Text(
            text = when (mode) {
                AuthMode.SignIn -> stringResource(Res.string.auth_title_sign_in)
                AuthMode.Register -> stringResource(Res.string.auth_title_create_account)
                AuthMode.Forgot -> stringResource(Res.string.auth_title_reset_password)
                AuthMode.Sent -> stringResource(Res.string.auth_title_check_email)
            },
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(BeFairDimension.Spacing.xs))

        if (mode == AuthMode.Sent) {
            // Static sentence only: the countdown lives outside this live region so ticks aren't announced.
            Text(
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                text = sentBody(email.trim()),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = when (mode) {
                    AuthMode.SignIn -> stringResource(Res.string.auth_subtitle_sign_in)
                    AuthMode.Register -> stringResource(Res.string.auth_subtitle_register)
                    else -> stringResource(Res.string.auth_subtitle_reset_password)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(BeFairDimension.Spacing.lg))

        when (mode) {
            AuthMode.SignIn, AuthMode.Register -> {
                SignInRegisterForm(
                    mode = mode,
                    name = name,
                    onNameChange = { name = it },
                    nameError = nameError,
                    email = email,
                    onEmailChange = { email = it },
                    emailError = emailError,
                    password = password,
                    onPasswordChange = { password = it },
                    passwordError = passwordError,
                    isLoading = isLoading,
                    serverError = serverError,
                    onSubmit = ::validateAndSubmit,
                    onForgotPassword = { mode = AuthMode.Forgot },
                    onContinueWithGoogle = onContinueWithGoogle,
                    onContinueWithApple = onContinueWithApple
                )
            }

            AuthMode.Forgot -> {
                EmailTextField(
                    value = email,
                    onValueChange = { email = it },
                    isEnabled = !isSendingReset,
                    isError = emailError != null,
                    errorMessage = emailError,
                    imeAction = ImeAction.Send,
                    keyboardActions = KeyboardActions(onSend = { sendReset() })
                )

                Spacer(modifier = Modifier.height(BeFairDimension.Spacing.lg))

                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    title = if (isSendingReset) {
                        stringResource(Res.string.auth_sending)
                    } else {
                        stringResource(Res.string.auth_send_reset_link)
                    },
                    isEnabled = !isSendingReset,
                    onClick = ::sendReset
                )
            }

            AuthMode.Sent -> {
                SecondaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(Res.string.auth_back_to_sign_in),
                    onClick = { mode = AuthMode.SignIn }
                )

                Spacer(modifier = Modifier.height(BeFairDimension.Spacing.sm))

                ResendLine(
                    cooldown = cooldown,
                    resent = resent,
                    isEnabled = !isSendingReset,
                    onResend = ::resend
                )
            }
        }

        Spacer(modifier = Modifier.height(BeFairDimension.Spacing.xl))

        AuthFooter(mode = mode, onModeChange = { mode = it })
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SignInRegisterForm(
    mode: AuthMode,
    name: String,
    onNameChange: (String) -> Unit,
    nameError: String?,
    email: String,
    onEmailChange: (String) -> Unit,
    emailError: String?,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordError: String?,
    isLoading: Boolean,
    serverError: String?,
    onSubmit: () -> Unit,
    onForgotPassword: () -> Unit,
    onContinueWithGoogle: () -> Unit,
    onContinueWithApple: () -> Unit,
) {
    Column {
        if (mode == AuthMode.Register) {
            BeFairTextField(
                value = name,
                onValueChange = onNameChange,
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
            onValueChange = onEmailChange,
            isEnabled = !isLoading,
            isError = emailError != null,
            errorMessage = emailError
        )

        Spacer(modifier = Modifier.height(BeFairDimension.Spacing.sm))

        // key(mode) resets the field's visibility toggle when switching modes.
        key(mode) {
            PasswordTextField(
                value = password,
                onValueChange = onPasswordChange,
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

        // ".w-auth__forgot": sign-in only, right-aligned under the password field.
        // LinkButton already guarantees a 44dp tap area.
        if (mode == AuthMode.SignIn) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                LinkButton(
                    text = stringResource(Res.string.auth_forgot_password),
                    onClick = onForgotPassword
                )
            }
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
            onClick = onSubmit
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
    }
}

// Body text with the (user-supplied) address in bold ink, ".w-auth__email".
@Composable
private fun sentBody(email: String): AnnotatedString {
    val full = stringResource(Res.string.auth_sent_body, email)
    val start = full.indexOf(email)
    return buildAnnotatedString {
        if (start < 0 || email.isEmpty()) {
            append(full)
        } else {
            append(full.substring(0, start))
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                append(email)
            }
            append(full.substring(start + email.length))
        }
    }
}

// ".w-auth__resend": 13sp ink-2, tabular figures, >= 44dp tall.
@Composable
private fun ResendLine(
    cooldown: Int,
    resent: Boolean,
    isEnabled: Boolean,
    onResend: () -> Unit,
) {
    val textStyle = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum")
    if (cooldown > 0) {
        Box(
            modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = stringResource(
                    if (resent) Res.string.auth_resend_cooldown_again else Res.string.auth_resend_cooldown,
                    cooldown.toString()
                ),
                style = textStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        FlowRow(
            modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
            horizontalArrangement = Arrangement.spacedBy(BeFairDimension.Spacing.xs),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = stringResource(Res.string.auth_resend_prompt),
                style = textStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isEnabled) {
                LinkButton(text = stringResource(Res.string.auth_resend_link), onClick = onResend)
            }
        }
    }
}

// ".w-auth__foot": "<prompt> <link>" mode toggle at the bottom of every mode.
@Composable
private fun AuthFooter(mode: AuthMode, onModeChange: (AuthMode) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(BeFairDimension.Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (prompt, link, target) = when (mode) {
            AuthMode.SignIn -> Triple(
                stringResource(Res.string.auth_toggle_no_account),
                stringResource(Res.string.auth_toggle_create_one),
                AuthMode.Register
            )
            AuthMode.Register -> Triple(
                stringResource(Res.string.auth_toggle_has_account),
                stringResource(Res.string.auth_title_sign_in),
                AuthMode.SignIn
            )
            AuthMode.Forgot -> Triple(
                stringResource(Res.string.auth_toggle_remembered),
                stringResource(Res.string.auth_title_sign_in),
                AuthMode.SignIn
            )
            AuthMode.Sent -> Triple(
                stringResource(Res.string.auth_toggle_wrong_address),
                stringResource(Res.string.auth_change_email),
                AuthMode.Forgot
            )
        }
        Text(
            text = prompt,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LinkButton(text = link, onClick = { onModeChange(target) })
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
            onContinueWithApple = {},
            onSendReset = { _, onResult -> onResult(true) }
        )
    }
}
