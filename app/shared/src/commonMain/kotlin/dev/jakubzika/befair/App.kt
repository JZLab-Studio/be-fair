package dev.jakubzika.befair

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import dev.jakubzika.befair.di.AppContainer
import dev.jakubzika.befair.ui.LocalAppContainer
import dev.jakubzika.befair.ui.LocalAppSettings
import dev.jakubzika.befair.ui.atoms.BeFairTheme
import dev.jakubzika.befair.ui.navigation.AddNewItem
import dev.jakubzika.befair.ui.navigation.ItemDetail
import dev.jakubzika.befair.ui.navigation.Items
import dev.jakubzika.befair.ui.navigation.Main
import dev.jakubzika.befair.ui.navigation.OtpVerification
import dev.jakubzika.befair.ui.navigation.Overview
import dev.jakubzika.befair.ui.navigation.Profile
import dev.jakubzika.befair.ui.navigation.Route
import dev.jakubzika.befair.ui.navigation.Settings
import dev.jakubzika.befair.ui.navigation.SignIn
import dev.jakubzika.befair.ui.screens.AuthScreen
import dev.jakubzika.befair.ui.screens.MainScreen
import dev.jakubzika.befair.ui.screens.OtpVerificationScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer

@Composable
@Preview
fun App() {
    // DI container initialization
    val appContainer = remember { AppContainer() }
    // Collected once per app render so every screen formats with the same live settings.
    val settings by appContainer.settingsRepository.settings.collectAsState()
    CompositionLocalProvider(
        LocalAppContainer provides appContainer,
        LocalAppSettings provides settings
    ) {
        // General app theme
        BeFairTheme {
            val config = remember {
                SavedStateConfiguration {
                    serializersModule = SerializersModule {
                        polymorphic(NavKey::class) {
                            subclass(SignIn::class, serializer())
                            subclass(OtpVerification::class, serializer())
                            subclass(Main::class, serializer())
                            // Also include nested routes for serialization support if needed at top level
                            subclass(Overview::class, serializer())
                            subclass(Items::class, serializer())
                            subclass(Profile::class, serializer())
                            subclass(ItemDetail::class, serializer())
                            subclass(AddNewItem::class, serializer())
                            subclass(Settings::class, serializer())
                        }
                    }
                }
            }
            @Suppress("UNCHECKED_CAST")
            val backStack: NavBackStack<Route> = rememberNavBackStack(config, SignIn) as NavBackStack<Route>

            NavDisplay(
                modifier = Modifier.fillMaxSize(),
                backStack = backStack,
                onBack = { backStack.removeLast() },
                entryProvider = entryProvider {
                    entry<SignIn> {
                        AuthScreen(
                            onNavigateToOtp = { email -> backStack.add(OtpVerification(email)) },
                            onAuthenticated = { backStack.resetTo(Main) }
                        )
                    }
                    entry<OtpVerification> { key ->
                        OtpVerificationScreen(
                            email = key.email,
                            onVerified = { backStack.resetTo(Main) }
                        )
                    }
                    entry<Main> {
                        MainScreen(
                            onSignOut = { backStack.resetTo(SignIn) }
                        )
                    }
                }
            )
        }
    }
}

/** Clears the entire back stack and makes [route] the only (root) destination. */
private fun NavBackStack<Route>.resetTo(route: Route) {
    while (isNotEmpty()) {
        removeLast()
    }
    add(route)
}
