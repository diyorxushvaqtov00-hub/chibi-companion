package uz.diyor.companion

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * Checks the GitHub latest release and stages its APK for Android's package installer.
 * Releases must use the same signing key as the installed app.
 */
class AiraUpdateManager(private val activity: Activity) {
    companion object {
        private const val RELEASE_API =
            "https://api.github.com/repos/diyorxushvaqtov00-hub/chibi-companion/releases/latest"
        private const val APK_ASSET = "aira-release.apk"
        private const val PREFS = "aira_updates"
        private const val STAGED_APK = "staged_apk"
        private val executor = Executors.newSingleThreadExecutor()
    }

    fun check(force: Boolean = false) {
        val prefs = activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE)
        val lastCheck = prefs.getLong("last_check", 0L)
        val now = System.currentTimeMillis()
        if (!force && now - lastCheck < 24L * 60L * 60L * 1000L) return

        Toast.makeText(activity, "AIRA yangilanishlarni tekshirmoqda...", Toast.LENGTH_SHORT).show()
        executor.execute {
            try {
                val connection = (URL(RELEASE_API).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 12000
                    readTimeout = 15000
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
                    setRequestProperty("User-Agent", "AIRA-Android-Updater")
                }
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                val release = JSONObject(response)
                val tag = release.optString("tag_name").removePrefix("v")
                val current = activity.packageManager.getPackageInfo(activity.packageName, 0).versionName ?: "0"
                prefs.edit().putLong("last_check", now).apply()

                if (compareVersions(tag, current) <= 0) {
                    if (force) activity.runOnUiThread {
                        Toast.makeText(activity, "AIRA eng so‘nggi versiyada ($current).", Toast.LENGTH_LONG).show()
                    }
                    return@execute
                }

                val assets = release.optJSONArray("assets") ?: throw IllegalStateException("Release APK topilmadi.")
                var downloadUrl: String? = null
                var size = 0L
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.optString("name") == APK_ASSET) {
                        downloadUrl = asset.optString("browser_download_url")
                        size = asset.optLong("size")
                        break
                    }
                }
                val assetUrl = downloadUrl ?: throw IllegalStateException("Release ichida $APK_ASSET yo‘q.")

                activity.runOnUiThread {
                    AlertDialog.Builder(activity)
                        .setTitle("AIRA yangilanishi mavjud")
                        .setMessage("Yangi versiya: $tag\nHajmi: %.1f MB\n\nYangilanishni yuklab olaymi?".format(size / 1_000_000.0))
                        .setNegativeButton("Keyin", null)
                        .setPositiveButton("Yuklab olish") { _, _ ->
                            downloadAndPrompt(assetUrl, tag)
                        }
                        .show()
                }
            } catch (error: Exception) {
                if (force) activity.runOnUiThread {
                    Toast.makeText(activity, "Yangilanish tekshirilmadi: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun downloadAndPrompt(url: String, version: String) {
        Toast.makeText(activity, "AIRA yangilanishi yuklanmoqda...", Toast.LENGTH_LONG).show()
        executor.execute {
            try {
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 30000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "AIRA-Android-Updater")
                }
                val file = File(activity.cacheDir, "aira-update.apk")
                connection.inputStream.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
                connection.disconnect()
                if (file.length() < 1_000_000L) throw IllegalStateException("Yuklangan APK hajmi noto‘g‘ri.")
                activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE)
                    .edit().putString(STAGED_APK, file.absolutePath).putString("staged_version", version).apply()
                activity.runOnUiThread { promptInstall(file) }
            } catch (error: Exception) {
                activity.runOnUiThread {
                    Toast.makeText(activity, "Yuklash xatosi: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun installStagedIfAllowed() {
        val path = activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE).getString(STAGED_APK, null)
        if (path.isNullOrBlank()) return
        val file = File(path)
        if (file.exists()) promptInstall(file)
    }

    private fun promptInstall(file: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !activity.packageManager.canRequestPackageInstalls()
        ) {
            AlertDialog.Builder(activity)
                .setTitle("O‘rnatishga ruxsat kerak")
                .setMessage("AIRA yangilanishini o‘rnatish uchun ushbu ilovaga noma’lum ilovalarni o‘rnatish ruxsatini bering. Keyin AIRA sozlamalariga qayting.")
                .setNegativeButton("Bekor qilish", null)
                .setPositiveButton("Ruxsat berish") { _, _ ->
                    activity.startActivity(
                        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${activity.packageName}"))
                    )
                }
                .show()
            return
        }

        try {
            val uri = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.fileprovider",
                file
            )
            val install = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            activity.startActivity(install)
        } catch (error: Exception) {
            Toast.makeText(activity, "O‘rnatish oynasi ochilmadi: ${error.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun compareVersions(a: String, b: String): Int {
        val left = a.split(".").map { it.toIntOrNull() ?: 0 }
        val right = b.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(left.size, right.size)) {
            val diff = (left.getOrElse(i) { 0 }).compareTo(right.getOrElse(i) { 0 })
            if (diff != 0) return diff
        }
        return 0
    }
}
