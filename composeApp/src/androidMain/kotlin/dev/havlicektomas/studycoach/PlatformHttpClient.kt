package dev.havlicektomas.studycoach

import dev.havlicektomas.studycoach.core.data.HttpClientFactory
import dev.havlicektomas.studycoach.core.data.NetworkConfig
import io.ktor.client.engine.okhttp.OkHttp

fun createPlatformHttpClient(config: NetworkConfig) = HttpClientFactory.create(OkHttp.create(), config)
