package dev.havlicektomas.studycoach

import androidx.lifecycle.SavedStateHandle
import dev.havlicektomas.studycoach.core.data.NetworkConfig
import dev.havlicektomas.studycoach.tutor.domain.TutorRemoteDataSource
import dev.havlicektomas.studycoach.tutor.presentation.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class DependencyInjectionTest {
    @Test fun assembledGraphUsesOneClientAndDeliversResponseToViewModel() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val engine = MockEngine {
            respond("""{"request_id":"di-test","mode":"explain","level":"beginner","title":"Title","message":"Answer"}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val app = koinApplication {
            modules(studyCoachModules(NetworkConfig("https://test.invalid"), engine))
            modules(module { factory { SavedStateHandle() } })
        }
        try {
            assertSame(app.koin.get<HttpClient>(), app.koin.get<HttpClient>())
            assertSame(app.koin.get<TutorRemoteDataSource>(), app.koin.get<TutorRemoteDataSource>())
            val vm = app.koin.get<TutorViewModel>()
            vm.onAction(TutorAction.QuestionChanged("A question"))
            vm.onAction(TutorAction.SubmitClicked)
            withTimeout(5000) {
                while (vm.state.value.isLoading) { runCurrent(); delay(10) }
            }
            assertEquals("di-test", vm.state.value.response?.requestId)
        } finally { app.close(); Dispatchers.resetMain() }
    }
}
