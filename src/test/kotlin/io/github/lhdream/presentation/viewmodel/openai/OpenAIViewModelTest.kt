package io.github.lhdream.presentation.viewmodel.openai

import io.github.lhdream.data.model.openai.OpenAIConfig
import io.github.lhdream.data.remote.openai.OpenAIRepository
import io.github.lhdream.data.remote.openai.OpenAITransport
import io.github.lhdream.infrastructure.network.HttpRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class OpenAIViewModelTest {

    private val config = OpenAIConfig("key", "https://example.test/v1", "model")

    @Test
    fun exposesSuccessStateAfterTranslation() = runBlocking {
        val repository = OpenAIRepository(
            transport = StaticTransport(HttpRequest.Result(200, """{"choices":[{"message":{"content":"ok"}}]}""")),
            ioDispatcher = Dispatchers.Unconfined
        )
        val viewModel = OpenAIViewModel(repository) { config }

        assertTrue(viewModel.state.value is OpenAIUiState.Idle)
        assertEquals("ok", viewModel.translate("hello", "en"))
        assertEquals(OpenAIUiState.Success("ok"), viewModel.state.value)
    }

    @Test
    fun exposesErrorStateWhenTranslationFails() = runBlocking {
        val repository = OpenAIRepository(
            transport = StaticTransport(HttpRequest.Result(500, "server error")),
            ioDispatcher = Dispatchers.Unconfined
        )
        val viewModel = OpenAIViewModel(repository) { config }

        try {
            viewModel.translate("hello", "en")
        } catch (_: IllegalStateException) {
            // The state is the observable error channel; the exception remains available to callers.
        }

        val state = viewModel.state.value
        assertTrue(state is OpenAIUiState.Error)
        assertTrue(state.message.contains("500"))
    }

    private class StaticTransport(private val response: HttpRequest.Result) : OpenAITransport {
        override fun post(url: String, headers: Map<String, String>, body: String): HttpRequest.Result = response
    }
}
