package uz.diyor.companion

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import uz.diyor.companion.ai.AiraModelDownloader
import uz.diyor.companion.ai.AiraModelProfile
import java.util.Locale

class ModelDownloadActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var progress: ProgressBar
    private lateinit var smallButton: Button
    private lateinit var largeButton: Button
    private val downloader by lazy { AiraModelDownloader(applicationContext) }
    private var lastPercent = -1

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(12, 10, 22)
        window.navigationBarColor = Color.rgb(12, 10, 22)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(22), dp(24), dp(22), dp(24))
            setBackgroundColor(Color.rgb(12, 10, 22))
        }
        root.addView(TextView(this).apply {
            text = "✦  AIRA LOCAL AI"
            textSize = 23f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(205, 185, 255))
        })
        root.addView(TextView(this).apply {
            text = "AIRA miyasi — mahalliy AI modellari\nChat matnlari AI serveriga yuborilmaydi."
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
            setPadding(0, dp(16), 0, dp(14))
        })
        status = TextView(this).apply {
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(0, dp(10), 0, dp(10))
        }
        root.addView(status)
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 1000
            progress = 0
        }
        root.addView(progress, LinearLayout.LayoutParams(-1, dp(12)))
        smallButton = Button(this).apply {
            text = "Qwen3-0.6B · 614 MB"
            isAllCaps = false
            setOnClickListener { confirmDownload(AiraModelProfile.QWEN_06B) }
        }
        root.addView(smallButton, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(14) })
        largeButton = Button(this).apply {
            text = "Kuchliroq Qwen3-1.7B INT4 · 977 MB"
            isAllCaps = false
            setOnClickListener { confirmDownload(AiraModelProfile.QWEN_17B_INT4) }
        }
        root.addView(largeButton, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
        root.addView(TextView(this).apply {
            text = "1.7B model uchun qo‘shimcha xotira kerak bo‘ladi. Wi-Fi orqali yukla. Eski model o‘chirilmaydi."
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(165, 145, 205))
            setPadding(0, dp(14), 0, 0)
        })
        setContentView(root)
        refreshInstalledState()
    }

    override fun onResume() {
        super.onResume()
        refreshInstalledState()
    }

    private fun refreshInstalledState() {
        val small = downloader.installedModel(AiraModelProfile.QWEN_06B) != null
        val large = downloader.installedModel(AiraModelProfile.QWEN_17B_INT4) != null
        status.text = when {
            large -> "Faol tanlov: Qwen3-1.7B INT4 — SHA-256 tekshiruvdan o‘tgan."
            small -> "Qwen3-0.6B tayyor. Kuchliroq 1.7B modelni ham yuklashing mumkin."
            else -> "Hali tekshirilgan model topilmadi. Modelni tanlab yukla."
        }
        smallButton.isEnabled = true
        largeButton.isEnabled = true
    }

    private fun confirmDownload(profile: AiraModelProfile) {
        val size = if (profile == AiraModelProfile.QWEN_17B_INT4) "taxminan 977 MB" else "taxminan 614 MB"
        AlertDialog.Builder(this)
            .setTitle("${profile.displayName} yuklansinmi?")
            .setMessage("Yuklash hajmi: $size. Eski model saqlanib qoladi. Yuklangach SHA-256 tekshiriladi. Wi-Fi tavsiya etiladi.")
            .setNegativeButton("Bekor qilish", null)
            .setPositiveButton("Yuklab olish") { _, _ -> startDownload(profile) }
            .show()
    }

    private fun startDownload(profile: AiraModelProfile) {
        smallButton.isEnabled = false
        largeButton.isEnabled = false
        progress.progress = 0
        lastPercent = -1
        status.text = "${profile.displayName}: serverga ulanmoqda..."
        downloader.download(
            profile = profile,
            onProgress = { downloaded, total ->
                val percent = if (total > 0L) ((downloaded * 100L) / total).toInt().coerceIn(0, 100) else 0
                if (percent != lastPercent) {
                    lastPercent = percent
                    runOnUiThread {
                        progress.progress = percent * 10
                        status.text = String.format(
                            Locale.US, "${profile.displayName}: %d%% · %.1f / %.1f MB",
                            percent, downloaded / 1_000_000.0, total / 1_000_000.0
                        )
                    }
                }
            },
            onComplete = { result ->
                runOnUiThread {
                    smallButton.isEnabled = true
                    largeButton.isEnabled = true
                    result.onSuccess {
                        progress.progress = 1000
                        status.text = "${profile.displayName} yuklandi va SHA-256 tekshiruvdan o‘tdi. Chatni qayta och."
                    }.onFailure { error ->
                        status.text = "Yuklash xatosi: ${error.message ?: "Noma’lum xato"}"
                    }
                }
            }
        )
    }
}
