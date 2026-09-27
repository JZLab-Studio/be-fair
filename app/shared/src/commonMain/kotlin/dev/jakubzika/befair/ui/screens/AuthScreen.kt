package dev.jakubzika.befair.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.jakubzika.befair.domain.AppResult
import dev.jakubzika.befair.ui.LocalAppContainer
import dev.jakubzika.befair.ui.templates.AuthMode
import dev.jakubzika.befair.ui.templates.AuthTemplate
import kotlinx.coroutines.launch

/**
 * Presenter for the combined Sign-In / Register screen. Holds transient submit state
 * (loading + server error) directly in the composable and drives the [AuthRepository].
 *
 *  - Register success -> navigate to OTP verification for [email].
 *  - Sign-in success  -> navigate to the authenticated home.
 */
@Composable
fun AuthScreen(
    onNavigateToOtp: (email: String) -> Unit,
    onAuthenticated: () -> Unit,
) {
    val authRepository = LocalAppContainer.current.authRepository
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var serverError by remember { mutableStateOf<String?>(null) }

    AuthTemplate(
        isLoading = isLoading,
        serverError = serverError,
        onSubmit = { mode, name, email, password ->
            serverError = null
            isLoading = true
            scope.launch {
                val result = when (mode) {
                    AuthMode.Register -> authRepository.register(name!!, email, password)
                    AuthMode.SignIn -> authRepository.login(email, password)
                }
                isLoading = false
                when (result) {
                    is AppResult.Success -> when (mode) {
                        AuthMode.Register -> onNavigateToOtp(email)
                        AuthMode.SignIn -> onAuthenticated()
                    }
                    is AppResult.Error -> serverError = result.message
                }
            }
        },
        // Social sign-in is not wired to the backend yet; keep existing shortcut behaviour.
        onContinueWithGoogle = onAuthenticated,
        onContinueWithApple = onAuthenticated,
    )
}
