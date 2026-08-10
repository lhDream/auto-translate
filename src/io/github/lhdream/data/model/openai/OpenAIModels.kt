package io.github.lhdream.data.model.openai

data class OpenAIConfig(
    val apiKey: String,
    val baseUrl: String,
    val modelName: String
)

data class OpenAIChatRequest(
    val model: String,
    val messages: List<OpenAIRequestMessage>
)

data class OpenAIRequestMessage(
    val role: String,
    val content: String
)

data class OpenAIChatResponse(
    val choices: List<OpenAIChoice>?
)

data class OpenAIChoice(
    val message: OpenAIResponseMessage?
)

data class OpenAIResponseMessage(
    val content: String?
)

data class OpenAIErrorResponse(
    val error: OpenAIError?
)

data class OpenAIError(
    val message: String?,
    val type: String?,
    val code: String?
)
