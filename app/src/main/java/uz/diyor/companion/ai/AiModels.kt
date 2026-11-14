package uz.diyor.companion.ai

enum class AiRole {
    SYSTEM,
    USER,
    ASSISTANT
}

data class AiMessage(
    val role: AiRole,
    val content: String
)

data class AiRequest(
    val messages: List<AiMessage>,
    val userMessage: String,
    val systemInstruction: String,
    val maxOutputTokens: Int = 512
)

data class AiReply(
    val text: String,
    val providerId: String,
    val isFallback: Boolean = false
)

sealed class AiFailure {
    data class Unavailable(val reason: String) : AiFailure()
    data class InvalidRequest(val reason: String) : AiFailure()
    data class Internal(val reason: String) : AiFailure()
}

interface AiCallback {
    fun onSuccess(reply: AiReply)
    fun onFailure(failure: AiFailure)
}
