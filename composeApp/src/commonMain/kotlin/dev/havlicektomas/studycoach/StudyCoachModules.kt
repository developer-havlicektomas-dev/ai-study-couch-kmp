package dev.havlicektomas.studycoach

import dev.havlicektomas.studycoach.core.data.*
import dev.havlicektomas.studycoach.tutor.data.tutorDataModule
import dev.havlicektomas.studycoach.tutor.presentation.tutorPresentationModule
import io.ktor.client.engine.HttpClientEngine
import org.koin.dsl.onClose
import org.koin.dsl.module

fun studyCoachModules(config: NetworkConfig, engine: HttpClientEngine) = listOf(
    module {
        single { config }
        single<HttpClientEngine> { engine } onClose { it?.close() }
    }, coreDataModule, tutorDataModule, tutorPresentationModule,
)
