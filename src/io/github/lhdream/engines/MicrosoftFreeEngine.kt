package io.github.lhdream.engines

import arc.util.Http
import arc.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.lhdream.core.TranslationEngine
import java.io.IOException

object MicrosoftFreeEngine : TranslationEngine {

    override val id: String = "microsoft-free"
    override val displayName: String = "Microsoft Free Engine"

    private val gson = Gson()

    // 认证令牌
    private var authToken: String? = null
    // 令牌获取时间戳
    private var tokenTimestamp: Long = 0L
    // 缓存持续时间（5分钟，单位：毫秒）
    private const val CACHE_DURATION_MS = 5 * 60 * 1000L

    // 请求体的数据类
    private data class RequestBody(val Text: String)
    // 响应体的数据类
    private data class TranslationResponse(val translations: List<TranslationResult>)
    private data class TranslationResult(val text: String, val to: String)

    /**
     * 检查并确保认证令牌已获取。
     * 使用 synchronized 块确保在多线程环境下只执行一次网络请求。
     */
    override fun isConfigured(): Boolean {
        // 检查当前令牌是否仍然有效
        val isTokenValid = authToken != null && (System.currentTimeMillis() - tokenTimestamp < CACHE_DURATION_MS)
        // 如果令牌有效，则无需任何操作
        if (isTokenValid) {
            return true
        }
        try {
            Log.info("MicrosoftFreeEngine: 正在获取认证令牌...")
            Http.get("https://edge.microsoft.com/translate/auth")
                .header("Content-Type", "application/json; charset=utf-8")
                .block { response ->
                    val responseBody = response.resultAsString
                    if (response.status == Http.HttpStatus.OK && !responseBody.isNullOrEmpty()) {
                        authToken = responseBody
                        tokenTimestamp = System.currentTimeMillis()
                        Log.info("MicrosoftFreeEngine: 认证令牌获取成功！")
                    } else {
                        tokenTimestamp = 0L
                        throw IOException("获取微软翻译认证令牌失败: ${response.status} - $responseBody")
                    }
                }
        } catch (e: Exception) {
            Log.err("MicrosoftFreeEngine: 获取认证令牌时发生网络或IO错误", e)
            return false
        }
        return authToken != null
    }

    override fun translate(text: String, targetLang: String): String {
        // 如果配置失败（例如网络问题），直接返回空字符串
        if (!isConfigured()) {
            Log.err("MicrosoftFreeEngine: 引擎未配置或认证失败，翻译中止。")
            return ""
        }

        val requestPayload = listOf(RequestBody(text))
        val requestBodyJson = gson.toJson(requestPayload)

        var translatedText = ""
        val url = "https://api-edge.cognitive.microsofttranslator.com/translate?to=${targetLang}&api-version=3.0&includeSentenceLength=true"

        try {
            withTls12 {
                Http.post(url)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Authorization", "Bearer $authToken")
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
                            Log.info("MicrosoftFreeEngine: 翻译成功！")
                        } else {
                            Log.warn("MicrosoftFreeEngine: API 响应中未找到翻译结果。响应体: $responseBody")
                        }
                    }
            }
        } catch (e: Exception) {
            Log.err("MicrosoftFreeEngine: 翻译过程中发生错误", e)
            // 出现异常时也返回空字符串
            return ""
        }
        return translatedText
    }

    private fun <T> withTls12(block: () -> T): T {
        val originalProtocols = System.getProperty("https.protocols")
        try {
            System.setProperty("https.protocols", "TLSv1.2,TLSv1.3")
            return block()
        } finally {
            if (originalProtocols == null) {
                System.clearProperty("https.protocols")
            } else {
                System.setProperty("https.protocols", originalProtocols)
            }
        }
    }
}
