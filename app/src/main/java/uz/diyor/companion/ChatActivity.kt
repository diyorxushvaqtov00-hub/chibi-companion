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
    private lateinit var messages: LinearLayout
    private lateinit var input: EditText
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(20, 8, 20)
        window.navigationBarColor = Color.rgb(20, 8, 20)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(12))
            setBackgroundColor(Color.rgb(20, 8, 20))
        }
        root.addView(TextView(this).apply {
            text = "👑  MALIKA BILAN SUHBAT"
            textSize = 20f
            setTextColor(Color.rgb(255, 190, 220))
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(12))
        })
        root.addView(TextView(this).apply {
            text = "Hozircha internet/API talab qilmaydigan demo suhbat rejimi."
            textSize = 12f
            setTextColor(Color.LTGRAY)
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
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        input = EditText(this).apply {
            hint = "Malikaga yoz..."
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
        addMessage("Malika", "Salom! Men Malika 👑 Hozir demo rejimdaman. Nima haqida gaplashamiz?")
        input.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_SEND) { sendMessage(); true } else false
        }
    }

    private fun sendMessage() {
        val text = input.text.toString().trim()
        if (text.isEmpty()) return
        addMessage("Sen", text)
        input.setText("")
        val lower = text.lowercase()
        val reply = when {
            listOf("salom", "assalom", "hello", "hi").any { lower.contains(it) } ->
                "Salom-salom! 😊 Bugun kayfiyating qanday?"
            listOf("isming", "kimsan", "kim san").any { lower.contains(it) } ->
                "Meni Malika deb atashing mumkin. Men sening chibi hamrohingman 👑"
            listOf("rahmat", "raxmat").any { lower.contains(it) } ->
                "Arzimaydi! 🖤 Yordam bera olganimdan xursandman."
            listOf("zerik", "xafa", "yomon").any { lower.contains(it) } ->
                "Yonindaman. Xohlasang, nima bo‘lganini aytib ber."
            listOf("anime", "manga", "manhwa").any { lower.contains(it) } ->
                "Anime va manhwa mavzusi zo‘r! Qaysi qahramon senga ko‘proq yoqadi?"
            lower.contains("?") ->
                "Qiziq savol! Hozirgi demo rejimida internetdagi ma’lumotlarni tekshira olmayman, lekin suhbatni davom ettiramiz 😊"
            else -> "Tushundim 🖤 Yana aytib ber-chi — seni tinglayapman."
        }
        addMessage("Malika", reply)
    }

    private fun addMessage(who: String, text: String) {
        messages.addView(TextView(this).apply {
            this.text = "$who: $text"
            textSize = 16f
            setTextColor(if (who == "Malika") Color.rgb(255, 190, 220) else Color.WHITE)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(if (who == "Malika") Color.rgb(52, 22, 47) else Color.rgb(37, 37, 45))
                cornerRadius = dp(14).toFloat()
            }
        }, LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, dp(4), 0, dp(4)) })
    }
}