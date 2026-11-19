package dev.jakubzika.befair.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.screen_profile_currency_custom
import be_fair.app.shared.generated.resources.screen_profile_currency_eur
import be_fair.app.shared.generated.resources.screen_profile_currency_usd
import be_fair.app.shared.generated.resources.screen_profile_settings_summary
import be_fair.app.shared.generated.resources.screen_profile_photo_failed
import dev.jakubzika.befair.domain.AppResult
import dev.jakubzika.befair.domain.model.CurrencyChoice
import io.github.ismoy.imagepickerkmp.config.CropConfig
import io.github.ismoy.imagepickerkmp.config.GalleryConfig
import io.github.ismoy.imagepickerkmp.extensions.loadBytes
import io.github.ismoy.imagepickerkmp.picker.CompressionLevel
import io.github.ismoy.imagepickerkmp.picker.ImagePickerKMPConfig
import io.github.ismoy.imagepickerkmp.picker.ImagePickerResult
import io.github.ismoy.imagepickerkmp.picker.MimeType
import io.github.ismoy.imagepickerkmp.picker.rememberImagePickerKMP
import org.jetbrains.compose.resources.decodeToImageBitmap
import dev.jakubzika.befair.ui.LocalAppContainer
import dev.jakubzika.befair.ui.LocalAppSettings
import dev.jakubzika.befair.ui.per
import org.jetbrains.compose.resources.stringResource
import dev.jakubzika.befair.ui.templates.ProfileTemplate
import kotlinx.coroutines.launch

/** Profile / settings screen. Name and email come from the on-device profile cache. */
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    onOpenSettings: () -> Unit = {},
) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val name = remember { container.userProfileStorage.getDisplayName().orEmpty() }
    val email = remember { container.userProfileStorage.getEmail().orEmpty() }

    val settings = LocalAppSettings.current
    val currencyName = when (settings.currency) {
        CurrencyChoice.EUR -> stringResource(Res.string.screen_profile_currency_eur)
        CurrencyChoice.USD -> stringResource(Res.string.screen_profile_currency_usd)
        CurrencyChoice.CUSTOM -> stringResource(Res.string.screen_profile_currency_custom, settings.currencyLabel)
    }
    val summary = stringResource(
        Res.string.screen_profile_settings_summary,
        currencyName,
        settings.toolBasis.per(),
    )

    // Gallery picker with the circular crop UI; the result is already cropped and compressed.
    val picker = rememberImagePickerKMP(
        config = ImagePickerKMPConfig(
            galleryConfig = GalleryConfig(
                mimeTypes = listOf(MimeType.IMAGE_JPEG, MimeType.IMAGE_PNG),
                compressionLevel = CompressionLevel.HIGH,
            ),
            cropConfig = CropConfig(
                enabled = true,
                circularCrop = true,
                squareCrop = false,
                aspectRatioLocked = true,
            ),
        ),
    )
    val pictureRepository = container.profilePictureRepository
    val pictureBytes by pictureRepository.picture.collectAsState()
    val avatar = remember(pictureBytes) {
        pictureBytes?.let { runCatching { it.decodeToImageBitmap() }.getOrNull() }
    }
    var failed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { pictureRepository.refresh() }

    val result = picker.result
    LaunchedEffect(result) {
        if (result is ImagePickerResult.Success) {
            val bytes = result.photos.firstOrNull()?.loadBytes()
            failed = bytes == null || bytes.isEmpty() || pictureRepository.upload(bytes) is AppResult.Error
            // Reset last: it changes the effect key and would cancel the upload above if done earlier.
            picker.reset()
        } else if (result is ImagePickerResult.Error) {
            failed = true
            picker.reset()
        }
    }

    ProfileTemplate(
        avatar = avatar,
        onAvatarClick = {
            failed = false
            picker.launchGallery()
        },
        message = if (failed) stringResource(Res.string.screen_profile_photo_failed) else null,
        name = name,
        email = email,
        settingsSummary = summary,
        onOpenSettings = onOpenSettings,
        onSignOut = {
            scope.launch {
                container.authRepository.logout()
                onSignOut()
            }
        },
    )
}
