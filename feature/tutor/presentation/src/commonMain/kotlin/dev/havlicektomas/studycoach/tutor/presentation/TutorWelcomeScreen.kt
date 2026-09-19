package dev.havlicektomas.studycoach.tutor.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.havlicektomas.studycoach.tutor.resources.Res
import dev.havlicektomas.studycoach.tutor.resources.welcome_title
import dev.havlicektomas.studycoach.tutor.resources.welcome_subtitle
import dev.havlicektomas.studycoach.tutor.resources.welcome_message
import dev.havlicektomas.studycoach.tutor.resources.welcome_status
import dev.havlicektomas.studycoach.designsystem.StudyCoachTheme

@Composable
fun TutorWelcomeScreen(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(Res.string.welcome_title), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(Res.string.welcome_subtitle), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(Res.string.welcome_message), style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(Res.string.welcome_status), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview
@Composable
private fun TutorWelcomePreview() {
    StudyCoachTheme { TutorWelcomeScreen() }
}
