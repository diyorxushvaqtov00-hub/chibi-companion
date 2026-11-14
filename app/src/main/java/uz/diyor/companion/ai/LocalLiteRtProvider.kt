package uz.diyor.companion.ai

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * One executor owns the LiteRT-LM Engine and Conversation.
 * Initialization, inference and resource release are serialized off the UI thread.
 */
class LocalLiteRtProvider(
    context: Context,
    private val modelFile: File
) : AiProvider, AutoCloseable {
    override val id: String = "litert-lm-local"
    private val appContext = context.applicationContext
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private var engine: Engine? = null
    private var conversation: Conversation? = null
    private var initializationError: String? = null
    @Volatile private var closed = false

    init {
        executor.execute {
            try {
                val profile = AiraModelDownloader(appContext).profileFor(modelFile)
                    ?: throw IllegalArgumentException("Noma’lum model fayli: ${modelFile.name}")
                require(modelFile.isFile && modelFile.length() in profile.minBytes..profile.maxBytes) {
                    "AI modeli hajmi noto‘g‘ri. ${profile.displayName} modelini qayta yuklab oling."
                }
                val actualHash = sha256(modelFile)
                require(actualHash == profile.sha256) {
                    "${profile.displayName} SHA-256 tekshiruvidan o‘tmadi. Faylni qayta yuklab oling."
                }
                val candidate = Engine(
                    EngineConfig(
                        modelPath = modelFile.absolutePath,
                        backend = Backend.CPU(),
                        cacheDir = appContext.cacheDir.absolutePath
                    )
                )
                candidate.initialize()
                engine = candidate
            } catch (error: Exception) {
                initializationError = error.message ?: "Mahalliy AI modelini ishga tushirib bo‘lmadi"
            }
        }
    }

    override fun generate(request: AiRequest, callback: AiCallback) {
        if (closed) {
            callback.onFailure(AiFailure.Unavailable("AIRA AI xizmati yopilgan."))
            return
        }
        executor.execute {
            try {
                val error = initializationError
                if (error != null) {
                    callback.onFailure(AiFailure.Unavailable(error))
                    return@execute
                }
                val activeEngine = engine
                    ?: throw IllegalStateException("AI modeli hali tayyor emas")
                val activeConversation = conversation ?: activeEngine.createConversation(
                    ConversationConfig(
                        systemInstruction = Contents.of(request.systemInstruction),
                        initialMessages = request.messages.mapNotNull { message ->
                            when (message.role) {
                                AiRole.USER -> Message.user(message.content)
                                AiRole.ASSISTANT -> Message.model(message.content)
                                AiRole.SYSTEM -> null
                            }
                        }
                    )
                ).also { conversation = it }
                val answer = activeConversation.sendMessage(request.userMessage).toString()
                callback.onSuccess(
                    AiReply(
                        text = answer.ifBlank { "Hozir javob hosil bo‘lmadi. Yana urinib ko‘r." },
                        providerId = id,
                        isFallback = false
                    )
                )
            } catch (error: Exception) {
                callback.onFailure(
                    AiFailure.Internal(error.message ?: "Mahalliy AI xatosi")
                )
            }
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(256 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }
    }

    override fun close() {
        if (closed) return
        closed = true
        executor.execute {
            try {
                conversation?.close()
            } catch (_: Exception) {
            } finally {
                conversation = null
            }
            try {
                engine?.close()
            } catch (_: Exception) {
            } finally {
                engine = null
            }
        }
        executor.shutdown()
    }
}
