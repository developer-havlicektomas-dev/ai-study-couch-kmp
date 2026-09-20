package dev.havlicektomas.studycoach.core.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.URLProtocol
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {
    /** The application owns the returned client and supplied engine; reuse both until shutdown. */
    fun create(engine: HttpClientEngine, config: NetworkConfig): HttpClient = HttpClient(engine) {
        install(createClientPlugin("RequireSecureTransport") {
            onRequest { request, _ ->
                check(config.development || request.url.protocol == URLProtocol.HTTPS) {
                    "Release requests must use HTTPS"
                }
            }
        })
        expectSuccess = false
        // Avoid forwarding a learner's request to a redirect target or downgrading HTTPS.
        followRedirects = false
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 30_000
        }
        defaultRequest {
            url(config.baseUrl)
            contentType(ContentType.Application.Json)
        }
        // No body/header logging, including in debug builds.
    }
}
