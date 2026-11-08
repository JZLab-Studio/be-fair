package dev.jakubzika.befair.data.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

/**
 * Opens the pooled PostgreSQL connection and creates the schema on startup.
 *
 * Unlike the in-process H2 database this replaced, Postgres is reached over TCP, so every
 * transaction must come from a pool rather than a fresh `DriverManager` connection.
 *
 * [init] is idempotent: the first call builds the pool and every later call returns it
 * untouched. Tests boot [dev.jakubzika.befair.module] once per test case, and each boot calls
 * through here -- without the guard that would leak one connection pool per test.
 */
object DatabaseFactory {

    @Volatile
    private var database: Database? = null

    /**
     * Connects to the database described by [config] and applies the schema, or returns the
     * already-connected database when called again.
     *
     * Tests call this explicitly with a container's coordinates before booting the
     * application, which then finds the connection already in place.
     */
    fun init(config: DatabaseConfig = DatabaseConfig.fromEnv()): Database =
        database ?: synchronized(this) {
            database ?: connect(config).also { database = it }
        }

    private fun connect(config: DatabaseConfig): Database {
        val database = Database.connect(dataSource(config))
        transaction(database) {
            SchemaUtils.create(Users, Items, ItemEvents, UserSettings)
        }
        return database
    }

    private fun dataSource(config: DatabaseConfig): DataSource = HikariDataSource(
        HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = config.url
            username = config.user
            password = config.password
            maximumPoolSize = config.maxPoolSize
            // Exposed opens a transaction per repository call and commits it explicitly.
            isAutoCommit = false
            validate()
        },
    )
}
