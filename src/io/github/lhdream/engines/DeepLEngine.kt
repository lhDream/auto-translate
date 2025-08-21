package io.github.lhdream.engines

import io.github.lhdream.core.TranslationEngine

import arc.Core
import com.google.gson.Gson
import arc.util.Log
import com.google.gson.annotations.SerializedName
import io.github.lhdream.expansion.HttpRequest

object DeepLEngine : TranslationEngine {
    override val id = "deepl"
    override val displayName = "DeepL Engine"

    private val gson = Gson()

    // DeepL 的数据结构
    private data class ApiRequest(val text: List<String>,@SerializedName("target_lang") val targetLang: String)
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

        val apiUrl = "https://api.deepl.com/v2/translate"
        val requestBody = gson.toJson(ApiRequest(listOf(text), targetLang))
        var translatedText = ""

        val result = HttpRequest.post(apiUrl)
            .header("Content-Type", "application/json")
            .header("Authorization", "DeepL-Auth-Key $apiKey")
            .body(requestBody)
            .withTlsPatcher()
            .execute()

        if(result.isSuccess){
            val apiResponse = gson.fromJson(result.body, ApiResponse::class.java)
            translatedText = apiResponse.translations.firstOrNull()?.text
                ?: throw IllegalStateException("API 响应中未找到翻译结果。")
            Log.info("DeepLEngine: 翻译成功！")
        }else{
            Log.info("请求失败: ${result.statusCode} - ${result.body}")
            throw result.error as Throwable
        }
        return translatedText
    }

}
