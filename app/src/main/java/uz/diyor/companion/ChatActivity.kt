package uz.diyor.companion

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class ChatActivity : Activity() {
    override fun onResume() {
        super.onResume()
        getSharedPreferences("companion_state", MODE_PRIVATE).edit().putBoolean("app_visible", true).apply()
    }

    override fun onPause() {
        getSharedPreferences("companion_state", MODE_PRIVATE).edit().putBoolean("app_visible", false).apply()
        super.onPause()
    }

    private lateinit var messages: LinearLayout
    private lateinit var input: EditText
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(12, 10, 22)
        window.navigationBarColor = Color.rgb(12, 10, 22)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(12))
            setBackgroundColor(Color.rgb(12, 10, 22))
        }
        root.addView(TextView(this).apply {
            text = "✦  A I R A"
            textSize = 22f
            letterSpacing = 0.12f
            setTextColor(Color.rgb(205, 185, 255))
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(12))
        })
        root.addView(TextView(this).apply {
            text = "M1 • OFFLINE DEMO"
            textSize = 11f
            letterSpacing = 0.16f
            setTextColor(Color.rgb(150, 140, 175))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(10))
        })
        val scroll = ScrollView(this)
        messages = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }
        scroll.addView(messages)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        input = EditText(this).apply {
            hint = "AIRA ga yoz..."
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_SEND
            setPadding(dp(12), 0, dp(12), 0)
        }
        row.addView(input, LinearLayout.LayoutParams(0, dp(52), 1f))
        row.addView(Button(this).apply {
            text = "Yuborish"
            isAllCaps = false
            setOnClickListener { sendMessage() }
        })
        root.addView(row)
        setContentView(root)

        addMessage("AIRA", "Salom. Men AIRA. Odatda ortiqcha gapirmayman. Lekin seni tinglashga vaqtim bor.")
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
        addMessage("Sen", text)
        input.setText("")
        addMessage("AIRA", makeReply(text))
    }

    // M1 placeholder persona logic. This is deliberately transparent: not a real language model.
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
                "Tushundim. Hozir maslahat berishga shoshilmayman. Nima bo‘lganini aytib berishing mumkin."
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

    private fun addMessage(who: String, text: String) {
        messages.addView(TextView(this).apply {
            this.text = "$who: $text"
            textSize = 16f
            setTextColor(if (who == "AIRA") Color.rgb(215, 198, 255) else Color.WHITE)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(if (who == "AIRA") Color.rgb(38, 29, 62) else Color.rgb(37, 37, 45))
                cornerRadius = dp(14).toFloat()
            }
        }, LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, dp(4), 0, dp(4))
        })
    }
}
