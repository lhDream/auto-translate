package io.github.lhdream.data.remote.openai

import com.google.gson.Gson
import io.github.lhdream.data.model.openai.OpenAIChatRequest
import io.github.lhdream.data.model.openai.OpenAIChatResponse
import io.github.lhdream.data.model.openai.OpenAIConfig
import io.github.lhdream.data.model.openai.OpenAIErrorResponse
import io.github.lhdream.data.model.openai.OpenAIRequestMessage
import io.github.lhdream.infrastructure.network.HttpRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface OpenAITransport {
    fun post(url: String, headers: Map<String, String>, body: String): HttpRequest.Result
}

class HttpUrlConnectionOpenAITransport : OpenAITransport {
    override fun post(url: String, headers: Map<String, String>, body: String): HttpRequest.Result {
        val request = HttpRequest.post(url)
            .withTlsPatcher()
            .body(body)

        headers.forEach { (key, value) -> request.header(key, value) }
        return request.execute()
    }
}

class OpenAIRepository(
    private val transport: OpenAITransport = HttpUrlConnectionOpenAITransport(),
    private val gson: Gson = Gson(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun translate(config: OpenAIConfig, text: String, targetLanguage: String): String =
        withContext(ioDispatcher) {
            translateBlocking(config, text, targetLanguage)
        }

    private fun translateBlocking(config: OpenAIConfig, text: String, targetLanguage: String): String {
        require(config.apiKey.isNotBlank()) { "OpenAI API Key 未设置" }
        require(config.baseUrl.isNotBlank()) { "OpenAI Base URL 未设置" }
        require(config.modelName.isNotBlank()) { "OpenAI 模型未设置" }

        val request = OpenAIChatRequest(
            model = config.modelName,
            messages = listOf(
                OpenAIRequestMessage(
                    role = "system",
                    content = "You are a translation engine. Translate the following text to $targetLanguage. " +
                        "Output ONLY the translated text, nothing else."
                ),
                OpenAIRequestMessage(role = "user", content = text)
            )
        )

        val result = transport.post(
            url = endpointUrl(config.baseUrl),
            headers = mapOf(
                "Authorization" to "Bearer ${config.apiKey}",
                "Content-Type" to "application/json"
            ),
            body = gson.toJson(request)
        )

        if (!result.isSuccess) {
            throw IllegalStateException(errorMessage(result))
        }

        val response = try {
            gson.fromJson(result.body, OpenAIChatResponse::class.java)
        } catch (e: Exception) {
            throw IllegalStateException("OpenAI 响应不是有效 JSON", e)
        }

        return response?.choices
            ?.firstOrNull()
            ?.message
            ?.content
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: throw IllegalStateException("OpenAI 响应中未找到翻译结果")
    }

    private fun endpointUrl(baseUrl: String): String {
        val normalized = baseUrl.trim().trimEnd('/')
        return if (normalized.endsWith("/chat/completions")) {
            normalized
        } else {
            "$normalized/chat/completions"
        }
    }

    private fun errorMessage(result: HttpRequest.Result): String {
        val error = try {
            gson.fromJson(result.body, OpenAIErrorResponse::class.java)?.error
        } catch (_: Exception) {
            null
        }

        val message = error?.message?.takeIf { it.isNotBlank() }
        if (message != null) return "OpenAI 请求失败: $message"

        val transportMessage = result.error?.message?.takeIf { it.isNotBlank() }
        if (transportMessage != null) return "OpenAI 请求失败: $transportMessage"

        return "OpenAI 请求失败，HTTP 状态码: ${result.statusCode}"
    }
}
