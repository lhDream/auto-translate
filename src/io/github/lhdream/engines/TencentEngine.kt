package io.github.lhdream.engines

import arc.Core
import arc.util.Log
import com.google.gson.Gson
import io.github.lhdream.core.TranslationEngine
import io.github.lhdream.expansion.HttpRequest
import java.io.IOException
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object TencentEngine : TranslationEngine {

    override val id = "tencent"
    override val displayName = "Tencent Translate"

    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd").apply { timeZone = TimeZone.getTimeZone("UTC") }

    private data class TencentRequest(
        val SourceText: String,
        val Source: String,
        val Target: String,
        val ProjectId: Int = 0,
        val UntranslatedText: String? = null
    )

    private data class TencentResponse(
        val Response: TencentResult
    )

    private data class TencentResult(
        val TargetText: String,
        val Source: String,
        val Target: String,
        val RequestId: String
    )

    private data class TencentError(
        val Response: TencentErrorResult
    )

    private data class TencentErrorResult(
        val Error: TencentErrorInfo,
        val RequestId: String
    )

    private data class TencentErrorInfo(
        val Code: String,
        val Message: String
    )

    private val secretId: String
        get() = Core.settings.getString("tencent-secret-id", "")

    private val secretKey: String
        get() = Core.settings.getString("tencent-secret-key", "")

    private val region: String
        get() = Core.settings.getString("tencent-region", "ap-guangzhou")

    override fun isConfigured(): Boolean {
        return secretId.isNotBlank() && secretKey.isNotBlank()
    }

    override fun translate(text: String, targetLang: String): String {
        if (!isConfigured()) {
            throw IllegalStateException("腾讯翻译 API 配置不完整，请设置 SecretId 和 SecretKey。")
        }

        // 腾讯翻译API单次请求文本长度限制为2000字符
        if (text.length > 2000) {
            throw IllegalArgumentException("腾讯翻译API单次请求文本长度不能超过2000字符。当前长度：${text.length}")
        }

        try {
            val requestPayload = TencentRequest(
                SourceText = text,
                Source = "auto", // 自动识别源语言
                Target = targetLang,
                ProjectId = 0 // 默认项目ID
            )

            val requestBodyJson = gson.toJson(requestPayload)
            val timestamp = (System.currentTimeMillis() / 1000).toString()

            // 构建HTTP请求
            val apiUrl = "https://tmt.tencentcloudapi.com/"

            // 计算签名
            val authorization = calculateAuthorization(
                method = "POST",
                uri = "/",
                queryString = "",
                headers = mapOf(
                    "content-type" to "application/json; charset=utf-8",
                    "host" to "tmt.tencentcloudapi.com",
                    "x-tc-action" to "TextTranslate",
                    "x-tc-timestamp" to timestamp,
                    "x-tc-version" to "2018-03-21",
                    "x-tc-region" to region
                ),
                payload = requestBodyJson,
                timestamp = timestamp
            )

            val result = HttpRequest.post(apiUrl)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Host", "tmt.tencentcloudapi.com")
                .header("X-TC-Action", "TextTranslate")
                .header("X-TC-Version", "2018-03-21")
                .header("X-TC-Region", region)
                .header("X-TC-Timestamp", timestamp)
                .header("Authorization", authorization)
                .body(requestBodyJson)
                .execute()

            if (result.isSuccess) {
                val responseBody = result.body ?: throw IllegalStateException("响应体为空")

                // 尝试解析为成功响应
                try {
                    val tencentResponse = gson.fromJson(responseBody, TencentResponse::class.java)
                    Log.info("TencentEngine: 翻译成功！RequestID: ${tencentResponse.Response.RequestId}")
                    return tencentResponse.Response.TargetText
                } catch (e: Exception) {
                    // 尝试解析为错误响应
                    try {
                        val errorResponse = gson.fromJson(responseBody, TencentError::class.java)
                        val errorInfo = errorResponse.Response.Error
                        val errorMsg = mapTencentError(errorInfo.Code)
                        throw IllegalStateException("腾讯翻译API错误: ${errorInfo.Code} - $errorMsg (${errorInfo.Message})")
                    } catch (parseException: Exception) {
                        Log.err("TencentEngine: 响应解析失败 - $responseBody")
                        throw IllegalStateException("腾讯翻译API响应格式异常: ${parseException.message}")
                    }
                }
            } else {
                Log.err("TencentEngine: 请求失败 - ${result.statusCode} - ${result.body}")
                throw result.error ?: IOException("请求失败: ${result.statusCode}")
            }
        } catch (e: IllegalStateException) {
            throw e
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            Log.err("TencentEngine: 翻译过程中发生错误", e)
            throw IllegalStateException("腾讯翻译过程中发生错误: ${e.message}")
        }
    }

    private fun calculateAuthorization(
        method: String,
        uri: String,
        queryString: String,
        headers: Map<String, String>,
        payload: String,
        timestamp: String
    ): String {
        val service = "tmt"
        val algorithm = "TC3-HMAC-SHA256"

        // 1. 拼接规范请求串
        val canonicalHeaders = headers.entries
            .sortedBy { it.key.lowercase() }
            .joinToString("\n") { "${it.key.lowercase()}=${it.value.trim()}\\n" }

        val signedHeaders = headers.keys
            .sortedBy { it.lowercase() }
            .joinToString(";") { it.lowercase() }

        val hashedPayload = sha256Hex(payload)

        val canonicalRequest = StringBuilder()
            .append(method).append("\n")
            .append(uri).append("\n")
            .append(queryString).append("\n")
            .append(canonicalHeaders).append("\n")
            .append(signedHeaders).append("\n")
            .append(hashedPayload)
            .toString()

        // 2. 拼接待签名字符串
        val requestDate = dateFormat.format(Date(timestamp.toLong() * 1000))
        val credentialScope = "$requestDate/$service/tc3_request"
        val hashedCanonicalRequest = sha256Hex(canonicalRequest)

        val stringToSign = StringBuilder()
            .append(algorithm).append("\n")
            .append(timestamp).append("\n")
            .append(credentialScope).append("\n")
            .append(hashedCanonicalRequest)
            .toString()

        // 3. 计算签名
        val secretDate = hmacSha256(requestDate, secretKey.toByteArray())
        val secretService = hmacSha256(service, secretDate)
        val secretSigning = hmacSha256("tc3_request", secretService)
        val signature = hmacSha256Hex(stringToSign, secretSigning)

        // 4. 拼接 Authorization
        return "$algorithm Credential=$secretId/$credentialScope, SignedHeaders=$signedHeaders, Signature=$signature"
    }

    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun hmacSha256(data: String, key: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data.toByteArray(Charsets.UTF_8))
    }

    private fun hmacSha256Hex(data: String, key: ByteArray): String {
        val hashBytes = hmacSha256(data, key)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun mapTencentError(errorCode: String): String {
        return when (errorCode) {
            "FailedOperation.NoFreeAmount" -> "本月免费额度已用完"
            "FailedOperation.ServiceIsolate" -> "账号因欠费停止服务"
            "FailedOperation.UserNotRegistered" -> "服务未开通"
            "InternalError" -> "内部错误"
            "InternalError.BackendTimeout" -> "后台服务超时"
            "InternalError.ErrorUnknown" -> "未知错误"
            "InternalError.RequestFailed" -> "请求失败"
            "InvalidParameter" -> "参数错误"
            "InvalidParameter.MissingParameter" -> "缺少参数错误"
            "LimitExceeded" -> "超过配额限制"
            "LimitExceeded.LimitedAccessFrequency" -> "超出请求频率"
            "MissingParameter" -> "缺少参数错误"
            "UnauthorizedOperation.ActionNotFound" -> "请填写正确的Action字段名称"
            "UnsupportedOperation" -> "操作不支持"
            "UnsupportedOperation.TextTooLong" -> "单次请求文本超过长度限制"
            "UnsupportedOperation.UnSupportedTargetLanguage" -> "不支持的目标语言"
            "UnsupportedOperation.UnsupportedLanguage" -> "不支持的语言"
            "UnsupportedOperation.UnsupportedSourceLanguage" -> "不支持的源语言"
            else -> "未知错误代码: $errorCode"
        }
    }
}