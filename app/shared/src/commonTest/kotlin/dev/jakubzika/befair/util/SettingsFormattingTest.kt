package dev.jakubzika.befair.util

import dev.jakubzika.befair.domain.model.AppSettings
import dev.jakubzika.befair.domain.model.CurrencyChoice
import dev.jakubzika.befair.domain.model.ToolBasis
import dev.jakubzika.befair.domain.model.toolCostCents
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsFormattingTest {

    private val eur = AppSettings()
    private val usd = AppSettings(currency = CurrencyChoice.USD)
    private fun custom(name: String) = AppSettings(currency = CurrencyChoice.CUSTOM, customCurrency = name)

    @Test
    fun `euro and dollar are prefixed`() {
        assertEquals("€12.50", formatMoneyCents(1250, eur))
        assertEquals("$12.50", formatMoneyCents(1250, usd))
        assertEquals("€280", formatMoneyWhole(27950, eur))
    }

    @Test
    fun `custom name is suffixed after a non-breaking space`() {
        assertEquals("12.50 CHF", formatMoneyCents(1250, custom("CHF")))
        assertEquals("280 CHF", formatMoneyWhole(28000, custom("CHF")))
    }

    @Test
    fun `blank custom name falls back to the generic currency sign`() {
        assertEquals("12.50 ¤", formatMoneyCents(1250, custom("")))
        assertEquals("12.50 ¤", formatMoneyCents(1250, custom("   ")))
    }

    @Test
    fun `negative amounts keep the sign in front`() {
        assertEquals("-€0.05", formatMoneyCents(-5, eur))
    }

    @Test
    fun `custom name is remembered while another currency is selected`() {
        val settings = custom("CHF").copy(currency = CurrencyChoice.EUR)
        assertEquals("€1.00", formatMoneyCents(100, settings))
        assertEquals("1.00 CHF", formatMoneyCents(100, settings.copy(currency = CurrencyChoice.CUSTOM)))
    }

    @Test
    fun `periods owned count full periods with a minimum of one`() {
        val purchased = isoToEpochDay("2026-01-01")!!
        val twentyDaysLater = isoToEpochDay("2026-01-21")!!
        assertEquals(20, periodsOwned(purchased, ToolBasis.DAY, twentyDaysLater))
        assertEquals(2, periodsOwned(purchased, ToolBasis.WEEK, twentyDaysLater))
        assertEquals(1, periodsOwned(purchased, ToolBasis.MONTH, twentyDaysLater))
        assertEquals(1, periodsOwned(purchased, ToolBasis.YEAR, twentyDaysLater))
    }

    @Test
    fun `periods owned for a purchase today or in the future is one`() {
        val today = isoToEpochDay("2026-01-01")!!
        ToolBasis.entries.forEach {
            assertEquals(1, periodsOwned(today, it, today))
            assertEquals(1, periodsOwned(today + 5, it, today))
        }
    }

    @Test
    fun `year periods are whole years of months`() {
        val purchased = isoToEpochDay("2024-01-15")!!
        assertEquals(1, periodsOwned(purchased, ToolBasis.YEAR, isoToEpochDay("2025-01-14")!!))
        assertEquals(1, periodsOwned(purchased, ToolBasis.YEAR, isoToEpochDay("2025-01-15")!!))
        assertEquals(2, periodsOwned(purchased, ToolBasis.YEAR, isoToEpochDay("2026-01-15")!!))
        assertEquals(14, periodsOwned(purchased, ToolBasis.MONTH, isoToEpochDay("2025-03-20")!!))
    }

    @Test
    fun `tool cost divides price by periods and never by zero`() {
        assertEquals(436, toolCostCents(13100, 30))
        assertEquals(13100, toolCostCents(13100, 0))
    }
}
