package dev.jakubzika.befair.data.repository

import dev.jakubzika.befair.domain.model.Item
import dev.jakubzika.befair.domain.repository.ItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// No item-sync endpoint exists on the server yet (tracked separately), so items
// live in memory for the lifetime of the process until that API ships.
class ItemRepositoryImpl : ItemRepository {

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    override val items: StateFlow<List<Item>> = _items.asStateFlow()

    override suspend fun addItem(item: Item) {
        _items.value = _items.value + item
    }
}
