package uz.diyor.companion

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject

class ChatActivity : Activity() {
    private lateinit var messages: LinearLayout
    private lateinit var scroll: ScrollView
    private lateinit var input: EditText
    private val history = mutableListOf<Pair<String, String>>()
    private val prefs by lazy { getSharedPreferences("aira_chat", MODE_PRIVATE) }

    override fun onResume() {
        super.onResume()
        getSharedPreferences("companion_state", MODE_PRIVATE).edit().putBoolean("app_visible", true).apply()
    }

    override fun onPause() {
        getSharedPreferences("companion_state", MODE_PRIVATE).edit().putBoolean("app_visible", false).apply()
        super.onPause()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(12, 10, 22)
        window.navigationBarColor = Color.rgb(12, 10, 22)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundColor(Color.rgb(12, 10, 22))
        }
        root.addView(TextView(this).apply {
            text = "✦  A I R A"
            textSize = 22f
            letterSpacing = 0.12f
            setTextColor(Color.rgb(205, 185, 255))
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(8))
        })
        root.addView(TextView(this).apply {
            text = "Sovuqqon aql • Yaqinlarga mehr"
            textSize = 11f
            letterSpacing = 0.08f
            setTextColor(Color.rgb(150, 140, 175))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(12))
        })

        scroll = ScrollView(this).apply { isFillViewport = true }
        messages = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(4), dp(4), dp(8))
        }
        scroll.addView(messages)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        input = EditText(this).apply {
            hint = "AIRA ga yoz..."
            setHintTextColor(Color.rgb(130, 120, 150))
            setTextColor(Color.WHITE)
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_SEND
            setPadding(dp(12), 0, dp(12), 0)
            background = rounded(Color.rgb(35, 30, 49), dp(14).toFloat())
        }
        row.addView(input, LinearLayout.LayoutParams(0, dp(50), 1f))
        row.addView(Button(this).apply {
            text = "Yuborish"
            isAllCaps = false
            setTextColor(Color.WHITE)
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(111, 75, 190))
            setOnClickListener { sendMessage() }
        })
        root.addView(row)
        setContentView(root)

        restoreHistory()
        if (history.isEmpty()) {
            appendMessage("AIRA", "Salom. Men AIRA. Odatda ortiqcha gapirmayman. Lekin seni tinglashga vaqtim bor.")
        }
        input.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }
    }

    private fun sendMessage() {
        val text = input.text.toString().trim()
        if (text.isEmpty()) return
        appendMessage("Sen", text)
        input.setText("")
        appendMessage("AIRA", makeReply(text))
    }

    private fun makeReply(message: String): String {
        val lower = message.lowercase()
        return when {
            listOf("salom", "assalom", "hello", "hi").any { lower.contains(it) } ->
                "Salom. Seni eshitib turibman. Nima gap?"
            listOf("isming", "kimsan", "kim san").any { lower.contains(it) } ->
                "Men AIRA — virtual hamrohingman. Sovuqqon fikrlayman, ammo yaqinlarimga befarq emasman."
            listOf("rahmat", "raxmat").any { lower.contains(it) } ->
                "Arzimaydi. Kerak bo‘lsa, yana yordam beraman."
            listOf("xafa", "yomonman", "zerik", "tushkun").any { lower.contains(it) } ->
                "Tushundim. Maslahat berishga shoshilmayman. Nima bo‘lganini aytib berishing mumkin. Men tinglayman."
            listOf("anime", "manga", "manhwa").any { lower.contains(it) } ->
                "Qiziqarli mavzu. Qahramonning kuchidan ko‘ra, uning qarorlari ko‘proq narsani ochib beradi."
            listOf("yordam", "reja", "qanday").any { lower.contains(it) } ->
                "Avval muammoni aniq belgilaymiz. Keyin eng sodda va samarali yechimni tanlaymiz."
            lower.contains("?") ->
                "Savolingni tushundim. Hozir men demo rejimidaman, shuning uchun tashqi ma’lumotni tekshira olmayman. Keyingi bosqichda AI modeli ulanadi."
            else ->
                "Tushunarli. Davom et — gaplaringni diqqat bilan kuzatyapman."
        }
    }

    private fun appendMessage(who: String, text: String) {
        history.add(who to text)
        if (history.size > 80) history.removeAt(0)
        renderMessage(who, text)
        saveHistory()
        scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun renderMessage(who: String, text: String) {
        val bubble = TextView(this).apply {
            this.text = "$who\n$text"
            textSize = 15f
            setTextColor(if (who == "AIRA") Color.rgb(225, 213, 255) else Color.WHITE)
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = rounded(
                if (who == "AIRA") Color.rgb(38, 29, 62) else Color.rgb(42, 40, 50),
                dp(15).toFloat()
            )
        }
        val params = LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, dp(4), 0, dp(4))
        }
        messages.addView(bubble, params)
    }

    private fun rounded(color: Int, radius: Float) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius
    }

    private fun saveHistory() {
        val array = JSONArray()
        history.forEach { (who, text) ->
            array.put(JSONObject().put("who", who).put("text", text))
        }
        prefs.edit().putString("messages", array.toString()).apply()
    }

    private fun restoreHistory() {
        val raw = prefs.getString("messages", null) ?: return
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val who = item.optString("who")
                val text = item.optString("text")
                if (who.isNotBlank() && text.isNotBlank()) {
                    history.add(who to text)
                    renderMessage(who, text)
                }
            }
        } catch (_: Exception) {
            prefs.edit().remove("messages").apply()
            history.clear()
            messages.removeAllViews()
        }
    }
}
