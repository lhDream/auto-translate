package io.github.lhdream.engines

import io.github.lhdream.core.TranslationEngine

import arc.Core
import com.google.gson.Gson
import arc.util.Http
import arc.util.Log
import java.io.IOException

object DeepLFreeEngine : TranslationEngine {
    override val id = "deepl-free"
    override val displayName = "DeepL Free Engine"

    private val gson = Gson()

    // DeepL 的数据结构
    private data class ApiRequest(val text: List<String>, val targetLang: String)
    private data class ApiResponse(val translations: List<Translation>)
    private data class Translation(val text: String)

    // DeepL API Key 从设置中读取
    private val apiKey: String
        get() = Core.settings.getString("deepl-api-key", "")

    override fun isConfigured(): Boolean {
        return apiKey.isNotBlank()
    }

    override fun translate(text: String, targetLang: String): String {
        if (!isConfigured()) throw IllegalStateException("DeepL API Key 未设置。")

        val apiUrl = "https://api-free.deepl.com/v2/translate"
        val requestBody = gson.toJson(ApiRequest(listOf(text), targetLang.uppercase()))
        var translatedText = ""

        Http.post(apiUrl)
            .header("Authorization", "DeepL-Auth-Key $apiKey")
            .header("Content-Type", "application/json; charset=utf-8")
            .content(requestBody)
            .block { response ->
                try {
                    val responseBody = response.resultAsString
                    if (response.status != Http.HttpStatus.OK || responseBody.isNullOrEmpty()) {
                        throw IOException("请求失败: ${response.status} - $responseBody")
                    }
                    val apiResponse = gson.fromJson(responseBody, ApiResponse::class.java)
                    translatedText = apiResponse.translations.firstOrNull()?.text
                        ?: throw IllegalStateException("API 响应中未找到翻译结果。")
                    Log.info("DeepLEngine: 翻译成功！")
                } catch (e: Exception) {
                    Log.err("DeepLEngine: 响应处理失败", e)
                }
            }

        return translatedText
    }
}
