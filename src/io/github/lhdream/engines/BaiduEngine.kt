package io.github.lhdream.engines

import arc.Core
import arc.util.Log
import com.google.gson.Gson
import io.github.lhdream.core.TranslationEngine
import io.github.lhdream.expansion.HttpRequest
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*

object BaiduEngine : TranslationEngine {

    override val id = "baidu"
    override val displayName = "Baidu Translate"

    private val gson = Gson()

    private data class BaiduResponse(
        val from: String,
        val to: String,
        val trans_result: List<TranslationResult>?,
        val error_code: String? = null,
        val src_tts: String? = null,
        val dst_tts: String? = null,
        val dict: String? = null
    )

    private data class TranslationResult(
        val src: String,
        val dst: String
    )

    private val appId: String
        get() = Core.settings.getString("baidu-app-id", "")
    
    private val apiKey: String
        get() = Core.settings.getString("baidu-api-key", "")

    override fun isConfigured(): Boolean {
        return appId.isNotBlank() && apiKey.isNotBlank()
    }

    override fun translate(text: String, targetLang: String): String {
        if (!isConfigured()) {
            throw IllegalStateException("百度翻译 API 配置不完整，请设置 APP ID 和 API Key。")
        }

        val salt = Random().nextInt(10000).toString()
        val sign = generateMd5(appId + text + salt + apiKey)

        // 构建POST请求体
        val formData = StringBuilder()
        formData.append("q=").append(URLEncoder.encode(text, StandardCharsets.UTF_8))
            .append("&from=auto")
            .append("&to=").append(targetLang)
            .append("&appid=").append(appId)
            .append("&salt=").append(salt)
            .append("&sign=").append(sign)

        val apiUrl = "https://api.fanyi.baidu.com/api/trans/vip/translate"
        
        val result = HttpRequest.post(apiUrl)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(formData.toString())
            .execute()

        if (result.isSuccess) {
            val responseBody = result.body ?: throw IllegalStateException("响应体为空")
            
            val baiduResponse = gson.fromJson(responseBody, BaiduResponse::class.java)
            
            baiduResponse.error_code?.let { errorCode ->
                throw IllegalStateException("百度翻译API错误: $errorCode")
            }

            val translatedText = baiduResponse.trans_result?.firstOrNull()?.dst
                ?: throw IllegalStateException("API 响应中未找到翻译结果。")
            
            Log.info("BaiduEngine: 翻译成功！")
            return translatedText
        } else {
            Log.err("BaiduEngine: 请求失败 - ${result.statusCode} - ${result.body}")
            throw result.error ?: IOException("请求失败: ${result.statusCode}")
        }
    }

    private fun generateMd5(input: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}