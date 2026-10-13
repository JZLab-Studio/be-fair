package dev.jakubzika.befair

import dev.jakubzika.befair.data.db.DatabaseConfig
import dev.jakubzika.befair.data.db.DatabaseFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

/**
 * Self-typed subclass so Kotlin can name the container type. [PostgreSQLContainer] is
 * declared as `PostgreSQLContainer<SELF extends PostgreSQLContainer<SELF>>`, which has no
 * usable Kotlin spelling without binding `SELF` like this.
 */
private class BeFairPostgresContainer :
    PostgreSQLContainer<BeFairPostgresContainer>(DockerImageName.parse(IMAGE)) {
    companion object {
        /** Pinned to the same major version the deployed database runs. */
        const val IMAGE = "postgres:17-alpine"
    }
}

/**
 * One PostgreSQL container shared by the whole test run.
 *
 * The suite talks to a real Postgres rather than an embedded stand-in so the tests exercise
 * the dialect the server actually ships against -- `upsert` compiles to `ON CONFLICT` here and
 * `MERGE` on H2, and constraint violations surface different SQL states.
 *
 * Requires a running Docker daemon. Testcontainers reaps the container when the JVM exits.
 */
object TestDatabase {

    private val container by lazy { BeFairPostgresContainer().apply { start() } }

    /**
     * Points [DatabaseFactory] at the shared container, starting it on first use.
     *
     * Safe to call from every test: the container starts once per JVM and
     * [DatabaseFactory.init] ignores repeat calls, so the later `module()` boot inside
     * `testApplication` reuses this connection instead of reading the environment.
     *
     * Tests share one database and isolate themselves with unique emails and generated ids,
     * matching how the suite already behaved against the single H2 file.
     */
    fun ensureReady() {
        DatabaseFactory.init(
            DatabaseConfig(
                url = container.jdbcUrl,
                user = container.username,
                password = container.password,
            ),
        )
    }
}
