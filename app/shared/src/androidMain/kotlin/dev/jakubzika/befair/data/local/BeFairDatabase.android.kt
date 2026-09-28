package dev.jakubzika.befair.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import dev.jakubzika.befair.data.storage.BeFairAndroidContext

actual fun beFairDatabaseBuilder(): RoomDatabase.Builder<BeFairDatabase> {
    val context = BeFairAndroidContext.application
    return Room.databaseBuilder<BeFairDatabase>(
        context = context,
        name = context.getDatabasePath(DATABASE_FILE_NAME).absolutePath,
    )
}
