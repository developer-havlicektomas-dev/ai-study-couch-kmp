package dev.havlicektomas.studycoach.core.data

import org.koin.dsl.onClose
import org.koin.dsl.module

val coreDataModule = module {
    single { HttpClientFactory.create(get(), get()) } onClose { it?.close() }
}
