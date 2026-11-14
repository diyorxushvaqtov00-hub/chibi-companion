package uz.diyor.companion

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray

class SettingsActivity : Activity() {
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(12, 10, 22)
        window.navigationBarColor = Color.rgb(12, 10, 22)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(18))
            setBackgroundColor(Color.rgb(12, 10, 22))
        }
        root.addView(TextView(this).apply {
            text = "✦  AIRA SOZLAMALARI"
            textSize = 21f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(205, 185, 255))
            setPadding(0, 0, 0, dp(18))
        })
        val voicePrefs = getSharedPreferences("aira_voice", MODE_PRIVATE)
        val voiceSwitch = Switch(this).apply {
            text = "AIRA javoblarini ovoz chiqarib o‘qish"
            textSize = 14f
            setTextColor(Color.WHITE)
            isChecked = voicePrefs.getBoolean("speak_replies", false)
            setOnCheckedChangeListener { _, checked ->
                voicePrefs.edit().putBoolean("speak_replies", checked).apply()
            }
        }
        root.addView(voiceSwitch, LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = dp(18)
        })
        root.addView(makeButton("Suhbat tarixini ulashish") { shareChat() })
        root.addView(makeButton("Suhbat tarixini o‘chirish") { confirmClearChat() })
        root.addView(makeButton("AIRA xotiralarini boshqarish") {
            startActivity(android.content.Intent(this, MemoryActivity::class.java))
        })
        root.addView(makeButton("AI modelni boshqarish") {
            startActivity(android.content.Intent(this, ModelDownloadActivity::class.java))
        })
        root.addView(TextView(this).apply {
            text = "Maxfiylik: chat tarixi va qo‘lda saqlangan xotiralar ilovaning shaxsiy xotirasida saqlanadi. OCR tanlangan rasmdagi matnni qurilmaning o‘zida ajratadi. Mikrofon faqat tugma bosilganda ishlatiladi."
            textSize = 13f
            setTextColor(Color.rgb(175, 165, 195))
            setPadding(0, dp(22), 0, dp(12))
        })
        root.addView(TextView(this).apply {
            text = "AIRA 1.6 • Android • Local-first"
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(Color.GRAY)
        })
        setContentView(root)
    }

    private fun makeButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = dp(8)
        }
    }

    private fun confirmClearChat() {
        AlertDialog.Builder(this)
            .setTitle("Suhbat tarixi o‘chirilsinmi?")
            .setMessage("Bu amal chat oynasidagi saqlangan yozishmalarni o‘chiradi. AIRA xotiralari alohida saqlanadi.")
            .setNegativeButton("Bekor qilish", null)
            .setPositiveButton("O‘chirish") { _, _ ->
                getSharedPreferences("aira_chat", MODE_PRIVATE).edit().remove("messages").apply()
                Toast.makeText(this, "Suhbat tarixi o‘chirildi.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun shareChat() {
        val raw = getSharedPreferences("aira_chat", MODE_PRIVATE).getString("messages", null)
        if (raw.isNullOrBlank()) {
            Toast.makeText(this, "Ulashish uchun suhbat tarixi yo‘q.", Toast.LENGTH_SHORT).show()
            return
        }
        val text = try {
            val array = JSONArray(raw)
            buildString {
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    append(item.optString("who"))
                    append(": ")
                    append(item.optString("text"))
                    append("\n\n")
                }
            }
        } catch (_: Exception) {
            Toast.makeText(this, "Suhbat tarixini o‘qib bo‘lmadi.", Toast.LENGTH_LONG).show()
            return
        }
        startActivity(android.content.Intent.createChooser(
            android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_SUBJECT, "AIRA suhbat tarixi")
                putExtra(android.content.Intent.EXTRA_TEXT, text)
            },
            "Suhbatni ulashish"
        ))
    }
}
