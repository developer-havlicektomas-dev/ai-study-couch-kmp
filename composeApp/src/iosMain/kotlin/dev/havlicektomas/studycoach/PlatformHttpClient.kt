package dev.havlicektomas.studycoach

import dev.havlicektomas.studycoach.core.data.NetworkConfig
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.context.startKoin
import platform.Foundation.NSBundle
import kotlin.experimental.ExperimentalNativeApi

internal object IosDependencyInjection {
    @OptIn(ExperimentalNativeApi::class)
    private val application by lazy {
        val baseUrl = NSBundle.mainBundle.objectForInfoDictionaryKey("StudyCoachApiBaseUrl") as? String
            ?: error("StudyCoachApiBaseUrl is missing from Info.plist")
        startKoin { modules(studyCoachModules(NetworkConfig(baseUrl, kotlin.native.Platform.isDebugBinary), Darwin.create())) }
    }
    fun initialize() { application }
}
