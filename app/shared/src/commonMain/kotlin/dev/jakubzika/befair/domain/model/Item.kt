package dev.jakubzika.befair.domain.model

sealed interface ItemUsageStats

data class ClothingStats(
    val wears: Int = 0,
    val washes: Int = 0
) : ItemUsageStats

data class ToolStats(
    val uses: Int = 0
) : ItemUsageStats

// price ÷ wears (clothing) or price ÷ months owned (tool) — the "true cost" BeFair reveals.
data class Item(
    val id: String,
    val kind: ItemKind,
    val name: String,
    val category: String,
    val price: Double,
    val purchased: String,
    val stats: ItemUsageStats
)
