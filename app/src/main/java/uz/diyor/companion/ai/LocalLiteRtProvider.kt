package uz.diyor.companion.ai

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import java.io.File
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
                require(modelFile.isFile && modelFile.length() > 0L) {
                    "AI modeli topilmadi. Avval modelni yuklab oling."
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
                conversation = candidate.createConversation(
                    ConversationConfig(
                        systemInstruction = Contents.of(AiraPersona.systemInstruction)
                    )
                )
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
                val activeConversation = conversation
                    ?: throw IllegalStateException("AI modeli hali tayyor emas")
                val answer = activeConversation.sendMessage(request.userMessage).text
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
