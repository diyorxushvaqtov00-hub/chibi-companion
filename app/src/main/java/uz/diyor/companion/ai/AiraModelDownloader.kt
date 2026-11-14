package uz.diyor.companion.ai

import android.content.Context
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Downloads the public Qwen3-0.6B LiteRT-LM file only after explicit user action.
 * The model is stored outside the APK in app-private filesDir.
 */
class AiraModelDownloader(context: Context) {
    companion object {
        const val MODEL_FILE_NAME = "Qwen3-0.6B.litertlm"
        const val MODEL_URL =
            "https://huggingface.co/litert-community/Qwen3-0.6B/resolve/main/Qwen3-0.6B.litertlm"
        const val EXPECTED_SHA256 =
            "c5f39b4191926786c3e6668b62af4d6eb15248dfc7a5348252a9b406f33f2108"
        const val EXPECTED_SIZE_BYTES = 614_000_000L
    }

    private val modelFile = File(context.filesDir, MODEL_FILE_NAME)
    private val partialFile = File(context.filesDir, "$MODEL_FILE_NAME.part")

    fun modelFileIfPresent(): File? = modelFile.takeIf { it.isFile && it.length() > 0L }

    fun installedModel(): File? {
        if (!modelFile.isFile || modelFile.length() <= 0L) return null
        return if (sha256(modelFile) == EXPECTED_SHA256) modelFile else null
    }

    fun download(
        onProgress: (downloaded: Long, total: Long) -> Unit,
        onComplete: (Result<File>) -> Unit
    ) {
        Thread({
            var connection: HttpURLConnection? = null
            try {
                partialFile.delete()
                connection = (URL(MODEL_URL).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 20_000
                    readTimeout = 30_000
                    instanceFollowRedirects = true
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "AIRA-Android/1.0")
                }
                connection.connect()
                val status = connection.responseCode
                check(status in 200..299) { "Model server returned HTTP $status" }

                val declaredLength = connection.contentLengthLong
                if (declaredLength > 0L) {
                    check(declaredLength in 550_000_000L..700_000_000L) {
                        "Model hajmi kutilgan qiymatga mos emas: $declaredLength bayt"
                    }
                }

                var downloaded = 0L
                connection.inputStream.use { input ->
                    partialFile.outputStream().buffered().use { output ->
                        val buffer = ByteArray(1024 * 256)
                        while (true) {
                            if (Thread.currentThread().isInterrupted) {
                                throw InterruptedException("Yuklab olish bekor qilindi")
                            }
                            val count = input.read(buffer)
                            if (count < 0) break
                            output.write(buffer, 0, count)
                            downloaded += count
                            onProgress(downloaded, if (declaredLength > 0L) declaredLength else EXPECTED_SIZE_BYTES)
                        }
                        output.flush()
                    }
                }

                check(partialFile.length() in 550_000_000L..700_000_000L) {
                    "Yuklab olingan fayl hajmi noto‘g‘ri: ${partialFile.length()} bayt"
                }
                check(sha256(partialFile) == EXPECTED_SHA256) {
                    "SHA-256 tekshiruvi muvaffaqiyatsiz. Fayl xavfsiz emas yoki buzilgan."
                }
                if (modelFile.exists()) modelFile.delete()
                check(partialFile.renameTo(modelFile)) { "Model faylini saqlab bo‘lmadi" }
                onComplete(Result.success(modelFile))
            } catch (error: Exception) {
                partialFile.delete()
                onComplete(Result.failure(error))
            } finally {
                connection?.disconnect()
            }
        }, "aira-model-download").start()
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(1024 * 256)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }
    }
}
