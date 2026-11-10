package dev.jakubzika.befair.data.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

/**
 * Creates the platform [HttpClient] (engine selection is the only platform-specific part).
 * Callers pass [block] to install their own plugins (e.g. ContentNegotiation, Auth) — see
 * [configureBeFair] for the authenticated setup.
 *
 * Mobile-only, so it lives here rather than in `core`: the server is a Ktor *server* and never
 * builds a client, and putting the engines in `core` only pushed `ktor-client-cio` into the
 * server's runtime classpath.
 */
expect fun createHttpClient(block: HttpClientConfig<*>.() -> Unit = {}): HttpClient
