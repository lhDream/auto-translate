package io.github.lhdream.engines

import arc.Core
import arc.util.Http
import arc.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.lhdream.core.TranslationEngine
import io.github.lhdream.expansion.withTls12And13

object AzureEngine : TranslationEngine {

    override val id: String = "azure"
    override val displayName: String = "Azure Engine"

    private val gson = Gson()
    // 请求体的数据类
    private data class RequestBody(val Text: String)
    // 响应体的数据类
    private data class TranslationResponse(val translations: List<TranslationResult>)
    private data class TranslationResult(val text: String, val to: String)

    private val apiKey: String
        get() = Core.settings.getString("azure-api-key", "")

    override fun isConfigured(): Boolean {
        return apiKey.isNotBlank()
    }

    override fun translate(text: String, targetLang: String): String {
        // 如果配置失败（例如网络问题），直接返回空字符串
        if (!isConfigured()) {
            Log.err("AzureEngine: 引擎未配置或认证失败，翻译中止。")
            return ""
        }

        val requestPayload = listOf(RequestBody(text))
        val requestBodyJson = gson.toJson(requestPayload)

        var translatedText = ""
        val url = "https://api.cognitive.microsofttranslator.com/translate?to=${targetLang}&api-version=3.0&includeSentenceLength=true"

        try {
            withTls12And13 {
                Http.post(url)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Ocp-Apim-Subscription-Key", apiKey)
                    .content(requestBodyJson)
                    .block { response ->
                        val responseBody = response.resultAsString

                        if (response.status != Http.HttpStatus.OK || responseBody.isNullOrEmpty()) {
                            Log.info("翻译请求失败: ${response.status} - $responseBody")
                            return@block
                        }

                        val listType = object : TypeToken<List<TranslationResponse>>() {}.type
                        val apiResponse: List<TranslationResponse> = gson.fromJson(responseBody, listType)

                        // 提取翻译结果
                        translatedText = apiResponse.firstOrNull()
                            ?.translations?.firstOrNull()
                            ?.text
                            ?: "" // 如果找不到结果，返回空字符串

                        if (translatedText.isNotEmpty()) {
                            Log.info("AzureEngine: 翻译成功！")
                        } else {
                            Log.warn("AzureEngine: API 响应中未找到翻译结果。响应体: $responseBody")
                        }
                    }
            }
        } catch (e: Exception) {
            Log.err("AzureEngine: 翻译过程中发生错误", e)
            // 出现异常时也返回空字符串
            return ""
        }
        return translatedText
    }
}
