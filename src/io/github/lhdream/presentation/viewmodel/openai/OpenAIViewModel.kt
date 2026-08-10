package io.github.lhdream.presentation.viewmodel.openai

import io.github.lhdream.data.model.openai.OpenAIConfig
import io.github.lhdream.data.remote.openai.OpenAIRepository
import java.util.concurrent.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class OpenAIUiState {
    object Idle : OpenAIUiState()
    object Loading : OpenAIUiState()
    data class Success(val text: String) : OpenAIUiState()
    data class Error(val message: String, val cause: Throwable? = null) : OpenAIUiState()
}

class OpenAIViewModel(
    private val repository: OpenAIRepository,
    private val configProvider: () -> OpenAIConfig
) {
    private val _state = MutableStateFlow<OpenAIUiState>(OpenAIUiState.Idle)
    val state: StateFlow<OpenAIUiState> = _state.asStateFlow()

    suspend fun translate(text: String, targetLanguage: String): String {
        _state.value = OpenAIUiState.Loading

        return try {
            val translated = repository.translate(configProvider(), text, targetLanguage)
            _state.value = OpenAIUiState.Success(translated)
            translated
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val message = e.message?.takeIf { it.isNotBlank() } ?: e.javaClass.simpleName
            _state.value = OpenAIUiState.Error(message, e)
            throw e
        }
    }
}
