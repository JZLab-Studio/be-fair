package dev.jakubzika.befair.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey

/**
 * Top-level routes
 */
@Serializable
data object SignIn : Route

@Serializable
data class OtpVerification(val email: String) : Route

@Serializable
data object Main : Route

/**
 * Nested routes for Main navigation
 */
@Serializable
data object Overview : Route

@Serializable
data object Items : Route

@Serializable
data object Profile : Route

@Serializable
data class ItemDetail(val id: String) : Route

@Serializable
data object AddNewItem : Route

@Serializable
data object Settings : Route
