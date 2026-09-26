package dev.jakubzika.befair.domain.repository

import dev.jakubzika.befair.domain.model.Item
import kotlinx.coroutines.flow.StateFlow

interface ItemRepository {
    val items: StateFlow<List<Item>>
    suspend fun addItem(item: Item)
}
