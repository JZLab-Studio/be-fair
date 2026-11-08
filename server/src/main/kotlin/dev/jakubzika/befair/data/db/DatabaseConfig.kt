package dev.jakubzika.befair.data.db

/**
 * Connection settings for the PostgreSQL database.
 *
 * Defaults match the local development database so `./gradlew :server:run` works against a
 * freshly started Postgres with no environment set up. A deployed container is expected to
 * override all three values; a wrong default fails loudly on the first connection attempt
 * rather than silently writing somewhere unexpected.
 */
data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String,
    val maxPoolSize: Int = DEFAULT_MAX_POOL_SIZE,
) {
    companion object {
        private const val DEFAULT_URL = "jdbc:postgresql://localhost:5432/befair"
        private const val DEFAULT_USER = "befair"
        private const val DEFAULT_PASSWORD = "befair"

        /**
         * Hikari's own default is 10. Netty serves requests from a bounded worker pool and
         * every repository call is a short transaction, so 10 connections per instance keeps
         * the pool well under Postgres' default `max_connections` of 100 even with a few
         * replicas running.
         */
        private const val DEFAULT_MAX_POOL_SIZE = 10

        /** Reads configuration from the environment, falling back to local defaults. */
        fun fromEnv(): DatabaseConfig = DatabaseConfig(
            url = System.getenv("DATABASE_URL") ?: DEFAULT_URL,
            user = System.getenv("DATABASE_USER") ?: DEFAULT_USER,
            password = System.getenv("DATABASE_PASSWORD") ?: DEFAULT_PASSWORD,
            maxPoolSize = System.getenv("DATABASE_MAX_POOL_SIZE")?.toIntOrNull()
                ?: DEFAULT_MAX_POOL_SIZE,
        )
    }
}
