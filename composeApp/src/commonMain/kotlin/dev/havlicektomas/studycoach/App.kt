package dev.havlicektomas.studycoach

import androidx.compose.runtime.Composable
import dev.havlicektomas.studycoach.designsystem.StudyCoachTheme
import dev.havlicektomas.studycoach.tutor.presentation.TutorWelcomeScreen

@Composable
fun App() {
    StudyCoachTheme { TutorWelcomeScreen() }
}
