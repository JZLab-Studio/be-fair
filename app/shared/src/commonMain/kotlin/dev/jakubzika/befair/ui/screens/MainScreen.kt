package dev.jakubzika.befair.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import dev.jakubzika.befair.ui.navigation.AddNewItem
import dev.jakubzika.befair.ui.navigation.ItemDetail
import dev.jakubzika.befair.ui.navigation.Items
import dev.jakubzika.befair.ui.navigation.Overview
import dev.jakubzika.befair.ui.navigation.Profile
import dev.jakubzika.befair.ui.navigation.Route
import dev.jakubzika.befair.ui.organisms.BottomBar
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer

@Composable
fun MainScreen(
    onSignOut: () -> Unit
) {
    val config = remember {
        SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Overview::class, serializer())
                    subclass(Items::class, serializer())
                    subclass(Profile::class, serializer())
                    subclass(ItemDetail::class, serializer())
                    subclass(AddNewItem::class, serializer())
                }
            }
        }
    }

    val overviewBackStack = rememberNavBackStack(config, Overview)
    val itemsBackStack = rememberNavBackStack(config, Items)
    val profileBackStack = rememberNavBackStack(config, Profile)

    val currentTab: MutableState<Route> = remember { mutableStateOf(Overview) }

    val currentBackStack: NavBackStack<*> = when (currentTab.value) {
        Overview -> overviewBackStack
        Items -> itemsBackStack
        Profile -> profileBackStack
        else -> overviewBackStack // Should not happen
    }

    Scaffold(
        bottomBar = {
            BottomBar(
                currentRoute = currentTab.value,
                onNavigateToOverview = { currentTab.value = Overview },
                onNavigateToItems = { currentTab.value = Items },
                onNavigateToProfile = { currentTab.value = Profile }
            )
        }
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier.fillMaxSize().padding(innerPadding).statusBarsPadding(),
            backStack = currentBackStack,
            onBack = { currentBackStack.removeLast() },
            entryProvider = entryProvider {
                entry<Overview> {
                    OverviewScreen(
                        onNavToAddNewItemScreen = { (overviewBackStack as NavBackStack<Route>).add(AddNewItem) },
                        onNavToItemDetailScreen = { id -> (overviewBackStack as NavBackStack<Route>).add(ItemDetail(id)) },
                        onNavToItemsScreen = { currentTab.value = Items }
                    )
                }
                entry<Items> {
                    ItemsScreen(
                        onNavToItemDetailScreen = { id -> (itemsBackStack as NavBackStack<Route>).add(ItemDetail(id)) },
                        onNavToAddNewItemScreen = { (itemsBackStack as NavBackStack<Route>).add(AddNewItem) }
                    )
                }
                entry<Profile> {
                    ProfileScreen(
                        onSignOut = onSignOut
                    )
                }
                entry<ItemDetail> { key ->
                    ItemDetailScreen(
                        id = key.id,
                        onBack = { currentBackStack.removeLast() }
                    )
                }
                entry<AddNewItem> {
                    AddNewItemScreen(
                        onCreateItem = { currentBackStack.removeLast() },
                        onBack = { currentBackStack.removeLast() }
                    )
                }
            }
        )
    }
}
