package dev.jakubzika.befair.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    /** Same ordering the server uses, so the cache matches a fresh fetch. */
    @Query("SELECT * FROM items ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<ItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ItemEntity>)

    @Query("DELETE FROM items")
    suspend fun clear()

    /** Replaces the whole cache with the server's list in one transaction. */
    @Transaction
    suspend fun replaceAll(items: List<ItemEntity>) {
        clear()
        upsertAll(items)
    }
}
