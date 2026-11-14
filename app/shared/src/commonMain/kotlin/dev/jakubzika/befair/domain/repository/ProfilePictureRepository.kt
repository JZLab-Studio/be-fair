package dev.jakubzika.befair.domain.repository

import dev.jakubzika.befair.domain.AppResult
import kotlinx.coroutines.flow.StateFlow

/** The signed-in user's cropped profile picture, stored on the server with their settings. */
interface ProfilePictureRepository {
    /** Encoded image bytes (JPEG/PNG), or null when the user has none (or it is not loaded yet). */
    val picture: StateFlow<ByteArray?>

    /** Downloads the picture from the server; a user without one ends up with null. */
    suspend fun refresh(): AppResult<Unit>

    /** Uploads [bytes] (JPEG/PNG); on success [picture] switches to them. */
    suspend fun upload(bytes: ByteArray): AppResult<Unit>

    /** Forgets the in-memory picture, e.g. on sign-out. */
    fun clear()
}
