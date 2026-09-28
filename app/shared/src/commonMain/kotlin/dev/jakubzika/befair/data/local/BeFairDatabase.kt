package dev.jakubzika.befair.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

const val DATABASE_FILE_NAME = "befair.db"

@Database(entities = [ItemEntity::class], version = 1, exportSchema = true)
@ConstructedBy(BeFairDatabaseConstructor::class)
abstract class BeFairDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}

// Room's compiler generates the actual implementation for each target.
@Suppress("KotlinNoActualForExpect")
expect object BeFairDatabaseConstructor : RoomDatabaseConstructor<BeFairDatabase> {
    override fun initialize(): BeFairDatabase
}

/** Platform-specific builder (file location); the rest of the configuration is shared. */
expect fun beFairDatabaseBuilder(): RoomDatabase.Builder<BeFairDatabase>

/** The cache is disposable (the server is the source of truth), so schema changes just reset it. */
fun createBeFairDatabase(): BeFairDatabase =
    beFairDatabaseBuilder()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
