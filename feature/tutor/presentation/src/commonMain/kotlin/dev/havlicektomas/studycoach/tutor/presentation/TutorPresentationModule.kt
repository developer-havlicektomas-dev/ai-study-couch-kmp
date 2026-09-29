package dev.havlicektomas.studycoach.tutor.presentation

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val tutorPresentationModule = module { viewModelOf(::TutorViewModel) }
