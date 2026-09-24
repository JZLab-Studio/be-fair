package dev.jakubzika.befair.data.db

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

/**
 * Initializes the file-based H2 database and creates the schema on startup.
 *
 * `DB_CLOSE_DELAY=-1` keeps the in-process database alive for the JVM lifetime, while
 * the `file:` URL means data survives server restarts.
 */
object DatabaseFactory {

    fun init() {
        Database.connect(
            url = "jdbc:h2:file:./build/befair-db;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
        )
        transaction {
            SchemaUtils.create(Users, Items, ItemEvents)
        }
    }
}
