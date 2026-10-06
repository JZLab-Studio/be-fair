package dev.jakubzika.befair.data.repository

import dev.jakubzika.befair.data.storage.SETTINGS_KEY_CURRENCY
import dev.jakubzika.befair.data.storage.SETTINGS_KEY_CUSTOM_CURRENCY
import dev.jakubzika.befair.data.storage.SETTINGS_KEY_TOOL_BASIS
import dev.jakubzika.befair.data.storage.SettingsStorage
import dev.jakubzika.befair.domain.model.AppSettings
import dev.jakubzika.befair.domain.model.CurrencyChoice
import dev.jakubzika.befair.domain.model.CustomCurrencyMaxLength
import dev.jakubzika.befair.domain.model.ToolBasis
import dev.jakubzika.befair.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepositoryImpl(
    private val storage: SettingsStorage,
) : SettingsRepository {

    private val _settings = MutableStateFlow(load())
    override val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        val next = transform(_settings.value).let {
            it.copy(customCurrency = it.customCurrency.take(CustomCurrencyMaxLength))
        }
        _settings.value = next
        storage.putString(SETTINGS_KEY_CURRENCY, next.currency.name)
        storage.putString(SETTINGS_KEY_CUSTOM_CURRENCY, next.customCurrency)
        storage.putString(SETTINGS_KEY_TOOL_BASIS, next.toolBasis.name)
    }

    // Stored values are merged over the defaults, so missing or unrecognised keys fall back safely.
    private fun load(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            currency = storage.getString(SETTINGS_KEY_CURRENCY)
                ?.let { name -> CurrencyChoice.entries.firstOrNull { it.name == name } }
                ?: defaults.currency,
            customCurrency = storage.getString(SETTINGS_KEY_CUSTOM_CURRENCY)
                ?.take(CustomCurrencyMaxLength)
                ?: defaults.customCurrency,
            toolBasis = storage.getString(SETTINGS_KEY_TOOL_BASIS)
                ?.let { name -> ToolBasis.entries.firstOrNull { it.name == name } }
                ?: defaults.toolBasis,
        )
    }
}
