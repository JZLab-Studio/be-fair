package dev.jakubzika.befair.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    /** Emits null until the first save or download, then the stored row. */
    @Query("SELECT * FROM settings WHERE id = $SETTINGS_ROW_ID")
    fun observe(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = $SETTINGS_ROW_ID")
    suspend fun get(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: SettingsEntity)

    @Query("DELETE FROM settings")
    suspend fun clear()

    /**
     * Stores the server's copy unless a local edit arrived while the download was in flight;
     * that edit stays pending and is uploaded next.
     */
    @Transaction
    suspend fun replaceIfClean(downloaded: SettingsEntity) {
        if (get()?.dirty != true) upsert(downloaded)
    }

    /**
     * Marks the row accepted by the server, but only if it still holds the [uploaded] values —
     * an edit made during the upload has to stay pending.
     */
    @Transaction
    suspend fun markClean(uploaded: SettingsEntity) {
        val current = get() ?: return
        if (current.copy(dirty = false) == uploaded.copy(dirty = false)) upsert(current.copy(dirty = false))
    }
}
