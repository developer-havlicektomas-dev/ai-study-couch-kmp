package dev.havlicektomas.studycoach.tutor.presentation

import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
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

/** Minimal request surface; mode controls and full response layouts follow in KMP-06–09. */
@Composable
fun TutorScreen(state: TutorState, onAction: (TutorAction) -> Unit) {
    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(Res.string.welcome_title), style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(state.question, { onAction(TutorAction.QuestionChanged(it)) },
            label = { Text(stringResource(Res.string.question_label)) },
            isError = state.validationError, modifier = Modifier.fillMaxWidth())
        if (state.validationError) Text(stringResource(Res.string.invalid_question))
        Button(onClick = { onAction(TutorAction.SubmitClicked) }, enabled = state.canSubmit) {
            Text(stringResource(if (state.isLoading) Res.string.preparing_answer else Res.string.ask_tutor))
        }
        if (state.isLoading) CircularProgressIndicator()
        if (state.error != null) {
            Text(stringResource(Res.string.request_failed))
            TextButton(onClick = { onAction(TutorAction.RetryClicked) }, enabled = !state.isLoading) {
                Text(stringResource(Res.string.retry))
            }
        }
        state.response?.let {
            if (it.isMock) Text(stringResource(Res.string.mock_response))
            Text(it.title, style = MaterialTheme.typography.titleLarge)
            Text(it.message)
        }
    }
}
