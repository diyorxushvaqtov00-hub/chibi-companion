package uz.diyor.companion

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(20, 8, 20)
        window.navigationBarColor = Color.rgb(20, 8, 20)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(28), dp(24), dp(28))
            setBackgroundColor(Color.rgb(20, 8, 20))
        }
        root.addView(TextView(this).apply {
            text = "👑"
            textSize = 48f
            gravity = Gravity.CENTER
        })
        root.addView(TextView(this).apply {
            text = "CHIBI COMPANION"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(0, dp(8), 0, dp(8))
        })
        root.addView(TextView(this).apply {
            text = "Malika endi telefon ekraningda hamrohing bo‘lishga tayyor.\n\nEkran ustida ko‘rinishi uchun maxsus ruxsat kerak."
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
            setPadding(0, dp(8), 0, dp(24))
        })

        root.addView(makeButton("1. Ekran ustida ko‘rinishga ruxsat berish") {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName"))
                startActivity(intent)
            } else {
                Toast.makeText(this, "Ruxsat allaqachon berilgan", Toast.LENGTH_SHORT).show()
            }
        })
        root.addView(makeButton("2. Malika yordamchini ishga tushirish") {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Avval ekran ustida ko‘rinish ruxsatini bering", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(this, PetService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
                else startService(intent)
                Toast.makeText(this, "Malika ishga tushdi!", Toast.LENGTH_SHORT).show()
            }
        })
        root.addView(makeButton("Yordamchini to‘xtatish") {
            stopService(Intent(this, PetService::class.java))
        })
        root.addView(TextView(this).apply {
            text = "Maslahat: yordamchini barmog‘ing bilan sudrab joyini o‘zgartir. Ustiga bosib salomlash."
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.GRAY)
            setPadding(0, dp(24), 0, 0)
        })
        setContentView(root)
    }

    private fun makeButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        setOnClickListener { action() }
        val p = LinearLayout.LayoutParams(-1, -2)
        p.setMargins(0, dp(5), 0, dp(5))
        layoutParams = p
    }
}
