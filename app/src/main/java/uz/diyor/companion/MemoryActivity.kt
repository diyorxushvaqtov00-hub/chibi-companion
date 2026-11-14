package uz.diyor.companion

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import uz.diyor.companion.memory.AiraMemory
import uz.diyor.companion.memory.MemoryStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MemoryActivity : Activity() {
    private lateinit var listContainer: LinearLayout
    private lateinit var input: EditText
    private lateinit var countLabel: TextView
    private val store by lazy { MemoryStore(applicationContext) }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(12, 10, 22)
        window.navigationBarColor = Color.rgb(12, 10, 22)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(16), dp(18), dp(16))
            setBackgroundColor(Color.rgb(12, 10, 22))
        }
        root.addView(TextView(this).apply {
            text = "✦  AIRA XOTIRASI"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(205, 185, 255))
            setPadding(0, 0, 0, dp(12))
        })
        root.addView(TextView(this).apply {
            text = "AIRA faqat sen shu yerda yozib saqlagan ma’lumotlarni eslab qoladi. Xohlagan payting o‘chirishing mumkin."
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
            setPadding(0, 0, 0, dp(14))
        })
        input = EditText(this).apply {
            hint = "Masalan: Men o‘zbek tilida suhbatlashishni afzal ko‘raman."
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(Color.rgb(35, 30, 49), dp(12).toFloat())
            minLines = 2
            maxLines = 3
        }
        root.addView(input, LinearLayout.LayoutParams(-1, -2))
        root.addView(Button(this).apply {
            text = "Xotiraga saqlash"
            isAllCaps = false
            setOnClickListener { saveMemory() }
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })

        countLabel = TextView(this).apply {
            textSize = 13f
            setTextColor(Color.rgb(190, 170, 230))
            setPadding(0, dp(16), 0, dp(8))
        }
        root.addView(countLabel)
        listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(listContainer)
        }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(Button(this).apply {
            text = "Barcha xotiralarni o‘chirish"
            isAllCaps = false
            setOnClickListener { confirmClear() }
        })
        setContentView(root)
        reload()
    }

    private fun saveMemory() {
        val value = input.text.toString().trim()
        if (value.isEmpty()) {
            Toast.makeText(this, "Avval xotira matnini yoz.", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            store.add(value)
            input.setText("")
            reload()
            Toast.makeText(this, "Xotira saqlandi.", Toast.LENGTH_SHORT).show()
        } catch (error: Exception) {
            Toast.makeText(this, error.message ?: "Saqlashda xato", Toast.LENGTH_LONG).show()
        }
    }

    private fun reload() {
        val items = store.list()
        countLabel.text = "Saqlangan xotiralar: ${items.size}"
        listContainer.removeAllViews()
        if (items.isEmpty()) {
            listContainer.addView(TextView(this).apply {
                text = "Hozircha xotira yo‘q. Yuqoridan birinchi xotirani qo‘shishing mumkin."
                setTextColor(Color.GRAY)
                gravity = Gravity.CENTER
                setPadding(dp(12), dp(28), dp(12), dp(28))
            })
        } else {
            items.forEach { addMemoryCard(it) }
        }
    }

    private fun addMemoryCard(memory: AiraMemory) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(Color.rgb(35, 30, 49), dp(14).toFloat())
        }
        card.addView(TextView(this).apply {
            text = memory.content
            textSize = 14f
            setTextColor(Color.WHITE)
        })
        card.addView(TextView(this).apply {
            text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(Date(memory.createdAt))
            textSize = 10f
            setTextColor(Color.LTGRAY)
            setPadding(0, dp(6), 0, dp(2))
        })
        card.addView(Button(this).apply {
            text = "O‘chirish"
            isAllCaps = false
            setOnClickListener {
                AlertDialog.Builder(this@MemoryActivity)
                    .setTitle("Xotirani o‘chirish")
                    .setMessage("Bu ma’lumot AIRA xotirasidan o‘chiriladi.")
                    .setNegativeButton("Bekor qilish", null)
                    .setPositiveButton("O‘chirish") { _, _ ->
                        store.delete(memory.id)
                        reload()
                    }
                    .show()
            }
        })
        listContainer.addView(card, LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = dp(8)
        })
    }

    private fun confirmClear() {
        AlertDialog.Builder(this)
            .setTitle("Barcha xotiralar o‘chirilsinmi?")
            .setMessage("Bu amalni ortga qaytarib bo‘lmaydi.")
            .setNegativeButton("Bekor qilish", null)
            .setPositiveButton("Hammasini o‘chirish") { _, _ ->
                store.clear()
                reload()
            }
            .show()
    }

    private fun rounded(color: Int, radius: Float) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius
    }

    override fun onDestroy() {
        store.close()
        super.onDestroy()
    }
}
