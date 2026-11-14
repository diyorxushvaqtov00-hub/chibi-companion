package uz.diyor.companion.ai

/**
 * Provider-neutral boundary for AIRA's language model.
 *
 * Implementations may use an on-device model or an explicitly configured
 * remote provider. Providers must never silently upload chat history.
 */
interface AiProvider {
    val id: String

    fun generate(request: AiRequest, callback: AiCallback)

    fun cancel() = Unit
}
