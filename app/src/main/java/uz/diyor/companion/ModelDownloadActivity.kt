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
import java.util.Locale

class ModelDownloadActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var progress: ProgressBar
    private lateinit var downloadButton: Button
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
            text = "Qwen3-0.6B · taxminan 614 MB\nModel telefonda ishlaydi. Chat matnlari AI serveriga yuborilmaydi."
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
            setPadding(0, dp(20), 0, dp(20))
        })
        status = TextView(this).apply {
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(0, dp(12), 0, dp(12))
        }
        root.addView(status)
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 1000
            progress = 0
        }
        root.addView(progress, LinearLayout.LayoutParams(-1, dp(12)))
        downloadButton = Button(this).apply {
            text = "AI modelni yuklab olish"
            isAllCaps = false
            setOnClickListener { confirmDownload() }
        }
        root.addView(downloadButton, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(22)
        })
        root.addView(TextView(this).apply {
            text = "Tavsiya: Wi-Fi orqali yuklab ol. Model faqat sen tugmani bosganingda yuklanadi."
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(165, 145, 205))
            setPadding(0, dp(18), 0, 0)
        })
        setContentView(root)
        refreshInstalledState()
    }

    private fun refreshInstalledState() {
        val valid = downloader.installedModel()
        if (valid != null) {
            status.text = "Model tayyor. AIRA mahalliy AI rejimida ishlashi mumkin."
            progress.progress = 1000
            downloadButton.text = "Model qayta yuklash"
        } else {
            status.text = "Model hali yuklanmagan."
            progress.progress = 0
        }
    }

    private fun confirmDownload() {
        AlertDialog.Builder(this)
            .setTitle("614 MB modelni yuklab olamizmi?")
            .setMessage("Bu fayl telefon xotirasiga saqlanadi. Wi-Fi ishlatishingni tavsiya qilaman. Yuklab olish tugagach SHA-256 tekshiruvi bajariladi.")
            .setNegativeButton("Bekor qilish", null)
            .setPositiveButton("Yuklab olish") { _, _ -> startDownload() }
            .show()
    }

    private fun startDownload() {
        downloadButton.isEnabled = false
        progress.progress = 0
        lastPercent = -1
        status.text = "Serverga ulanmoqda..."
        downloader.download(
            onProgress = { downloaded, total ->
                val percent = if (total > 0L) ((downloaded * 100L) / total).toInt().coerceIn(0, 100) else 0
                if (percent != lastPercent) {
                    lastPercent = percent
                    runOnUiThread {
                        progress.progress = percent * 10
                        status.text = String.format(
                            Locale.US,
                            "Yuklanmoqda: %d%%  ·  %.1f / %.1f MB",
                            percent,
                            downloaded / 1_000_000.0,
                            total / 1_000_000.0
                        )
                    }
                }
            },
            onComplete = { result ->
                runOnUiThread {
                    downloadButton.isEnabled = true
                    result.onSuccess {
                        progress.progress = 1000
                        status.text = "Model yuklandi va SHA-256 tekshiruvdan o‘tdi. Endi AIRA chatini och."
                        downloadButton.text = "Model qayta yuklash"
                    }.onFailure { error ->
                        status.text = "Yuklash xatosi: ${error.message ?: "Noma’lum xato"}"
                    }
                }
            }
        )
    }
}
