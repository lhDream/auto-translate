package io.github.lhdream.expansion

import io.github.lhdream.factory.TlsPatcherSocketFactory
import javax.net.ssl.HttpsURLConnection


import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.Security
import java.util.zip.GZIPInputStream


class HttpRequest private constructor(private val url: String, private val method: String) {

    private val headers = mutableMapOf<String, String>()
    private var body: String? = null
    private var connectTimeout: Int = 15000 // 15秒
    private var readTimeout: Int = 15000    // 15秒
    private var useTlsPatcher: Boolean = false

    // 使用数据类来封装响应结果，自带 equals, hashCode, toString 等方法
    data class Result(
        val statusCode: Int,
        val body: String?,
        val error: Exception? = null
    ) {
        val isSuccess: Boolean
            get() = error == null && statusCode in 200..299
    }

    /**
     * 添加请求头。
     */
    fun header(key: String, value: String) = apply { headers[key] = value }

    /**
     * 设置POST请求的请求体。
     */
    fun body(body: String) = apply {
        if (method == "POST") {
            this.body = body
        } else {
            System.err.println("Warning: Body is not applicable for non-POST requests.")
        }
    }

    /**
     * 设置连接超时（毫秒）。
     */
    fun connectTimeout(timeout: Int) = apply { connectTimeout = timeout }

    /**
     * 设置读取超时（毫秒）。
     */
    fun readTimeout(timeout: Int) = apply { readTimeout = timeout }

    /**
     * 启用TLS补丁，用于解决旧版JDK下的SSL握手失败问题。
     */
    fun withTlsPatcher() = apply { useTlsPatcher = true }

    /**
     * 执行HTTP请求。
     */
    fun execute(): Result {
        var connection: HttpURLConnection? = null
        return try {
            val requestUrl = URL(url)
            connection = (requestUrl.openConnection() as HttpURLConnection).apply {
                // 如果是HTTPS并且启用了TLS补丁
                if (useTlsPatcher && this is HttpsURLConnection) {
                    // 临时应用我们定制的工厂
                    this.sslSocketFactory = TlsPatcherSocketFactory()
                }

                requestMethod = method
                this.connectTimeout = this@HttpRequest.connectTimeout
                this.readTimeout = this@HttpRequest.readTimeout

                // 设置默认header，如果用户没有提供的话
                headers.putIfAbsent("Accept-Encoding", "gzip")
                headers.putIfAbsent("User-Agent", "LhDream-HttpRequest-Client/1.0")
                headers.putIfAbsent("Accept", "*/*")
                headers.putIfAbsent("Host", requestUrl.host)

                // 应用所有请求头
                headers.forEach { (key, value) -> setRequestProperty(key, value) }

                // 如果是POST请求，写入请求体
                if (method == "POST" && body != null) {
                    doOutput = true
                    outputStream.use { os ->
                        os.write(body!!.toByteArray(StandardCharsets.UTF_8))
                    }
                }
            }

            val statusCode = connection.responseCode
            val responseBody = connection.getInputStreamSafely().readAndClose(connection)
            Result(statusCode, responseBody)

        } catch (e: IOException) {
            e.printStackTrace()
            val errorBody = connection?.errorStream?.readAndClose(connection)
            val errorMessage = "Request failed with error. ${errorBody?.let { "Error Body: $it" } ?: ""}, error: ${e.stackTraceToString()}"
            Result(connection?.responseCode ?: -1, errorBody, IOException(errorMessage, e))
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * 安全地获取输入流，优先获取正常输入流，失败则获取错误流。
     */
    private fun HttpURLConnection.getInputStreamSafely(): InputStream? {
        return try {
            inputStream
        } catch (e: IOException) {
            errorStream
        }
    }

    /**
     * 读取输入流内容并关闭它。
     */
    private fun InputStream?.readAndClose(connection: HttpURLConnection): String {
        if (this == null) return ""
        // 检查 Content-Encoding 响应头
        val contentEncoding = connection.getHeaderField("Content-Encoding")
        val reader = if ("gzip".equals(contentEncoding, ignoreCase = true)) {
            // 如果是 gzip 编码，则使用 GZIPInputStream 解压
            BufferedReader(InputStreamReader(GZIPInputStream(this), StandardCharsets.UTF_8))
        } else {
            BufferedReader(InputStreamReader(this, StandardCharsets.UTF_8))
        }
        return reader.use { it.readText() }
    }

    companion object {
        /**
         * 创建一个GET请求。
         */
        @JvmStatic
        fun get(url: String) = HttpRequest(url, "GET")

        /**
         * 创建一个POST请求。
         */
        @JvmStatic
        fun post(url: String) = HttpRequest(url, "POST")
    }
}


fun <T> withTls12And13(block: () -> T): T {
    val originalProtocols = System.getProperty("https.protocols")
    val origDisabledAlgos = Security.getProperty("jdk.tls.disabledAlgorithms")
    try {
        System.setProperty("https.protocols", "TLSv1.2,TLSv1.3")
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2,TLSv1.3")
        Security.setProperty("jdk.tls.disabledAlgorithms", "")
        return block()
    } finally {
        originalProtocols?: System.clearProperty("originalProtocols")
        origDisabledAlgos?: System.clearProperty("jdk.tls.disabledAlgorithms")
        if (originalProtocols == null) {
            System.clearProperty("https.protocols")
        } else {
            System.setProperty("https.protocols", originalProtocols)
            System.setProperty("jdk.tls.disabledAlgorithms", origDisabledAlgos)
        }
    }
}