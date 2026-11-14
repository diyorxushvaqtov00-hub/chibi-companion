package uz.diyor.companion.ai

import android.content.Context
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

enum class AiraModelProfile(
    val fileName: String,
    val url: String,
    val sha256: String,
    val expectedBytes: Long,
    val minBytes: Long,
    val maxBytes: Long,
    val displayName: String
) {
    QWEN_06B(
        "Qwen3-0.6B.litertlm",
        "https://huggingface.co/litert-community/Qwen3-0.6B/resolve/main/Qwen3-0.6B.litertlm",
        "c5f39b4191926786c3e6668b62af4d6eb15248dfc7a5348252a9b406f33f2108",
        614_000_000L, 550_000_000L, 700_000_000L, "Qwen3-0.6B"
    ),
    QWEN_17B_INT4(
        "Qwen3-1.7B_dynamic_wi4b32_afp32.litertlm",
        "https://huggingface.co/litert-community/Qwen3-1.7B/resolve/d9b8a9126e5ac18591306eacd4311ba43b92421e/Qwen3-1.7B_dynamic_wi4b32_afp32.litertlm",
        "2eeffef7b51bc3e1225ea69fe7aa5f417397934b56a5b6c20cc068d6fd2c918b",
        977_184_032L, 950_000_000L, 1_000_000_000L, "Qwen3-1.7B INT4"
    )
}

class AiraModelDownloader(context: Context) {
    private val directory = context.filesDir

    fun modelFileIfPresent(): File? =
        listOf(AiraModelProfile.QWEN_17B_INT4, AiraModelProfile.QWEN_06B)
            .asSequence()
            .map { File(directory, it.fileName) }
            .firstOrNull { it.isFile && it.length() > 0L }

    fun profileFor(file: File): AiraModelProfile? =
        AiraModelProfile.entries.firstOrNull { it.fileName == file.name }

    fun installedModel(profile: AiraModelProfile): File? {
        val file = File(directory, profile.fileName)
        if (!file.isFile || file.length() !in profile.minBytes..profile.maxBytes) return null
        return if (sha256(file) == profile.sha256) file else null
    }

    fun download(
        profile: AiraModelProfile,
        onProgress: (downloaded: Long, total: Long) -> Unit,
        onComplete: (Result<File>) -> Unit
    ) {
        Thread({
            var connection: HttpURLConnection? = null
            val target = File(directory, profile.fileName)
            val partial = File(directory, "${profile.fileName}.part")
            try {
                partial.delete()
                connection = (URL(profile.url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 25_000
                    readTimeout = 60_000
                    instanceFollowRedirects = true
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "AIRA-Android/1.0")
                }
                connection.connect()
                check(connection.responseCode in 200..299) {
                    "Model server HTTP ${connection.responseCode}"
                }
                val declared = connection.contentLengthLong
                if (declared > 0L) check(declared in profile.minBytes..profile.maxBytes) {
                    "Serverdan kelgan model hajmi noto‘g‘ri: $declared bayt"
                }
                var downloaded = 0L
                connection.inputStream.use { input ->
                    partial.outputStream().buffered().use { output ->
                        val buffer = ByteArray(256 * 1024)
                        while (true) {
                            if (Thread.currentThread().isInterrupted) {
                                throw InterruptedException("Yuklash bekor qilindi")
                            }
                            val count = input.read(buffer)
                            if (count < 0) break
                            output.write(buffer, 0, count)
                            downloaded += count
                            onProgress(downloaded, if (declared > 0L) declared else profile.expectedBytes)
                        }
                        output.flush()
                    }
                }
                check(partial.length() in profile.minBytes..profile.maxBytes) {
                    "Yuklangan fayl hajmi noto‘g‘ri: ${partial.length()} bayt"
                }
                check(sha256(partial) == profile.sha256) {
                    "SHA-256 tekshiruvi xato. Faylni qayta yuklash kerak."
                }
                if (target.exists()) target.delete()
                check(partial.renameTo(target)) { "Model faylini saqlab bo‘lmadi" }
                onComplete(Result.success(target))
            } catch (error: Exception) {
                partial.delete()
                onComplete(Result.failure(error))
            } finally {
                connection?.disconnect()
            }
        }, "aira-model-download").start()
    }

    fun sha256(file: File): String {
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
}
