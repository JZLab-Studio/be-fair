package dev.jakubzika.befair.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.jakubzika.befair.ui.LocalAppContainer
import dev.jakubzika.befair.ui.templates.ProfileTemplate
import kotlinx.coroutines.launch

/** Profile / settings screen. Name and email come from the on-device profile cache. */
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val name = remember { container.userProfileStorage.getDisplayName().orEmpty() }
    val email = remember { container.userProfileStorage.getEmail().orEmpty() }

    ProfileTemplate(
        name = name,
        email = email,
        onSignOut = {
            scope.launch {
                container.authRepository.logout()
                onSignOut()
            }
        },
    )
}
