package uz.diyor.companion

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
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
    companion object {
        private const val REQUEST_NOTIFICATIONS = 410
    }

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
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(28), dp(24), dp(28))
            setBackgroundColor(Color.rgb(12, 10, 22))
        }
        root.addView(TextView(this).apply {
            text = "✦"
            textSize = 54f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(190, 155, 255))
        })
        root.addView(TextView(this).apply {
            text = "A I R A"
            textSize = 30f
            letterSpacing = 0.28f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(0, dp(8), 0, dp(8))
        })
        root.addView(TextView(this).apply {
            text = "Sening virtual hamrohing.\nSovuqqon aql, yaqinlarga mehr.\n\nMahalliy AI • Xotira • Ovoz • Rasm matnini o‘qish."
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(202, 196, 220))
            setPadding(0, dp(8), 0, dp(24))
        })

        root.addView(makeButton("1. Ekran ustida ko‘rinishga ruxsat berish") {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            } else {
                Toast.makeText(this, "Ruxsat allaqachon berilgan", Toast.LENGTH_SHORT).show()
            }
        })
        root.addView(makeButton("2. AIRA yordamchini ishga tushirish") {
            startCompanion()
        })
        root.addView(makeButton("Rasmni AIRA bilan o‘qish") {
            startActivity(Intent(this, VisionActivity::class.java))
        })
        root.addView(makeButton("AIRA xotiralarini boshqarish") {
            startActivity(Intent(this, MemoryActivity::class.java))
        })
        root.addView(makeButton("AI modelni yuklab olish (614 MB)") {
            startActivity(Intent(this, ModelDownloadActivity::class.java))
        })
        root.addView(makeButton("AIRA bilan suhbatlashish") {
            startActivity(Intent(this, ChatActivity::class.java))
        })
        root.addView(TextView(this).apply {
            text = "AIRA mahalliy AI modelini alohida yuklab ishlata oladi. Model o‘rnatilmagan bo‘lsa, suhbat offline demo rejimida ishlaydi. Ovoz ishlaydi. Rasm matnini telefonda ajratish ham mavjud. 3D keyingi bosqichda."
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(165, 145, 205))
            setPadding(0, dp(12), 0, dp(8))
        })
        root.addView(makeButton("AIRA ni to‘xtatish") {
            stopService(Intent(this, PetService::class.java))
        })
        root.addView(TextView(this).apply {
            text = "Ekrandagi AIRA ni sudrab joyini o‘zgartir. Unga tegsang, suhbat ochiladi."
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.GRAY)
            setPadding(0, dp(24), 0, 0)
        })
        setContentView(android.widget.ScrollView(this).apply {
            isFillViewport = true
            addView(root)
        })
    }

    private fun startCompanion() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Avval ekran ustida ko‘rinish ruxsatini bering", Toast.LENGTH_LONG).show()
            return
        }

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATIONS)
            return
        }
        launchCompanionService()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCompanionService()
            } else {
                Toast.makeText(
                    this,
                    "Bildirishnoma ruxsati berilmadi. AIRA ishga tushadi, lekin bildirishnoma ko‘rinishi cheklanishi mumkin.",
                    Toast.LENGTH_LONG
                ).show()
                launchCompanionService()
            }
        }
    }

    private fun launchCompanionService() {
        val intent = Intent(this, PetService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent) else startService(intent)
        Toast.makeText(this, "AIRA ishga tushdi", Toast.LENGTH_SHORT).show()
    }

    private fun makeButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        setTextColor(Color.WHITE)
        backgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(91, 65, 145))
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, dp(5), 0, dp(5))
        }
    }
}
