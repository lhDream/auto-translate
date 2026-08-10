package io.github.lhdream.data.remote.translation

import arc.Core
import arc.util.Log
import io.github.lhdream.data.model.openai.OpenAIConfig
import io.github.lhdream.data.remote.openai.OpenAIRepository
import io.github.lhdream.domain.translation.TranslationEngine
import io.github.lhdream.presentation.viewmodel.openai.OpenAIViewModel
import java.util.concurrent.CancellationException
import kotlinx.coroutines.runBlocking

object OpenAIEngine : TranslationEngine {

    override val id: String = "openai"
    override val displayName: String = "OpenAI Translate"

    private const val DEFAULT_BASE_URL = "https://api.openai.com/v1/"
    private const val DEFAULT_MODEL = "gpt-4o-mini"

    private val viewModel = OpenAIViewModel(
        repository = OpenAIRepository(),
        configProvider = ::currentConfig
    )

    val state get() = viewModel.state

    private val apiKey: String
        get() = Core.settings.getString("openai-api-key", "")

    private val baseUrl: String
        get() = Core.settings.getString("openai-base-url", DEFAULT_BASE_URL)

    private val modelName: String
        get() = Core.settings.getString("openai-model-name", DEFAULT_MODEL)

    override fun isConfigured(): Boolean {
        return apiKey.isNotBlank()
    }

    override fun translate(text: String, targetLang: String): String = runBlocking {
        translateAsync(text, targetLang)
    }

    suspend fun translateAsync(text: String, targetLang: String): String {
        return try {
            val translatedText = viewModel.translate(text, targetLang)
            Log.info("OpenAIEngine: 翻译成功！")
            translatedText
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.err("OpenAIEngine: 翻译失败", e)
            throw e as? IllegalStateException ?: IllegalStateException("OpenAI 翻译错误: ${e.message}", e)
        }
    }

    private fun currentConfig() = OpenAIConfig(
        apiKey = apiKey,
        baseUrl = baseUrl,
        modelName = modelName
    )
}
