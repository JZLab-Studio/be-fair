package dev.jakubzika.befair.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.jakubzika.befair.domain.AppResult
import dev.jakubzika.befair.ui.LocalAppContainer
import dev.jakubzika.befair.ui.templates.OtpVerificationTemplate
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.otp_error_incomplete
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

/**
 * Presenter for OTP verification. On a successful verify the [AuthRepository] persists the
 * returned tokens, so this screen only needs to navigate onward.
 */
@Composable
fun OtpVerificationScreen(
    email: String,
    onVerified: () -> Unit,
) {
    val authRepository = LocalAppContainer.current.authRepository
    val scope = rememberCoroutineScope()

    var otp by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    OtpVerificationTemplate(
        email = email,
        otp = otp,
        onOtpChange = {
            otp = it
            error = null
        },
        isLoading = isLoading,
        errorMessage = error,
        onSubmit = {
            if (otp.length != 6) {
                scope.launch { error = getString(Res.string.otp_error_incomplete) }
                return@OtpVerificationTemplate
            }
            isLoading = true
            scope.launch {
                when (val result = authRepository.verifyOtp(email, otp)) {
                    is AppResult.Success -> {
                        isLoading = false
                        onVerified()
                    }
                    is AppResult.Error -> {
                        isLoading = false
                        error = result.message
                    }
                }
            }
        },
    )
}
