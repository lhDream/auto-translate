package io.github.lhdream.engines

import io.github.lhdream.core.TranslationEngine

import arc.Core
import arc.util.Http
import arc.util.Log
import com.google.gson.Gson
import java.io.IOException

object GoogleEngine : TranslationEngine {
    override val id = "google"
    override val displayName = "Google Translate"

    private val gson = Gson()

    // 内部数据类
    private data class ApiRequest(val q: String, val target: String, val format: String = "text")
    private data class ApiResponse(val data: Data)
    private data class Data(val translations: List<Translation>)
    private data class Translation(val translatedText: String)

    private val apiKey: String
        get() = Core.settings.getString("google-api-key", "")

    override fun isConfigured(): Boolean {
        return apiKey.isNotBlank()
    }

    override fun translate(text: String, targetLang: String): String {
        if (!isConfigured()) throw IllegalStateException("Google API Key 未设置。")

        val requestBody = gson.toJson(ApiRequest(q = text, target = targetLang))
        var translatedText = ""

        Http.post("https://translation.googleapis.com/language/translate/v2?key=$apiKey")
            .header("Content-Type", "application/json; charset=utf-8")
            .content(requestBody)
            .block { response ->
                try {
                    val responseBody = response.resultAsString
                    if (response.status != Http.HttpStatus.OK || responseBody.isNullOrEmpty()) {
                        throw IOException("请求失败: ${response.status} - $responseBody")
                    }
                    val apiResponse = gson.fromJson(responseBody, ApiResponse::class.java)
                    translatedText = apiResponse.data.translations.firstOrNull()?.translatedText
                        ?: throw IllegalStateException("API 响应中未找到翻译结果。")
                    Log.info("GoogleEngine: 翻译成功！")
                } catch (e: Exception) {
                    Log.err("GoogleEngine: 响应处理失败", e)
                }
            }

        return translatedText
    }
}
