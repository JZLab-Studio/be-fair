package dev.jakubzika.befair

import dev.jakubzika.befair.data.db.DatabaseConfig
import dev.jakubzika.befair.data.db.DatabaseFactory
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

/**
 * One PostgreSQL container shared by the whole test run.
 *
 * The suite talks to a real Postgres rather than an embedded stand-in so the tests exercise
 * the dialect the server actually ships against -- `upsert` compiles to `ON CONFLICT` here and
 * `MERGE` on H2, and a failed statement aborts the surrounding transaction, which is how the
 * idempotent-create path in `ItemRepository` was caught.
 *
 * Requires a running Docker daemon. Testcontainers reaps the container when the JVM exits.
 *
 * Testcontainers must stay on 2.x: 1.x asks the daemon for Docker API v1.32, and Docker
 * Engine 29 raised its minimum to v1.40 and rejects anything older with an opaque HTTP 400
 * ("Could not find a valid Docker environment"). 2.0.2 moved the default to v1.44.
 */
object TestDatabase {

    /** Pinned to the same major version the deployed database runs. */
    private const val IMAGE = "postgres:17-alpine"

    private val container by lazy {
        PostgreSQLContainer(DockerImageName.parse(IMAGE)).apply { start() }
    }

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
