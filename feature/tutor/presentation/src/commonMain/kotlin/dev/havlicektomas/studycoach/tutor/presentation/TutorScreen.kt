package dev.havlicektomas.studycoach.tutor.presentation

import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.semantics.*
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import dev.havlicektomas.studycoach.designsystem.StudyCoachTheme
import dev.havlicektomas.studycoach.tutor.domain.TutorMode
import dev.havlicektomas.studycoach.tutor.domain.LearnerLevel
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import dev.havlicektomas.studycoach.tutor.resources.*
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TutorRoot(viewModel: TutorViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    is TutorEvent.RequestFailed -> snackbar.showSnackbar(getString(Res.string.request_failed))
                }
            }
        }
    }
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Box(Modifier.padding(padding)) { TutorScreen(state, viewModel::onAction) }
    }
}

/** The form renders state only; validation and submission decisions remain outside Compose. */
@Composable
fun TutorScreen(state: TutorState, onAction: (TutorAction) -> Unit) {
    val keyboard = LocalSoftwareKeyboardController.current
    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(stringResource(Res.string.welcome_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(Res.string.form_description), style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = state.question,
            onValueChange = { onAction(TutorAction.QuestionChanged(it)) },
            label = { Text(stringResource(Res.string.question_label)) },
            minLines = 3,
            maxLines = 8,
            isError = state.showQuestionError,
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (state.showQuestionError) {
                        Text(stringResource(if (state.questionLength < 3) Res.string.question_too_short else Res.string.question_too_long),
                            Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                    } else {
                        Text(stringResource(Res.string.question_guidance))
                    }
                    if (state.showCharacterCount) {
                        Text(stringResource(Res.string.question_count, state.questionLength, 2000))
                    }
                }
            },
        )
        ChoiceGroup(stringResource(Res.string.response_mode)) {
            Choice(stringResource(Res.string.mode_explain), state.mode == TutorMode.EXPLAIN) { onAction(TutorAction.ModeSelected(TutorMode.EXPLAIN)) }
            Choice(stringResource(Res.string.mode_hint), state.mode == TutorMode.HINT) { onAction(TutorAction.ModeSelected(TutorMode.HINT)) }
            Choice(stringResource(Res.string.mode_quiz), state.mode == TutorMode.QUIZ) { onAction(TutorAction.ModeSelected(TutorMode.QUIZ)) }
        }
        ChoiceGroup(stringResource(Res.string.knowledge_level)) {
            Choice(stringResource(Res.string.level_beginner), state.level == LearnerLevel.BEGINNER) { onAction(TutorAction.LevelSelected(LearnerLevel.BEGINNER)) }
            Choice(stringResource(Res.string.level_intermediate), state.level == LearnerLevel.INTERMEDIATE) { onAction(TutorAction.LevelSelected(LearnerLevel.INTERMEDIATE)) }
            Choice(stringResource(Res.string.level_advanced), state.level == LearnerLevel.ADVANCED) { onAction(TutorAction.LevelSelected(LearnerLevel.ADVANCED)) }
        }
        Button(
            onClick = { keyboard?.hide(); onAction(TutorAction.SubmitClicked) },
            enabled = state.canSubmit,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(Res.string.ask_tutor)) }
        if (state.isLoading) {
            Row(Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(24.dp))
                Text(stringResource(Res.string.preparing_answer), style = MaterialTheme.typography.bodyLarge)
            }
        }
        if (state.error != null) {
            Text(stringResource(Res.string.request_failed))
            TextButton(onClick = { onAction(TutorAction.RetryClicked) }, enabled = state.canSubmit) {
                Text(stringResource(Res.string.retry))
            }
        }
        state.response?.let {
            HorizontalDivider()
            if (it.isMock) Text(stringResource(Res.string.mock_response))
            Text(it.title, style = MaterialTheme.typography.titleLarge)
            Text(it.message)
        }
    }
}

@Composable
private fun ChoiceGroup(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleSmall)
        FlowRow(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    }
}

@Composable
private fun Choice(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.heightIn(min = 48.dp).selectable(selected, role = Role.RadioButton, onClick = onClick).padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected, onClick = null)
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview
@Composable
private fun DefaultFormPreview() { StudyCoachTheme { TutorScreen(TutorState(), {}) } }

@Preview
@Composable
private fun LoadingFormPreview() { StudyCoachTheme { TutorScreen(TutorState(question = "Why is the sky blue?", isLoading = true), {}) } }

@Preview
@Composable
private fun InvalidFormPreview() { StudyCoachTheme { TutorScreen(TutorState(question = "Hi", validationError = true), {}) } }

@Preview
@Composable
private fun NearLimitFormPreview() { StudyCoachTheme { TutorScreen(TutorState(question = "A".repeat(1900)), {}) } }
