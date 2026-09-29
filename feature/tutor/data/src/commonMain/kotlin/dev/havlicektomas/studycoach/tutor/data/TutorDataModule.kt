package dev.havlicektomas.studycoach.tutor.data

import dev.havlicektomas.studycoach.tutor.domain.TutorRemoteDataSource
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val tutorDataModule = module {
    singleOf(::KtorTutorRemoteDataSource) bind TutorRemoteDataSource::class
}
