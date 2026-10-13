package dev.jakubzika.befair.data.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database

/**
 * Opens the pooled PostgreSQL connection and migrates the schema on startup.
 *
 * Unlike the in-process H2 database this replaced, Postgres is reached over TCP, so every
 * transaction must come from a pool rather than a fresh `DriverManager` connection.
 *
 * [init] is idempotent: the first call builds the pool and every later call returns it
 * untouched. Tests boot [dev.jakubzika.befair.module] once per test case, and each boot calls
 * through here -- without the guard that would leak one connection pool per test.
 */
object DatabaseFactory {

    private const val MIGRATIONS_LOCATION = "classpath:db/migration"

    @Volatile
    private var database: Database? = null

    /**
     * Connects to the database described by [config] and migrates the schema, or returns the
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
        migrate(config)
        return Database.connect(dataSource(config))
    }

    /**
     * Applies pending migrations from `db/migration` before the request-serving pool exists.
     *
     * This replaced `SchemaUtils.create`, which only ever created *missing* tables and never
     * altered existing ones -- fine while the database was disposable, but it left no path
     * forward for the first schema change against data worth keeping.
     *
     * Flyway gets its own connection from [DatabaseConfig] rather than the Hikari pool: it
     * runs once at startup, has no use for pooling, and manages its own transactions, which
     * would otherwise have to interleave with the pool's `autoCommit = false` default.
     *
     * `baselineOnMigrate` covers databases whose tables were already created by
     * `SchemaUtils`: Flyway adopts the existing schema as the baseline instead of refusing to
     * run against a non-empty database, and skips V1 because it is already applied in effect.
     */
    private fun migrate(config: DatabaseConfig) {
        Flyway.configure()
            .dataSource(config.url, config.user, config.password)
            .locations(MIGRATIONS_LOCATION)
            .baselineOnMigrate(true)
            .baselineVersion("1")
            .load()
            .migrate()
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
