package io.github.lhdream.data.remote.openai

import com.google.gson.Gson
import io.github.lhdream.data.model.openai.OpenAIChatRequest
import io.github.lhdream.data.model.openai.OpenAIConfig
import io.github.lhdream.infrastructure.network.HttpRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class OpenAIRepositoryTest {

    private val config = OpenAIConfig(
        apiKey = "test-key",
        baseUrl = "https://example.test/v1/",
        modelName = "test-model"
    )

    @Test
    fun buildsChatCompletionRequestAndParsesResponse() = runBlocking {
        val transport = FakeTransport(
            HttpRequest.Result(
                statusCode = 200,
                body = """{"choices":[{"message":{"content":" translated text "}}]}"""
            )
        )
        val repository = OpenAIRepository(transport = transport, ioDispatcher = Dispatchers.Unconfined)

        val result = repository.translate(config, "hello", "zh")

        assertEquals("translated text", result)
        assertEquals("https://example.test/v1/chat/completions", transport.url)
        assertEquals("Bearer test-key", transport.headers["Authorization"])
        assertEquals("application/json", transport.headers["Content-Type"])

        val request = Gson().fromJson(transport.body, OpenAIChatRequest::class.java)
        assertEquals("test-model", request.model)
        assertEquals(listOf("system", "user"), request.messages.map { it.role })
        assertEquals("hello", request.messages[1].content)
        assertTrue(request.messages[0].content.contains("zh"))
    }

    @Test
    fun acceptsAFullChatCompletionsBaseUrl() = runBlocking {
        val transport = FakeTransport(
            HttpRequest.Result(200, """{"choices":[{"message":{"content":"ok"}}]}""")
        )
        val repository = OpenAIRepository(transport = transport, ioDispatcher = Dispatchers.Unconfined)

        repository.translate(
            config.copy(baseUrl = "https://example.test/v1/chat/completions"),
            "hello",
            "en"
        )

        assertEquals("https://example.test/v1/chat/completions", transport.url)
    }

    @Test
    fun reportsApiErrorsWithoutLeakingTheApiKey() = runBlocking {
        val transport = FakeTransport(
            HttpRequest.Result(
                statusCode = 401,
                body = """{"error":{"message":"invalid API key","type":"auth_error"}}"""
            )
        )
        val repository = OpenAIRepository(transport = transport, ioDispatcher = Dispatchers.Unconfined)

        val error = assertFailsWith<IllegalStateException> {
            repository.translate(config, "hello", "en")
        }

        assertTrue(error.message!!.contains("invalid API key"))
        assertTrue(!error.message!!.contains(config.apiKey))
    }

    private class FakeTransport(private val response: HttpRequest.Result) : OpenAITransport {
        lateinit var url: String
        lateinit var headers: Map<String, String>
        lateinit var body: String

        override fun post(url: String, headers: Map<String, String>, body: String): HttpRequest.Result {
            this.url = url
            this.headers = headers
            this.body = body
            return response
        }
    }
}
