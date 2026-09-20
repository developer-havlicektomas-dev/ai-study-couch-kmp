package dev.havlicektomas.studycoach

import dev.havlicektomas.studycoach.core.data.HttpClientFactory
import dev.havlicektomas.studycoach.core.data.NetworkConfig
import io.ktor.client.engine.darwin.Darwin
import platform.Foundation.NSBundle
import kotlin.experimental.ExperimentalNativeApi

/** One client for the application lifetime; KMP-05 will bind this instance in Koin. */
object IosNetworking {
    @OptIn(ExperimentalNativeApi::class)
    val client by lazy {
        val baseUrl = NSBundle.mainBundle.objectForInfoDictionaryKey("StudyCoachApiBaseUrl") as? String
            ?: error("StudyCoachApiBaseUrl is missing from Info.plist")
        HttpClientFactory.create(Darwin.create(), NetworkConfig(baseUrl, kotlin.native.Platform.isDebugBinary))
    }
}
