package dev.jakubzika.befair.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.jakubzika.befair.domain.AppResult
import dev.jakubzika.befair.ui.LocalAppContainer
import dev.jakubzika.befair.ui.atoms.BeFairDimension
import dev.jakubzika.befair.ui.atoms.PrimaryButton
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.screen_profile_sign_out
import be_fair.app.shared.generated.resources.screen_profile_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * Protected playground screen. On entry it calls the authenticated GET /api/profile to prove
 * the Ktor client injects the Bearer token; the resolved email is shown to confirm success.
 */
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
) {
    val authRepository = LocalAppContainer.current.authRepository
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        email = when (val result = authRepository.fetchProfile()) {
            is AppResult.Success -> result.data.email
            is AppResult.Error -> result.message
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(Res.string.screen_profile_title))

            email?.let {
                Spacer(modifier = Modifier.height(BeFairDimension.Spacing.sm))
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(BeFairDimension.Spacing.md))

            PrimaryButton(
                title = stringResource(Res.string.screen_profile_sign_out),
                onClick = {
                    scope.launch {
                        authRepository.logout()
                        onSignOut()
                    }
                },
            )
        }
    }
}
