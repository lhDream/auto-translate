package io.github.lhdream.engines

import arc.Core
import arc.util.Log
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.openai.OpenAiChatModel
import io.github.lhdream.core.TranslationEngine

object OpenAIEngine : TranslationEngine {

    override val id: String = "openai"
    override val displayName: String = "OpenAI Translate"

    private const val DEFAULT_BASE_URL = "https://api.openai.com/v1/"
    private const val DEFAULT_MODEL = "gpt-4o-mini"

    private val apiKey: String
        get() = Core.settings.getString("openai-api-key", "")

    private val baseUrl: String
        get() = Core.settings.getString("openai-base-url", DEFAULT_BASE_URL)

    private val modelName: String
        get() = Core.settings.getString("openai-model-name", DEFAULT_MODEL)

    @Volatile
    private var cachedModel: OpenAiChatModel? = null
    @Volatile
    private var cachedConfigFingerprint: String = ""

    override fun isConfigured(): Boolean {
        return apiKey.isNotBlank()
    }

    @Synchronized
    private fun getOrCreateModel(): OpenAiChatModel {
        val fingerprint = "${apiKey}||${baseUrl}||${modelName}"
        if (cachedModel != null && cachedConfigFingerprint == fingerprint) {
            return cachedModel!!
        }
        cachedModel = OpenAiChatModel.builder()
            .apiKey(apiKey)
            .baseUrl(baseUrl)
            .modelName(modelName)
            .build()
        cachedConfigFingerprint = fingerprint
        return cachedModel!!
    }

    override fun translate(text: String, targetLang: String): String {
        if (!isConfigured()) {
            throw IllegalStateException("OpenAI API Key 未设置")
        }

        try {
            val model = getOrCreateModel()
            val response = model.chat(
                SystemMessage.from(
                    "You are a translation engine. Translate the following text to $targetLang. " +
                    "Output ONLY the translated text, nothing else."
                ),
                UserMessage.from(text)
            )
            val translatedText = response.aiMessage().text()
                ?: throw IllegalStateException("OpenAI 返回了空响应")
            Log.info("OpenAIEngine: 翻译成功！")
            return translatedText
        } catch (e: IllegalStateException) {
            throw e
        } catch (e: Exception) {
            Log.err("OpenAIEngine: 翻译失败", e)
            throw IllegalStateException("OpenAI 翻译错误: ${e.message}", e)
        }
    }
}