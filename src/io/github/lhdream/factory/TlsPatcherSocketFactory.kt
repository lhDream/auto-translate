package io.github.lhdream.factory

import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/**
 * 一个包装类，用于创建启用了更强加密套件的SSLSocket。
 * 此工厂用于在可能默认禁用它们的旧版Java上启用现代加密套件。
 */
class TlsPatcherSocketFactory : SSLSocketFactory() {

    // 使用属性委托来初始化 SSLSocketFactory 的实例
    private val delegate: SSLSocketFactory by lazy {
        val context = SSLContext.getInstance("TLS")
        context.init(null, null, null)
        context.socketFactory
    }

    private val ENABLED_PROTOCOLS = arrayOf("TLSv1.3", "TLSv1.2","TLSv1.1","TLSv1")
    private val ENABLED_CIPHER_SUITES = arrayOf( // TLS 1.3 Cipher Suites (Java 11+ supports them)
        "TLS_AES_128_GCM_SHA256",
        "TLS_AES_256_GCM_SHA384",
        "TLS_CHACHA20_POLY1305_SHA256",  // TLS 1.2 Modern Cipher Suites
        "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
        "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
        "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
        "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256" // 可以根据需要添加更多
    )

    override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites

    override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites

    @Throws(IOException::class)
    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket =
        patch(delegate.createSocket(s, host, port, autoClose))

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int): Socket =
        patch(delegate.createSocket(host, port))

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket =
        patch(delegate.createSocket(host, port, localHost, localPort))

    @Throws(IOException::class)
    override fun createSocket(host: InetAddress, port: Int): Socket =
        patch(delegate.createSocket(host, port))

    @Throws(IOException::class)
    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket =
        patch(delegate.createSocket(address, port, localAddress, localPort))

    /**
     * 核心方法，用于修改 Socket 的加密套件配置
     */
    private fun patch(socket: Socket): Socket {
        if (socket is SSLSocket) {
            // 获取当前 JVM 支持的所有协议
            val supportedProtocols = socket.supportedProtocols.toSet()

            // 从我们期望启用的协议列表中，筛选出当前 JVM 真正支持的那些
            val protocolsToEnable = ENABLED_PROTOCOLS.filter { supportedProtocols.contains(it) }
            // 如果筛选结果不为空，则应用它
            if (protocolsToEnable.isNotEmpty()) {
                socket.enabledProtocols = protocolsToEnable.toTypedArray()
            }
        }
        return socket
    }
}
